package io.github.dongxuetaffy.aobihelper.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.config.FileProperties;
import io.github.dongxuetaffy.aobihelper.file.entity.FileAsset;
import io.github.dongxuetaffy.aobihelper.file.mapper.FileAssetMapper;
import io.github.dongxuetaffy.aobihelper.file.service.FileAssetService;
import io.github.dongxuetaffy.aobihelper.file.vo.FileUploadVO;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileAssetServiceImpl extends ServiceImpl<FileAssetMapper, FileAsset> implements FileAssetService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> PUBLIC_SCENES = Set.of("public", "public-post");

    private final FileProperties fileProperties;
    private final TransactionTemplate transactionTemplate;
    private final LocalFileStoreGuard localFileStoreGuard;
    private final ConcurrentMap<Long, PreviewMetadata> publicPreviewCache = new ConcurrentHashMap<>();

    public FileAssetServiceImpl(FileProperties fileProperties, TransactionTemplate transactionTemplate) {
        this.fileProperties = fileProperties;
        this.transactionTemplate = transactionTemplate;
        this.localFileStoreGuard = new LocalFileStoreGuard(fileProperties.getMaxConcurrentImageStores());
    }

    @Override
    public FileUploadVO uploadImage(Long userId, String scene, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Image file is required");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(BusinessCode.FILE_TYPE_UNSUPPORTED, "Only JPEG, PNG or WEBP images are supported");
        }

        long fileSize = file.getSize();
        if (fileSize <= 0 || fileSize > fileProperties.getMaxImageBytes()) {
            throw new BusinessException(
                BusinessCode.UPLOAD_FAILED,
                "Image size exceeds the allowed limit"
            );
        }
        byte[] fileBytes = readFileBytes(file);
        ensureImageSignature(contentType, fileBytes);

        boolean isPublic = resolvePublicScene(scene);
        LocalDate today = LocalDate.now();
        String objectKey = userId + "/" + today + "/" + UUID.randomUUID() + resolveExtension(contentType);
        Path rootPath = Paths.get(fileProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path targetPath = resolveTargetPath(rootPath, objectKey);
        storeImageFile(rootPath, targetPath, fileBytes, contentType);

        FileAsset fileAsset = new FileAsset();
        LocalDateTime now = LocalDateTime.now();
        fileAsset.setUserId(userId);
        fileAsset.setStorageProvider("local");
        fileAsset.setBucketName("local");
        fileAsset.setObjectKey(objectKey);
        fileAsset.setOriginalName(resolveOriginalName(file.getOriginalFilename()));
        fileAsset.setContentType(contentType);
        fileAsset.setFileSize(fileSize);
        fileAsset.setIsPublic(isPublic);
        fileAsset.setCreatedAt(now);
        fileAsset.setUpdatedAt(now);
        try {
            transactionTemplate.executeWithoutResult(status -> baseMapper.insert(fileAsset));
        } catch (RuntimeException exception) {
            deleteStoredFile(targetPath);
            throw exception;
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setFileId(String.valueOf(fileAsset.getId()));
        vo.setPreviewUrl("/api/files/" + fileAsset.getId() + "/preview");
        vo.setFileSize(fileSize);
        vo.setContentType(contentType);
        return vo;
    }

    @Override
    public FilePreviewResult loadPreview(Long currentUserId, String fileId) {
        Long parsedFileId = parseFileId(fileId);
        PreviewMetadata cachedMetadata = publicPreviewCache.get(parsedFileId);
        if (cachedMetadata != null) {
            return loadCachedPublicPreview(cachedMetadata);
        }

        FileAsset fileAsset = baseMapper.selectById(parsedFileId);
        if (fileAsset == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Image not found");
        }

        if (!Boolean.TRUE.equals(fileAsset.getIsPublic())) {
            if (currentUserId == null) {
                throw new BusinessException(BusinessCode.UNAUTHORIZED, "Login required");
            }
            if (!fileAsset.getUserId().equals(currentUserId)) {
                throw new BusinessException(BusinessCode.FORBIDDEN, "You can only access your own image");
            }
        }

        Resource resource = loadResource(fileAsset.getObjectKey(), parsedFileId);
        if (Boolean.TRUE.equals(fileAsset.getIsPublic())) {
            publicPreviewCache.put(parsedFileId, PreviewMetadata.from(fileAsset));
        }
        return new FilePreviewResult(fileAsset, resource);
    }

    private FilePreviewResult loadCachedPublicPreview(PreviewMetadata metadata) {
        Resource resource = loadResource(metadata.objectKey(), metadata.id());
        return new FilePreviewResult(metadata.toFileAsset(), resource);
    }

    private Resource loadResource(String objectKey, Long fileId) {
        Path rootPath = Paths.get(fileProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path filePath = resolveTargetPath(rootPath, objectKey);
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists() || !resource.isReadable()) {
            publicPreviewCache.remove(fileId);
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Image file not found");
        }
        return resource;
    }

    private Long parseFileId(String fileId) {
        try {
            return Long.parseLong(fileId);
        } catch (NumberFormatException exception) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Invalid file id");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        return contentType.trim().toLowerCase();
    }

    private String resolveOriginalName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "image";
        }
        return originalFilename.trim();
    }

    private boolean resolvePublicScene(String scene) {
        if (scene == null || scene.isBlank()) {
            return false;
        }
        return PUBLIC_SCENES.contains(scene.trim().toLowerCase());
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private Path resolveTargetPath(Path rootPath, String objectKey) {
        Path targetPath = rootPath.resolve(objectKey).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new BusinessException(BusinessCode.UPLOAD_FAILED, "Invalid file target");
        }
        return targetPath;
    }

    private void storeImageFile(Path rootPath, Path targetPath, byte[] fileBytes, String contentType) {
        Path tempPath = rootPath.resolve(".tmp").resolve(UUID.randomUUID() + resolveExtension(contentType)).normalize();
        if (!tempPath.startsWith(rootPath)) {
            throw new BusinessException(BusinessCode.UPLOAD_FAILED, "Invalid temporary upload target");
        }
        try {
            localFileStoreGuard.execute(() -> writeImageFile(tempPath, targetPath, fileBytes));
        } catch (IOException exception) {
            deleteStoredFile(tempPath);
            deleteStoredFile(targetPath);
            throw new BusinessException(BusinessCode.UPLOAD_FAILED, "Failed to store image");
        }
    }

    private void writeImageFile(Path tempPath, Path targetPath, byte[] fileBytes) throws IOException {
        try {
            writeImageFileOnce(tempPath, targetPath, fileBytes);
        } catch (NoSuchFileException exception) {
            localFileStoreGuard.forgetDirectory(tempPath.getParent());
            localFileStoreGuard.forgetDirectory(targetPath.getParent());
            writeImageFileOnce(tempPath, targetPath, fileBytes);
        }
    }

    private void writeImageFileOnce(Path tempPath, Path targetPath, byte[] fileBytes) throws IOException {
        localFileStoreGuard.ensureDirectoryExists(tempPath.getParent());
        localFileStoreGuard.ensureDirectoryExists(targetPath.getParent());
        Files.write(tempPath, fileBytes);
        moveTempFile(tempPath, targetPath);
    }

    private void moveTempFile(Path tempPath, Path targetPath) throws IOException {
        try {
            Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteStoredFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Best-effort cleanup only; the request should surface the original failure.
        }
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessException(BusinessCode.UPLOAD_FAILED, "Failed to read image");
        }
    }

    private void ensureImageSignature(String contentType, byte[] bytes) {
        boolean matched = switch (contentType) {
            case "image/jpeg" -> startsWith(bytes, 0xFF, 0xD8, 0xFF);
            case "image/png" -> startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/webp" -> isWebp(bytes);
            default -> false;
        };
        if (!matched) {
            throw new BusinessException(BusinessCode.FILE_TYPE_UNSUPPORTED, "Image content does not match its type");
        }
    }

    private boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index += 1) {
            if ((bytes[index] & 0xFF) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12
            && bytes[0] == 'R'
            && bytes[1] == 'I'
            && bytes[2] == 'F'
            && bytes[3] == 'F'
            && bytes[8] == 'W'
            && bytes[9] == 'E'
            && bytes[10] == 'B'
            && bytes[11] == 'P';
    }

    private record PreviewMetadata(
        Long id,
        Long userId,
        String storageProvider,
        String bucketName,
        String objectKey,
        String originalName,
        String contentType,
        Long fileSize,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        private static PreviewMetadata from(FileAsset fileAsset) {
            return new PreviewMetadata(
                fileAsset.getId(),
                fileAsset.getUserId(),
                fileAsset.getStorageProvider(),
                fileAsset.getBucketName(),
                fileAsset.getObjectKey(),
                fileAsset.getOriginalName(),
                fileAsset.getContentType(),
                fileAsset.getFileSize(),
                fileAsset.getCreatedAt(),
                fileAsset.getUpdatedAt()
            );
        }

        private FileAsset toFileAsset() {
            FileAsset fileAsset = new FileAsset();
            fileAsset.setId(id);
            fileAsset.setUserId(userId);
            fileAsset.setStorageProvider(storageProvider);
            fileAsset.setBucketName(bucketName);
            fileAsset.setObjectKey(objectKey);
            fileAsset.setOriginalName(originalName);
            fileAsset.setContentType(contentType);
            fileAsset.setFileSize(fileSize);
            fileAsset.setIsPublic(Boolean.TRUE);
            fileAsset.setCreatedAt(createdAt);
            fileAsset.setUpdatedAt(updatedAt);
            return fileAsset;
        }
    }
}
