package io.github.dongxuetaffy.aobihelper.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.dongxuetaffy.aobihelper.BackendApplication;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryBatchDeleteRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryBatchTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryPageQuery;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.inventory.entity.InventoryItem;
import io.github.dongxuetaffy.aobihelper.inventory.mapper.InventoryItemMapper;
import io.github.dongxuetaffy.aobihelper.inventory.service.InventoryItemService;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryPageResponseVO;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPostFlag;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostFlagMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:inventory_summary_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-h2.sql",
        "app.auth.cache.type=memory",
        "app.auth.mail-enabled=false"
    }
)
@ActiveProfiles("local-lite")
@Transactional
class InventoryItemServiceImplTest {
    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private InventoryItemMapper inventoryItemMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private PublicPostMapper publicPostMapper;

    @Autowired
    private PublicPostFlagMapper publicPostFlagMapper;

    @Test
    void pageWarehouseItemsKeepsSummaryIndependentFromCategoryFilter() {
        Long userId = createUser();
        insertInventoryItem(userId, "Star Dress", "obi", "unsold", "100.00");
        insertInventoryItem(userId, "Star Wand", "magic", "unsold", "200.00");
        insertInventoryItem(userId, "Star Sold Item", "magic", "sold", "300.00");
        insertInventoryItem(createUser(), "Star Other User Item", "obi", "unsold", "400.00");

        InventoryPageQuery query = new InventoryPageQuery();
        query.setPageNo(1L);
        query.setPageSize(30L);
        query.setKeyword("Star");
        query.setCategory("obi");
        query.setPriceRange("50-250");

        InventoryPageResponseVO response = inventoryItemService.pageWarehouseItems(userId, query);

        assertThat(response.getTotalCount()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getItemName()).isEqualTo("Star Dress");
        assertThat(response.getSummary().getTotalBuyPrice()).isEqualByComparingTo("300.00");
        assertThat(response.getSummary().getObiCount()).isEqualTo(1);
        assertThat(response.getSummary().getObiBuyPrice()).isEqualByComparingTo("100.00");
        assertThat(response.getSummary().getMagicCount()).isEqualTo(1);
        assertThat(response.getSummary().getMagicBuyPrice()).isEqualByComparingTo("200.00");
    }

    @Test
    void pageWarehouseItemsSortsByBuyPriceAscendingAndDescending() {
        Long userId = createUser();
        insertInventoryItem(userId, "Middle Price", "obi", "unsold", "80.00");
        insertInventoryItem(userId, "Lowest Price", "obi", "unsold", "20.00");
        insertInventoryItem(userId, "Highest Price", "magic", "unsold", "150.00");

        InventoryPageQuery ascendingQuery = new InventoryPageQuery();
        ascendingQuery.setPageNo(1L);
        ascendingQuery.setPageSize(30L);
        ascendingQuery.setSortType("buyPriceAsc");

        InventoryPageResponseVO ascendingResponse = inventoryItemService.pageWarehouseItems(userId, ascendingQuery);

        assertThat(ascendingResponse.getItems())
            .extracting("itemName")
            .containsExactly("Lowest Price", "Middle Price", "Highest Price");

        InventoryPageQuery descendingQuery = new InventoryPageQuery();
        descendingQuery.setPageNo(1L);
        descendingQuery.setPageSize(30L);
        descendingQuery.setSortType("buyPriceDesc");

        InventoryPageResponseVO descendingResponse = inventoryItemService.pageWarehouseItems(userId, descendingQuery);

        assertThat(descendingResponse.getItems())
            .extracting("itemName")
            .containsExactly("Highest Price", "Middle Price", "Lowest Price");
    }

    @Test
    void batchDeleteInventoryItemsDeletesInBatchAndCleansPublicPosts() {
        Long userId = createUser();
        Long firstItemId = insertInventoryItem(userId, "Batch Inventory A", "obi", "unsold", "100.00");
        Long secondItemId = insertInventoryItem(userId, "Batch Inventory B", "magic", "sold", "200.00");
        Long firstPostId = insertLinkedPublicPost(userId, firstItemId, "Batch Inventory A");
        Long secondPostId = insertLinkedPublicPost(userId, secondItemId, "Batch Inventory B");
        Long firstFlagId = insertPublicPostFlag(userId, firstPostId);
        Long secondFlagId = insertPublicPostFlag(userId, secondPostId);

        InventoryBatchDeleteRequest request = new InventoryBatchDeleteRequest();
        request.setIds(List.of(firstItemId, secondItemId, firstItemId));
        request.setRequestId("batch-delete-inventory-" + UUID.randomUUID());

        inventoryItemService.batchDeleteInventoryItems(userId, request);

        assertThat(inventoryItemMapper.selectById(firstItemId)).isNull();
        assertThat(inventoryItemMapper.selectById(secondItemId)).isNull();
        assertThat(publicPostMapper.selectById(firstPostId)).isNull();
        assertThat(publicPostMapper.selectById(secondPostId)).isNull();
        assertThat(publicPostFlagMapper.selectById(firstFlagId)).isNull();
        assertThat(publicPostFlagMapper.selectById(secondFlagId)).isNull();
    }

    @Test
    void batchTogglePublicTogglesInventoryItemsInBatch() {
        Long userId = createUser();
        Long itemToPublishId = insertInventoryItem(userId, "Publish Inventory", "obi", "unsold", "100.00");
        Long itemToUnpublishId = insertInventoryItem(userId, "Unpublish Inventory", "magic", "unsold", "200.00");
        Long oldPostId = insertLinkedPublicPost(userId, itemToUnpublishId, "Unpublish Inventory");
        Long oldFlagId = insertPublicPostFlag(userId, oldPostId);

        InventoryBatchTogglePublicRequest request = new InventoryBatchTogglePublicRequest();
        request.setIds(List.of(itemToPublishId, itemToUnpublishId, itemToPublishId));
        request.setRequestId("batch-toggle-inventory-public-" + UUID.randomUUID());

        inventoryItemService.batchTogglePublic(userId, request);

        InventoryItem publishedItem = inventoryItemMapper.selectById(itemToPublishId);
        InventoryItem unpublishedItem = inventoryItemMapper.selectById(itemToUnpublishId);
        PublicPost newPost = publicPostMapper.selectOne(
            new LambdaQueryWrapper<PublicPost>()
                .eq(PublicPost::getSourceInventoryItemId, itemToPublishId)
                .last("limit 1")
        );

        assertThat(publishedItem.getPublicPosted()).isTrue();
        assertThat(publishedItem.getPublicPostId()).isNotNull();
        assertThat(newPost).isNotNull();
        assertThat(newPost.getPrice()).isEqualByComparingTo("100.00");
        assertThat(newPost.getDirection()).isEqualTo("buy");
        assertThat(unpublishedItem.getPublicPosted()).isFalse();
        assertThat(unpublishedItem.getPublicPostId()).isNull();
        assertThat(publicPostMapper.selectById(oldPostId)).isNull();
        assertThat(publicPostFlagMapper.selectById(oldFlagId)).isNull();
    }

    @Test
    void batchTogglePublicDoesNotShareFrequencyGuardWithSingleToggle() {
        Long userId = createUser();
        Long singleItemId = insertInventoryItem(userId, "Single Public Inventory", "obi", "unsold", "100.00");
        Long batchItemId = insertInventoryItem(userId, "Batch Public Inventory", "magic", "unsold", "200.00");

        InventoryTogglePublicRequest singleRequest = new InventoryTogglePublicRequest();
        singleRequest.setPrice(new BigDecimal("100.00"));
        singleRequest.setTradeTime(LocalDate.now());
        singleRequest.setDirection("buy");
        singleRequest.setRequestId("single-toggle-inventory-public-" + UUID.randomUUID());
        inventoryItemService.togglePublic(userId, singleItemId, singleRequest);

        InventoryBatchTogglePublicRequest batchRequest = new InventoryBatchTogglePublicRequest();
        batchRequest.setIds(List.of(batchItemId));
        batchRequest.setRequestId("batch-toggle-inventory-public-" + UUID.randomUUID());

        assertThatCode(() -> inventoryItemService.batchTogglePublic(userId, batchRequest))
            .doesNotThrowAnyException();
    }

    private Long createUser() {
        LocalDateTime now = LocalDateTime.now();
        UserAccount user = new UserAccount();
        user.setEmail("perf-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setNickname("perf-user");
        user.setAvatarUrl("");
        user.setStatus("active");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userAccountMapper.insert(user);
        return user.getId();
    }

    private Long insertInventoryItem(Long userId, String itemName, String category, String status, String buyPrice) {
        LocalDateTime now = LocalDateTime.now();
        InventoryItem item = new InventoryItem();
        item.setUserId(userId);
        item.setItemName(itemName);
        item.setBuyPrice(new BigDecimal(buyPrice));
        item.setBuyTime(LocalDate.now().minusDays(1));
        item.setChannel("xianyu");
        item.setCategory(category);
        item.setStatus(status);
        item.setPublicPosted(Boolean.FALSE);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        inventoryItemMapper.insert(item);
        return item.getId();
    }

    private Long insertLinkedPublicPost(Long userId, Long itemId, String itemName) {
        LocalDateTime now = LocalDateTime.now();
        PublicPost post = new PublicPost();
        post.setUserId(userId);
        post.setSourceInventoryItemId(itemId);
        post.setItemName(itemName);
        post.setPrice(new BigDecimal("100.00"));
        post.setTradeTime(LocalDate.now());
        post.setDirection("buy");
        post.setChannel("xianyu");
        post.setCategory("obi");
        post.setUntrustedCount(0);
        post.setPublisherName("perf-user");
        post.setPublisherAvatar("");
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        publicPostMapper.insert(post);

        InventoryItem item = inventoryItemMapper.selectById(itemId);
        item.setPublicPosted(Boolean.TRUE);
        item.setPublicPostId(post.getId());
        item.setPublicPostedAt(now);
        inventoryItemMapper.updateById(item);
        return post.getId();
    }

    private Long insertPublicPostFlag(Long userId, Long postId) {
        PublicPostFlag flag = new PublicPostFlag();
        flag.setUserId(userId);
        flag.setPostId(postId);
        flag.setCreatedAt(LocalDateTime.now());
        publicPostFlagMapper.insert(flag);
        return flag.getId();
    }
}
