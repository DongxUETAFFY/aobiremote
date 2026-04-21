package io.github.dongxuetaffy.aobihelper.trade.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.dongxuetaffy.aobihelper.BackendApplication;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.inventory.entity.InventoryItem;
import io.github.dongxuetaffy.aobihelper.inventory.mapper.InventoryItemMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPostFlag;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostFlagMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostMapper;
import io.github.dongxuetaffy.aobihelper.stats.entity.UserStats;
import io.github.dongxuetaffy.aobihelper.stats.mapper.UserStatsMapper;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeBatchDeleteRequest;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeBatchTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradePageQuery;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeUpsertRequest;
import io.github.dongxuetaffy.aobihelper.trade.service.TradeService;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradePageResponseVO;
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
        "spring.datasource.url=jdbc:h2:mem:trade_summary_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-h2.sql",
        "app.auth.cache.type=memory",
        "app.auth.mail-enabled=false"
    }
)
@ActiveProfiles("local-lite")
@Transactional
class TradeServiceImplTest {
    @Autowired
    private TradeService tradeService;

    @Autowired
    private InventoryItemMapper inventoryItemMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserStatsMapper userStatsMapper;

    @Autowired
    private PublicPostMapper publicPostMapper;

    @Autowired
    private PublicPostFlagMapper publicPostFlagMapper;

    @Test
    void pageTradeItemsKeepsExistingCategorySummarySemantics() {
        Long userId = createUser();
        insertTradeItem(userId, "Star Obi Profit", "obi", "100.00", "160.00");
        insertTradeItem(userId, "Star Magic Loss", "magic", "200.00", "150.00");
        insertUnsoldItem(userId, "Star Unsold Obi", "obi", "80.00");
        insertTradeItem(createUser(), "Star Other User", "obi", "300.00", "500.00");

        TradePageQuery query = new TradePageQuery();
        query.setPageNo(1L);
        query.setPageSize(30L);
        query.setKeyword("Star");
        query.setScope("all");
        query.setCategory("obi");

        TradePageResponseVO response = tradeService.pageTradeItems(userId, query);

        assertThat(response.getTotalCount()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getItemName()).isEqualTo("Star Obi Profit");
        assertThat(response.getSummary().getTotalBuyAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getSummary().getTotalSellAmount()).isEqualByComparingTo("160.00");
        assertThat(response.getSummary().getTotalProfit()).isEqualByComparingTo("60.00");
        assertThat(response.getSummary().getTotalLoss()).isEqualByComparingTo("0.00");
        assertThat(response.getSummary().getObiCount()).isEqualTo(1);
        assertThat(response.getSummary().getObiBuyAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getSummary().getMagicCount()).isEqualTo(1);
        assertThat(response.getSummary().getMagicBuyAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void pageTradeItemsAppliesLossScopeToSummary() {
        Long userId = createUser();
        insertTradeItem(userId, "Star Obi Profit", "obi", "100.00", "160.00");
        insertTradeItem(userId, "Star Magic Loss", "magic", "200.00", "150.00");

        TradePageQuery query = new TradePageQuery();
        query.setPageNo(1L);
        query.setPageSize(30L);
        query.setKeyword("Star");
        query.setScope("loss");

        TradePageResponseVO response = tradeService.pageTradeItems(userId, query);

        assertThat(response.getTotalCount()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getItemName()).isEqualTo("Star Magic Loss");
        assertThat(response.getSummary().getTotalBuyAmount()).isEqualByComparingTo("200.00");
        assertThat(response.getSummary().getTotalSellAmount()).isEqualByComparingTo("150.00");
        assertThat(response.getSummary().getTotalProfit()).isEqualByComparingTo("0.00");
        assertThat(response.getSummary().getTotalLoss()).isEqualByComparingTo("50.00");
        assertThat(response.getSummary().getObiCount()).isEqualTo(0);
        assertThat(response.getSummary().getMagicCount()).isEqualTo(1);
        assertThat(response.getSummary().getMagicBuyAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void pageTradeItemsSortsBySellPriceAscendingAndDescending() {
        Long userId = createUser();
        insertTradeItem(userId, "Middle Sell Price", "obi", "30.00", "80.00");
        insertTradeItem(userId, "Lowest Sell Price", "obi", "20.00", "35.00");
        insertTradeItem(userId, "Highest Sell Price", "magic", "100.00", "160.00");

        TradePageQuery ascendingQuery = new TradePageQuery();
        ascendingQuery.setPageNo(1L);
        ascendingQuery.setPageSize(30L);
        ascendingQuery.setScope("all");
        ascendingQuery.setSortType("sellPriceAsc");

        TradePageResponseVO ascendingResponse = tradeService.pageTradeItems(userId, ascendingQuery);

        assertThat(ascendingResponse.getItems())
            .extracting("itemName")
            .containsExactly("Lowest Sell Price", "Middle Sell Price", "Highest Sell Price");

        TradePageQuery descendingQuery = new TradePageQuery();
        descendingQuery.setPageNo(1L);
        descendingQuery.setPageSize(30L);
        descendingQuery.setScope("all");
        descendingQuery.setSortType("sellPriceDesc");

        TradePageResponseVO descendingResponse = tradeService.pageTradeItems(userId, descendingQuery);

        assertThat(descendingResponse.getItems())
            .extracting("itemName")
            .containsExactly("Highest Sell Price", "Middle Sell Price", "Lowest Sell Price");
    }

    @Test
    void createTradeIncrementallyUpdatesExistingUserStats() {
        Long userId = createUser();
        insertUserStats(userId);

        TradeUpsertRequest request = new TradeUpsertRequest();
        request.setItemName("Created Trade");
        request.setBuyPrice(new BigDecimal("20.00"));
        request.setBuyTime(LocalDate.now().minusDays(2));
        request.setSellPrice(new BigDecimal("35.00"));
        request.setSellTime(LocalDate.now().minusDays(1));
        request.setChannel("xianyu");
        request.setCategory("obi");
        request.setRequestId("create-trade-" + UUID.randomUUID());

        tradeService.createTrade(userId, request);

        UserStats stats = userStatsMapper.selectById(userId);
        assertThat(stats.getSoldCount()).isEqualTo(4);
        assertThat(stats.getSoldBuyTotal()).isEqualByComparingTo("120.00");
        assertThat(stats.getSoldSellTotal()).isEqualByComparingTo("147.00");
        assertThat(stats.getTotalProfit()).isEqualByComparingTo("25.00");
        assertThat(stats.getTotalLoss()).isEqualByComparingTo("2.00");
        assertThat(stats.getUnsoldCount()).isEqualTo(5);
        assertThat(stats.getUnsoldBuyTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void updateTradeIncrementallyUpdatesExistingUserStats() {
        Long userId = createUser();
        Long itemId = insertTradeItem(userId, "Updated Trade", "magic", "50.00", "40.00");
        insertUserStats(
            userId,
            1,
            new BigDecimal("50.00"),
            new BigDecimal("40.00"),
            BigDecimal.ZERO,
            new BigDecimal("10.00"),
            5,
            new BigDecimal("50.00")
        );

        TradeUpsertRequest request = new TradeUpsertRequest();
        request.setItemName("Updated Trade");
        request.setBuyPrice(new BigDecimal("60.00"));
        request.setBuyTime(LocalDate.now().minusDays(2));
        request.setSellPrice(new BigDecimal("90.00"));
        request.setSellTime(LocalDate.now().minusDays(1));
        request.setChannel("xianyu");
        request.setCategory("magic");
        request.setRequestId("update-trade-" + UUID.randomUUID());

        tradeService.updateTrade(userId, itemId, request);

        UserStats stats = userStatsMapper.selectById(userId);
        assertThat(stats.getSoldCount()).isEqualTo(1);
        assertThat(stats.getSoldBuyTotal()).isEqualByComparingTo("60.00");
        assertThat(stats.getSoldSellTotal()).isEqualByComparingTo("90.00");
        assertThat(stats.getTotalProfit()).isEqualByComparingTo("30.00");
        assertThat(stats.getTotalLoss()).isEqualByComparingTo("0.00");
        assertThat(stats.getUnsoldCount()).isEqualTo(5);
        assertThat(stats.getUnsoldBuyTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void deleteTradeIncrementallyUpdatesExistingUserStats() {
        Long userId = createUser();
        Long itemId = insertTradeItem(userId, "Deleted Trade", "obi", "70.00", "40.00");
        insertUserStats(
            userId,
            1,
            new BigDecimal("70.00"),
            new BigDecimal("40.00"),
            BigDecimal.ZERO,
            new BigDecimal("30.00"),
            5,
            new BigDecimal("50.00")
        );

        tradeService.deleteTrade(userId, itemId, "delete-trade-" + UUID.randomUUID());

        UserStats stats = userStatsMapper.selectById(userId);
        assertThat(inventoryItemMapper.selectById(itemId)).isNull();
        assertThat(stats.getSoldCount()).isZero();
        assertThat(stats.getSoldBuyTotal()).isEqualByComparingTo("0.00");
        assertThat(stats.getSoldSellTotal()).isEqualByComparingTo("0.00");
        assertThat(stats.getTotalProfit()).isEqualByComparingTo("0.00");
        assertThat(stats.getTotalLoss()).isEqualByComparingTo("0.00");
        assertThat(stats.getUnsoldCount()).isEqualTo(5);
        assertThat(stats.getUnsoldBuyTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void batchDeleteTradeItemsDeletesInBatchAndUpdatesExistingUserStats() {
        Long userId = createUser();
        Long firstItemId = insertTradeItem(userId, "Batch Profit", "obi", "100.00", "130.00");
        Long secondItemId = insertTradeItem(userId, "Batch Loss", "magic", "80.00", "70.00");
        Long firstPostId = insertLinkedPublicPost(userId, firstItemId, "Batch Profit");
        Long secondPostId = insertLinkedPublicPost(userId, secondItemId, "Batch Loss");
        Long firstFlagId = insertPublicPostFlag(userId, firstPostId);
        Long secondFlagId = insertPublicPostFlag(userId, secondPostId);
        insertUserStats(
            userId,
            2,
            new BigDecimal("180.00"),
            new BigDecimal("200.00"),
            new BigDecimal("30.00"),
            new BigDecimal("10.00"),
            5,
            new BigDecimal("50.00")
        );

        TradeBatchDeleteRequest request = new TradeBatchDeleteRequest();
        request.setIds(List.of(firstItemId, secondItemId, firstItemId));
        request.setRequestId("batch-delete-trade-" + UUID.randomUUID());

        tradeService.batchDeleteTradeItems(userId, request);

        UserStats stats = userStatsMapper.selectById(userId);
        assertThat(inventoryItemMapper.selectById(firstItemId)).isNull();
        assertThat(inventoryItemMapper.selectById(secondItemId)).isNull();
        assertThat(publicPostMapper.selectById(firstPostId)).isNull();
        assertThat(publicPostMapper.selectById(secondPostId)).isNull();
        assertThat(publicPostFlagMapper.selectById(firstFlagId)).isNull();
        assertThat(publicPostFlagMapper.selectById(secondFlagId)).isNull();
        assertThat(stats.getSoldCount()).isZero();
        assertThat(stats.getSoldBuyTotal()).isEqualByComparingTo("0.00");
        assertThat(stats.getSoldSellTotal()).isEqualByComparingTo("0.00");
        assertThat(stats.getTotalProfit()).isEqualByComparingTo("0.00");
        assertThat(stats.getTotalLoss()).isEqualByComparingTo("0.00");
        assertThat(stats.getUnsoldCount()).isEqualTo(5);
        assertThat(stats.getUnsoldBuyTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void batchTogglePublicTogglesTradeItemsInBatch() {
        Long userId = createUser();
        Long itemToPublishId = insertTradeItem(userId, "Batch Publish", "obi", "100.00", "130.00");
        Long itemToUnpublishId = insertTradeItem(userId, "Batch Unpublish", "magic", "80.00", "70.00");
        Long oldPostId = insertLinkedPublicPost(userId, itemToUnpublishId, "Batch Unpublish");
        Long oldFlagId = insertPublicPostFlag(userId, oldPostId);

        TradeBatchTogglePublicRequest request = new TradeBatchTogglePublicRequest();
        request.setIds(List.of(itemToPublishId, itemToUnpublishId, itemToPublishId));
        request.setRequestId("batch-toggle-trade-public-" + UUID.randomUUID());

        tradeService.batchTogglePublic(userId, request);

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
        assertThat(newPost.getPrice()).isEqualByComparingTo("130.00");
        assertThat(newPost.getDirection()).isEqualTo("sell");
        assertThat(unpublishedItem.getPublicPosted()).isFalse();
        assertThat(unpublishedItem.getPublicPostId()).isNull();
        assertThat(publicPostMapper.selectById(oldPostId)).isNull();
        assertThat(publicPostFlagMapper.selectById(oldFlagId)).isNull();
    }

    private Long createUser() {
        LocalDateTime now = LocalDateTime.now();
        UserAccount user = new UserAccount();
        user.setEmail("trade-perf-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setNickname("trade-perf-user");
        user.setAvatarUrl("");
        user.setStatus("active");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userAccountMapper.insert(user);
        return user.getId();
    }

    private void insertUserStats(Long userId) {
        insertUserStats(
            userId,
            3,
            new BigDecimal("100.00"),
            new BigDecimal("112.00"),
            new BigDecimal("10.00"),
            new BigDecimal("2.00"),
            5,
            new BigDecimal("50.00")
        );
    }

    private void insertUserStats(
        Long userId,
        int soldCount,
        BigDecimal soldBuyTotal,
        BigDecimal soldSellTotal,
        BigDecimal totalProfit,
        BigDecimal totalLoss,
        int unsoldCount,
        BigDecimal unsoldBuyTotal
    ) {
        LocalDateTime now = LocalDateTime.now();
        UserStats stats = new UserStats();
        stats.setUserId(userId);
        stats.setTotalProfit(totalProfit);
        stats.setTotalLoss(totalLoss);
        stats.setSoldCount(soldCount);
        stats.setSoldBuyTotal(soldBuyTotal);
        stats.setSoldSellTotal(soldSellTotal);
        stats.setUnsoldCount(unsoldCount);
        stats.setUnsoldBuyTotal(unsoldBuyTotal);
        stats.setCreatedAt(now);
        stats.setUpdatedAt(now);
        userStatsMapper.insert(stats);
    }

    private Long insertTradeItem(Long userId, String itemName, String category, String buyPrice, String sellPrice) {
        BigDecimal buy = new BigDecimal(buyPrice);
        BigDecimal sell = new BigDecimal(sellPrice);
        InventoryItem item = buildBaseItem(userId, itemName, category, buy);
        item.setSellPrice(sell);
        item.setSellTime(LocalDate.now());
        item.setTradeTime(LocalDate.now());
        item.setStatus("sold");
        item.setProfitAmount(sell.subtract(buy));
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
        post.setDirection("sell");
        post.setChannel("xianyu");
        post.setCategory("obi");
        post.setUntrustedCount(0);
        post.setPublisherName("trade-perf-user");
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

    private void insertUnsoldItem(Long userId, String itemName, String category, String buyPrice) {
        InventoryItem item = buildBaseItem(userId, itemName, category, new BigDecimal(buyPrice));
        item.setStatus("unsold");
        inventoryItemMapper.insert(item);
    }

    private InventoryItem buildBaseItem(Long userId, String itemName, String category, BigDecimal buyPrice) {
        LocalDateTime now = LocalDateTime.now();
        InventoryItem item = new InventoryItem();
        item.setUserId(userId);
        item.setItemName(itemName);
        item.setBuyPrice(buyPrice);
        item.setBuyTime(LocalDate.now().minusDays(2));
        item.setChannel("xianyu");
        item.setCategory(category);
        item.setPublicPosted(Boolean.FALSE);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return item;
    }
}
