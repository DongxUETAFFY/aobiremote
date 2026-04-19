package io.github.dongxuetaffy.aobihelper.file.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dongxuetaffy.aobihelper.BackendApplication;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.config.FileProperties;
import io.github.dongxuetaffy.aobihelper.file.entity.FileAsset;
import io.github.dongxuetaffy.aobihelper.file.mapper.FileAssetMapper;
import io.github.dongxuetaffy.aobihelper.file.service.FileAssetService;
import io.github.dongxuetaffy.aobihelper.file.vo.FileUploadVO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:file_asset_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-h2.sql",
        "app.auth.cache.type=memory",
        "app.auth.mail-enabled=false",
        "app.file.local-root=./target/aobi-file-test"
    }
)
@ActiveProfiles("local-lite")
@Transactional
class FileAssetServiceImplTest {
    @Autowired
    private FileAssetService fileAssetService;

    @Autowired
    private FileAssetMapper fileAssetMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private FileProperties fileProperties;

    @AfterEach
    void cleanUploadedFiles() throws Exception {
        Path rootPath = Path.of(fileProperties.getLocalRoot());
        if (!Files.exists(rootPath)) {
            return;
        }
        try (var paths = Files.walk(rootPath)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    void uploadImageStoresFileAndMetadata() {
        Long userId = createUser();
        MockMultipartFile file = pngFile("dress.png");

        FileUploadVO upload = fileAssetService.uploadImage(userId, "private", file);

        FileAsset asset = fileAssetMapper.selectById(Long.valueOf(upload.getFileId()));
        assertThat(asset).isNotNull();
        assertThat(asset.getUserId()).isEqualTo(userId);
        assertThat(asset.getContentType()).isEqualTo("image/png");
        assertThat(asset.getIsPublic()).isFalse();
        assertThat(Files.exists(Path.of(fileProperties.getLocalRoot()).resolve(asset.getObjectKey()))).isTrue();
        assertThat(fileAssetService.loadPreview(userId, upload.getFileId()).resource().exists()).isTrue();
    }

    @Test
    void loadPreviewKeepsPrivateImageAuthorization() {
        Long ownerId = createUser();
        Long otherUserId = createUser();
        FileUploadVO upload = fileAssetService.uploadImage(ownerId, "private", pngFile("private.png"));

        assertThatThrownBy(() -> fileAssetService.loadPreview(otherUserId, upload.getFileId()))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void loadPreviewAllowsPublicImageWithoutLogin() {
        Long ownerId = createUser();
        FileUploadVO upload = fileAssetService.uploadImage(ownerId, "public", pngFile("public.png"));

        FileAssetService.FilePreviewResult result = fileAssetService.loadPreview(null, upload.getFileId());

        assertThat(result.fileAsset().getIsPublic()).isTrue();
        assertThat(result.resource().exists()).isTrue();
    }

    private Long createUser() {
        LocalDateTime now = LocalDateTime.now();
        UserAccount user = new UserAccount();
        user.setEmail("file-test-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setNickname("file-user");
        user.setAvatarUrl("");
        user.setStatus("active");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userAccountMapper.insert(user);
        return user.getId();
    }

    private MockMultipartFile pngFile(String filename) {
        byte[] pngBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x00
        };
        return new MockMultipartFile("file", filename, "image/png", pngBytes);
    }
}
