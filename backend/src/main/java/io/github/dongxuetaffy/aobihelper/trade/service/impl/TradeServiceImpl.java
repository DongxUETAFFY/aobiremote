package io.github.dongxuetaffy.aobihelper.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.common.guard.service.OperationGuardService;
import io.github.dongxuetaffy.aobihelper.inventory.entity.InventoryItem;
import io.github.dongxuetaffy.aobihelper.inventory.mapper.InventoryItemMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostFlagMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.service.PublicPostService;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicSourceToggleVO;
import io.github.dongxuetaffy.aobihelper.stats.entity.UserStats;
import io.github.dongxuetaffy.aobihelper.stats.mapper.UserStatsMapper;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradePageQuery;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeUpsertRequest;
import io.github.dongxuetaffy.aobihelper.trade.service.TradeService;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeDetailVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeIdVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeListItemVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradePageResponseVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeSummaryVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeTogglePublicVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeServiceImpl extends ServiceImpl<InventoryItemMapper, InventoryItem> implements TradeService {
    private static final String STATUS_SOLD = "sold";
    private static final String SCOPE_ALL = "all";
    private static final String SCOPE_PROFIT = "profit";
    private static final String SCOPE_LOSS = "loss";
    private static final Set<String> ALLOWED_CHANNELS = Set.of("xianyu", "tieba", "other");
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("magic", "obi");
    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 50L;

    private final InventoryItemMapper inventoryItemMapper;
    private final PublicPostMapper publicPostMapper;
    private final PublicPostFlagMapper publicPostFlagMapper;
    private final UserStatsMapper userStatsMapper;
    private final PublicPostService publicPostService;
    private final OperationGuardService operationGuardService;

    public TradeServiceImpl(
        InventoryItemMapper inventoryItemMapper,
        PublicPostMapper publicPostMapper,
        PublicPostFlagMapper publicPostFlagMapper,
        UserStatsMapper userStatsMapper,
        PublicPostService publicPostService,
        OperationGuardService operationGuardService
    ) {
        this.inventoryItemMapper = inventoryItemMapper;
        this.publicPostMapper = publicPostMapper;
        this.publicPostFlagMapper = publicPostFlagMapper;
        this.userStatsMapper = userStatsMapper;
        this.publicPostService = publicPostService;
        this.operationGuardService = operationGuardService;
    }

    @Override
    public TradePageResponseVO pageTradeItems(Long userId, TradePageQuery query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        String scope = normalizeScope(query.getScope());

        Page<InventoryItem> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<InventoryItem> pageWrapper = new LambdaQueryWrapper<InventoryItem>()
            .eq(InventoryItem::getUserId, userId)
            .eq(InventoryItem::getStatus, STATUS_SOLD);
        applyScopeFilter(pageWrapper, scope);
        pageWrapper.orderByDesc(InventoryItem::getSellTime).orderByDesc(InventoryItem::getUpdatedAt);

        Page<InventoryItem> resultPage = inventoryItemMapper.selectPage(page, pageWrapper);

        TradeSummaryVO summary = calculateSummary(userId, scope);

        TradePageResponseVO response = new TradePageResponseVO();
        response.setItems(resultPage.getRecords().stream().map(this::toListItem).toList());
        response.setPageNo(resultPage.getCurrent());
        response.setPageSize(resultPage.getSize());
        response.setTotalCount(resultPage.getTotal());
        response.setTotalPages(resultPage.getPages());
        response.setHasMore(resultPage.getCurrent() < resultPage.getPages());
        response.setSummary(summary);
        return response;
    }

    @Override
    public TradeDetailVO getTradeDetail(Long userId, Long itemId) {
        InventoryItem item = getOwnedSoldItem(userId, itemId);
        return toDetailVO(item);
    }

    @Override
    @Transactional
    public TradeIdVO createTrade(Long userId, TradeUpsertRequest request) {
        operationGuardService.assertMutationAllowed(userId, "createTrade", request.getRequestId());
        validateUpsertRequest(request);
        validateSellAfterBuy(request.getBuyTime(), request.getSellTime());

        LocalDateTime now = LocalDateTime.now();
        InventoryItem item = new InventoryItem();
        item.setUserId(userId);
        item.setItemName(normalizeRequiredText(request.getItemName()));
        item.setBuyPrice(request.getBuyPrice());
        item.setBuyTime(request.getBuyTime());
        item.setSellPrice(request.getSellPrice());
        item.setSellTime(request.getSellTime());
        item.setTradeTime(request.getSellTime());
        item.setChannel(request.getChannel());
        item.setCategory(request.getCategory());
        item.setStatus(STATUS_SOLD);
        item.setProfitAmount(request.getSellPrice().subtract(request.getBuyPrice()));
        item.setRemark(normalizeNullableText(request.getRemark()));
        item.setImageFileId(normalizeNullableText(request.getImageFileId()));
        item.setPublicPosted(Boolean.FALSE);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        inventoryItemMapper.insert(item);

        updateUserStatsAfterCreate(userId);
        return new TradeIdVO(item.getId());
    }

    @Override
    @Transactional
    public TradeIdVO updateTrade(Long userId, Long itemId, TradeUpsertRequest request) {
        operationGuardService.assertMutationAllowed(userId, "updateTrade", request.getRequestId());
        validateUpsertRequest(request);

        InventoryItem item = getOwnedSoldItem(userId, itemId);
        validateSellAfterBuy(request.getBuyTime(), request.getSellTime());

        BigDecimal oldBuyPrice = item.getBuyPrice();
        BigDecimal oldSellPrice = item.getSellPrice();

        item.setItemName(normalizeRequiredText(request.getItemName()));
        item.setBuyPrice(request.getBuyPrice());
        item.setBuyTime(request.getBuyTime());
        item.setSellPrice(request.getSellPrice());
        item.setSellTime(request.getSellTime());
        item.setTradeTime(request.getSellTime());
        item.setChannel(request.getChannel());
        item.setCategory(request.getCategory());
        item.setProfitAmount(request.getSellPrice().subtract(request.getBuyPrice()));
        item.setRemark(normalizeNullableText(request.getRemark()));
        item.setImageFileId(normalizeNullableText(request.getImageFileId()));
        item.setUpdatedAt(LocalDateTime.now());
        inventoryItemMapper.updateById(item);

        if (!oldBuyPrice.equals(request.getBuyPrice()) || !oldSellPrice.equals(request.getSellPrice())) {
            updateUserStatsAfterUpdate(userId);
        }
        return new TradeIdVO(item.getId());
    }

    @Override
    @Transactional
    public void deleteTrade(Long userId, Long itemId, String requestId) {
        operationGuardService.assertMutationAllowed(userId, "deleteTrade", requestId);
        InventoryItem item = getOwnedSoldItem(userId, itemId);
        cleanupLinkedPublicPosts(item);
        inventoryItemMapper.deleteById(item.getId());
        updateUserStatsAfterDelete(userId);
    }

    @Override
    @Transactional
    public TradeTogglePublicVO togglePublic(Long userId, Long itemId, TradeTogglePublicRequest request) {
        InventoryItem item = getOwnedSoldItem(userId, itemId);
        PublicSourceToggleVO result = publicPostService.toggleSourceItemPublicPost(
            userId,
            item.getId(),
            request.getPrice(),
            request.getTradeTime(),
            request.getDirection(),
            request.getRemark(),
            request.getImageFileId(),
            request.getRequestId()
        );
        return new TradeTogglePublicVO(result.getPublicPosted(), result.getPostId());
    }

    private void applyScopeFilter(LambdaQueryWrapper<InventoryItem> wrapper, String scope) {
        if (SCOPE_PROFIT.equals(scope)) {
            wrapper.apply("profit_amount > 0");
        } else if (SCOPE_LOSS.equals(scope)) {
            wrapper.apply("profit_amount < 0");
        }
    }

    private TradeSummaryVO calculateSummary(Long userId, String scope) {
        TradeSummaryVO summary = new TradeSummaryVO();
        summary.setTotalBuyAmount(sumByScope(userId, scope, "COALESCE(SUM(buy_price), 0)"));
        summary.setTotalSellAmount(sumByScope(userId, scope, "COALESCE(SUM(sell_price), 0)"));
        summary.setTotalProfit(sumByScope(userId, scope, "COALESCE(SUM(CASE WHEN profit_amount > 0 THEN profit_amount ELSE 0 END), 0)"));
        summary.setTotalLoss(sumByScope(userId, scope, "COALESCE(SUM(CASE WHEN profit_amount < 0 THEN ABS(profit_amount) ELSE 0 END), 0)"));
        return summary;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }

    private InventoryItem getOwnedSoldItem(Long userId, Long itemId) {
        InventoryItem item = inventoryItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Trade item not found");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(BusinessCode.FORBIDDEN, "You can only access your own trade item");
        }
        if (!STATUS_SOLD.equals(item.getStatus())) {
            throw new BusinessException(BusinessCode.STATE_INVALID, "Only sold items can be accessed here");
        }
        return item;
    }

    private void validateUpsertRequest(TradeUpsertRequest request) {
        validateDateNotFuture(request.getBuyTime(), "Buy time cannot be later than today");
        validateDateNotFuture(request.getSellTime(), "Sell time cannot be later than today");
        validateChannel(request.getChannel());
        validateCategory(request.getCategory());
    }

    private void validateSellAfterBuy(LocalDate buyTime, LocalDate sellTime) {
        if (sellTime.isBefore(buyTime)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Sell time cannot be earlier than buy time");
        }
    }

    private void validateDateNotFuture(LocalDate date, String message) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, message);
        }
    }

    private void validateChannel(String channel) {
        if (!ALLOWED_CHANNELS.contains(channel)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported channel");
        }
    }

    private void validateCategory(String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported category");
        }
    }

    private long normalizePageNo(Long pageNo) {
        if (pageNo == null) {
            return DEFAULT_PAGE_NO;
        }
        return Math.max(pageNo, DEFAULT_PAGE_NO);
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        long normalized = Math.max(pageSize, 1L);
        return Math.min(normalized, MAX_PAGE_SIZE);
    }

    private String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return SCOPE_ALL;
        }
        if (!SCOPE_ALL.equals(scope) && !SCOPE_PROFIT.equals(scope) && !SCOPE_LOSS.equals(scope)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported scope");
        }
        return scope;
    }

    private String normalizeRequiredText(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Required text field cannot be blank");
        }
        return normalized;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void cleanupLinkedPublicPosts(InventoryItem item) {
        List<PublicPost> linkedPosts = publicPostMapper.selectList(
            new LambdaQueryWrapper<PublicPost>()
                .eq(PublicPost::getSourceInventoryItemId, item.getId())
        );
        List<Long> postIds = new ArrayList<>(linkedPosts.stream().map(PublicPost::getId).toList());
        if (item.getPublicPostId() != null && !postIds.contains(item.getPublicPostId())) {
            postIds.add(item.getPublicPostId());
        }
        if (postIds.isEmpty()) {
            return;
        }
        publicPostFlagMapper.delete(new QueryWrapper<io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPostFlag>().in("post_id", postIds));
        publicPostMapper.delete(new QueryWrapper<PublicPost>().in("id", postIds));
    }

    private void updateUserStatsAfterCreate(Long userId) {
        recalculateUserStats(userId);
    }

    private void updateUserStatsAfterUpdate(Long userId) {
        recalculateUserStats(userId);
    }

    private void updateUserStatsAfterDelete(Long userId) {
        recalculateUserStats(userId);
    }

    private void recalculateUserStats(Long userId) {
        UserStats stats = userStatsMapper.selectById(userId);
        boolean isNew = (stats == null);
        if (isNew) {
            stats = new UserStats();
            stats.setUserId(userId);
            stats.setCreatedAt(LocalDateTime.now());
        }
        stats.setSoldBuyTotal(sumByStatus(userId, STATUS_SOLD, "COALESCE(SUM(buy_price), 0)"));
        stats.setSoldSellTotal(sumByStatus(userId, STATUS_SOLD, "COALESCE(SUM(sell_price), 0)"));
        stats.setTotalProfit(sumByStatus(userId, STATUS_SOLD, "COALESCE(SUM(CASE WHEN profit_amount > 0 THEN profit_amount ELSE 0 END), 0)"));
        stats.setTotalLoss(sumByStatus(userId, STATUS_SOLD, "COALESCE(SUM(CASE WHEN profit_amount < 0 THEN ABS(profit_amount) ELSE 0 END), 0)"));
        stats.setSoldCount(countByStatus(userId, STATUS_SOLD));
        stats.setUnsoldBuyTotal(sumByStatus(userId, "unsold", "COALESCE(SUM(buy_price), 0)"));
        stats.setUnsoldCount(countByStatus(userId, "unsold"));
        stats.setUpdatedAt(LocalDateTime.now());
        if (isNew) {
            userStatsMapper.insert(stats);
        } else {
            userStatsMapper.updateById(stats);
        }
    }

    private BigDecimal sumByScope(Long userId, String scope, String sqlExpr) {
        QueryWrapper<InventoryItem> wrapper = new QueryWrapper<>();
        wrapper.select(sqlExpr);
        wrapper.eq("user_id", userId).eq("status", STATUS_SOLD);
        if (SCOPE_PROFIT.equals(scope)) {
            wrapper.apply("profit_amount > 0");
        } else if (SCOPE_LOSS.equals(scope)) {
            wrapper.apply("profit_amount < 0");
        }
        List<Object> results = inventoryItemMapper.selectObjs(wrapper);
        return results.isEmpty() ? BigDecimal.ZERO : toBigDecimal(results.get(0));
    }

    private BigDecimal sumByStatus(Long userId, String status, String sqlExpr) {
        QueryWrapper<InventoryItem> wrapper = new QueryWrapper<>();
        wrapper.select(sqlExpr);
        wrapper.eq("user_id", userId).eq("status", status);
        List<Object> results = inventoryItemMapper.selectObjs(wrapper);
        return results.isEmpty() ? BigDecimal.ZERO : toBigDecimal(results.get(0));
    }

    private int countByStatus(Long userId, String status) {
        return Math.toIntExact(
            inventoryItemMapper.selectCount(
                new LambdaQueryWrapper<InventoryItem>()
                    .eq(InventoryItem::getUserId, userId)
                    .eq(InventoryItem::getStatus, status)
            )
        );
    }

    private TradeListItemVO toListItem(InventoryItem item) {
        TradeListItemVO vo = new TradeListItemVO();
        vo.setId(item.getId());
        vo.setItemName(item.getItemName());
        vo.setBuyPrice(item.getBuyPrice());
        vo.setSellPrice(item.getSellPrice());
        vo.setProfitAmount(item.getProfitAmount());
        vo.setBuyTime(item.getBuyTime());
        vo.setSellTime(item.getSellTime());
        vo.setChannel(item.getChannel());
        vo.setCategory(item.getCategory());
        vo.setRemark(item.getRemark());
        vo.setImageFileId(item.getImageFileId());
        vo.setPublicPosted(item.getPublicPosted());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }

    private TradeDetailVO toDetailVO(InventoryItem item) {
        TradeDetailVO vo = new TradeDetailVO();
        vo.setId(item.getId());
        vo.setItemName(item.getItemName());
        vo.setBuyPrice(item.getBuyPrice());
        vo.setSellPrice(item.getSellPrice());
        vo.setProfitAmount(item.getProfitAmount());
        vo.setBuyTime(item.getBuyTime());
        vo.setSellTime(item.getSellTime());
        vo.setTradeTime(item.getTradeTime());
        vo.setChannel(item.getChannel());
        vo.setCategory(item.getCategory());
        vo.setRemark(item.getRemark());
        vo.setImageFileId(item.getImageFileId());
        vo.setPublicPosted(item.getPublicPosted());
        vo.setPublicPostId(item.getPublicPostId());
        vo.setPublicPostedAt(item.getPublicPostedAt());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }
}
