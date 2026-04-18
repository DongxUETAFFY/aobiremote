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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileAssetServiceImpl extends ServiceImpl<FileAssetMapper, FileAsset> implements FileAssetService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> PUBLIC_SCENES = Set.of("public", "public-post");

    private final FileProperties fileProperties;

    public FileAssetServiceImpl(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    @Transactional
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
        Path targetPath = rootPath.resolve(objectKey).normalize();

        if (!targetPath.startsWith(rootPath)) {
            throw new BusinessException(BusinessCode.UPLOAD_FAILED, "Invalid upload target");
        }

        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, fileBytes);
        } catch (IOException exception) {
            throw new BusinessException(BusinessCode.UPLOAD_FAILED, "Failed to store image");
        }

        LocalDateTime now = LocalDateTime.now();
        FileAsset fileAsset = new FileAsset();
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
        baseMapper.insert(fileAsset);

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

        Path filePath = Paths.get(fileProperties.getLocalRoot()).toAbsolutePath().normalize().resolve(fileAsset.getObjectKey()).normalize();
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Image file not found");
        }
        return new FilePreviewResult(fileAsset, resource);
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
}
