package io.github.dongxuetaffy.aobihelper.file.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import io.github.dongxuetaffy.aobihelper.file.service.FileAssetService;
import io.github.dongxuetaffy.aobihelper.file.vo.FileUploadVO;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileAssetService fileAssetService;

    public FileController(FileAssetService fileAssetService) {
        this.fileAssetService = fileAssetService;
    }

    @SaCheckLogin
    @PostMapping("/images")
    public ApiResponse<FileUploadVO> uploadImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String scene
    ) {
        return ApiResponse.success("Image uploaded", fileAssetService.uploadImage(StpUtil.getLoginIdAsLong(), scene, file));
    }

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<Resource> previewImage(@PathVariable String fileId) {
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        FileAssetService.FilePreviewResult previewResult = fileAssetService.loadPreview(currentUserId, fileId);
        MediaType mediaType = MediaType.parseMediaType(previewResult.fileAsset().getContentType());
        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(previewResult.fileAsset().getFileSize())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + previewResult.fileAsset().getOriginalName() + "\"")
            .cacheControl(CacheControl.noCache())
            .body(previewResult.resource());
    }
}
