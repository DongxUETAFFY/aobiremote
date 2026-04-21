package io.github.dongxuetaffy.aobihelper.publiczone.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.dongxuetaffy.aobihelper.BackendApplication;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostPageQuery;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.service.PublicPostService;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostPageResponseVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:public_post_sort_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-h2.sql",
        "app.auth.cache.type=memory",
        "app.auth.mail-enabled=false"
    }
)
@ActiveProfiles("local-lite")
@Transactional
class PublicPostServiceImplTest {
    @Autowired
    private PublicPostService publicPostService;

    @Autowired
    private PublicPostMapper publicPostMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Test
    void pagePublicPostsSortsByPriceAscendingAndDescending() {
        Long userId = createUser();
        insertPublicPost(userId, "Middle Public Price", "80.00");
        insertPublicPost(userId, "Lowest Public Price", "20.00");
        insertPublicPost(userId, "Highest Public Price", "150.00");

        PublicPostPageQuery ascendingQuery = new PublicPostPageQuery();
        ascendingQuery.setPageNo(1L);
        ascendingQuery.setPageSize(30L);
        ascendingQuery.setSortType("priceAsc");

        PublicPostPageResponseVO ascendingResponse = publicPostService.pagePublicPosts(userId, ascendingQuery);

        assertThat(ascendingResponse.getItems())
            .extracting("itemName")
            .containsExactly("Lowest Public Price", "Middle Public Price", "Highest Public Price");

        PublicPostPageQuery descendingQuery = new PublicPostPageQuery();
        descendingQuery.setPageNo(1L);
        descendingQuery.setPageSize(30L);
        descendingQuery.setSortType("priceDesc");

        PublicPostPageResponseVO descendingResponse = publicPostService.pagePublicPosts(userId, descendingQuery);

        assertThat(descendingResponse.getItems())
            .extracting("itemName")
            .containsExactly("Highest Public Price", "Middle Public Price", "Lowest Public Price");
    }

    private Long createUser() {
        LocalDateTime now = LocalDateTime.now();
        UserAccount user = new UserAccount();
        user.setEmail("public-sort-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setNickname("public-sort-user");
        user.setAvatarUrl("");
        user.setStatus("active");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userAccountMapper.insert(user);
        return user.getId();
    }

    private void insertPublicPost(Long userId, String itemName, String price) {
        LocalDateTime now = LocalDateTime.now();
        PublicPost post = new PublicPost();
        post.setUserId(userId);
        post.setItemName(itemName);
        post.setPrice(new BigDecimal(price));
        post.setTradeTime(LocalDate.now());
        post.setDirection("sell");
        post.setChannel("xianyu");
        post.setCategory("obi");
        post.setUntrustedCount(0);
        post.setPublisherName("public-sort-user");
        post.setPublisherAvatar("");
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        publicPostMapper.insert(post);
    }
}
