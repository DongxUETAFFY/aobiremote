package io.github.dongxuetaffy.aobihelper.file.service;

import io.github.dongxuetaffy.aobihelper.file.entity.FileAsset;
import io.github.dongxuetaffy.aobihelper.file.vo.FileUploadVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileAssetService {
    FileUploadVO uploadImage(Long userId, String scene, MultipartFile file);

    FilePreviewResult loadPreview(Long currentUserId, String fileId);

    record FilePreviewResult(FileAsset fileAsset, Resource resource) {
    }
}
