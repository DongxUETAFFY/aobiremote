package io.github.dongxuetaffy.aobihelper.publiczone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostCreateRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostPageQuery;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostToggleUntrustedRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostUpdateRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostDetailVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostIdVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostPageResponseVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicSourceToggleVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostToggleUntrustedVO;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PublicPostService extends IService<PublicPost> {
    PublicPostPageResponseVO pagePublicPosts(Long currentUserId, PublicPostPageQuery query);

    PublicPostDetailVO getPublicPostDetail(Long currentUserId, Long postId);

    PublicPostIdVO createPublicPost(Long currentUserId, PublicPostCreateRequest request);

    PublicPostIdVO updatePublicPost(Long currentUserId, Long postId, PublicPostUpdateRequest request);

    PublicPostToggleUntrustedVO togglePublicPostUntrusted(
        Long currentUserId,
        Long postId,
        PublicPostToggleUntrustedRequest request
    );

    PublicSourceToggleVO toggleSourceItemPublicPost(
        Long currentUserId,
        Long sourceItemId,
        BigDecimal price,
        LocalDate tradeTime,
        String direction,
        String remark,
        String imageFileId,
        String requestId
    );

    void deletePublicPost(Long currentUserId, Long postId, String requestId);
}
