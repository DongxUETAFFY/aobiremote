package io.github.dongxuetaffy.aobihelper.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.common.guard.service.OperationGuardService;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryBatchDeleteRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryBatchTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryMarkSoldRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryPageQuery;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryUpsertRequest;
import io.github.dongxuetaffy.aobihelper.inventory.entity.InventoryItem;
import io.github.dongxuetaffy.aobihelper.inventory.mapper.InventoryItemMapper;
import io.github.dongxuetaffy.aobihelper.inventory.service.InventoryItemService;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryDetailVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryItemIdVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryListItemVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryPageResponseVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventorySummaryVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryTogglePublicVO;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostFlagMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.service.PublicPostService;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicSourceToggleVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryItemServiceImpl extends ServiceImpl<InventoryItemMapper, InventoryItem> implements InventoryItemService {
    private static final String STATUS_UNSOLD = "unsold";
    private static final String STATUS_SOLD = "sold";
    private static final String CATEGORY_MAGIC = "magic";
    private static final String CATEGORY_OBI = "obi";
    private static final Set<String> ALLOWED_CHANNELS = Set.of("xianyu", "tieba", "other");
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("magic", "obi");
    private static final Set<String> ALLOWED_SORT_TYPES = Set.of("buyTimeDesc", "buyPriceDesc");
    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 50L;

    private final PublicPostMapper publicPostMapper;
    private final PublicPostFlagMapper publicPostFlagMapper;
    private final PublicPostService publicPostService;
    private final OperationGuardService operationGuardService;

    public InventoryItemServiceImpl(
        PublicPostMapper publicPostMapper,
        PublicPostFlagMapper publicPostFlagMapper,
        PublicPostService publicPostService,
        OperationGuardService operationGuardService
    ) {
        this.publicPostMapper = publicPostMapper;
        this.publicPostFlagMapper = publicPostFlagMapper;
        this.publicPostService = publicPostService;
        this.operationGuardService = operationGuardService;
    }

    @Override
    public InventoryPageResponseVO pageWarehouseItems(Long userId, InventoryPageQuery query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        PriceRange priceRange = parsePriceRange(query.getPriceRange());
        String sortType = normalizeSortType(query.getSortType());
        String keyword = normalizeNullableText(query.getKeyword());
        String category = normalizeCategoryFilter(query.getCategory());

        Page<InventoryItem> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<InventoryItem> pageWrapper = new LambdaQueryWrapper<InventoryItem>()
            .eq(InventoryItem::getUserId, userId)
            .eq(InventoryItem::getStatus, STATUS_UNSOLD);
        applyWarehouseFilters(pageWrapper, keyword, category, priceRange, sortType);

        Page<InventoryItem> resultPage = baseMapper.selectPage(page, pageWrapper);

        InventorySummaryVO summary = calculateSummary(userId, keyword, priceRange, category);

        InventoryPageResponseVO response = new InventoryPageResponseVO();
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
    public InventoryDetailVO getInventoryItemDetail(Long userId, Long itemId) {
        return toDetailVO(getOwnedInventoryItem(userId, itemId));
    }

    @Override
    @Transactional
    public InventoryItemIdVO createWarehouseItem(Long userId, InventoryUpsertRequest request) {
        validateUpsertRequest(request);
        operationGuardService.assertMutationAllowed(userId, "createInventoryItem", request.getRequestId());

        LocalDateTime now = LocalDateTime.now();
        InventoryItem item = new InventoryItem();
        item.setUserId(userId);
        item.setItemName(normalizeRequiredText(request.getItemName()));
        item.setBuyPrice(request.getBuyPrice());
        item.setBuyTime(request.getBuyTime());
        item.setChannel(request.getChannel());
        item.setCategory(request.getCategory());
        item.setStatus(STATUS_UNSOLD);
        item.setRemark(normalizeNullableText(request.getRemark()));
        item.setImageFileId(normalizeNullableText(request.getImageFileId()));
        item.setPublicPosted(Boolean.FALSE);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        baseMapper.insert(item);
        return new InventoryItemIdVO(item.getId());
    }

    @Override
    @Transactional
    public InventoryItemIdVO updateWarehouseItem(Long userId, Long itemId, InventoryUpsertRequest request) {
        validateUpsertRequest(request);
        operationGuardService.assertMutationAllowed(userId, "updateInventoryItem", request.getRequestId());

        InventoryItem item = getOwnedInventoryItem(userId, itemId);
        ensureUnsold(item, "Only unsold inventory items can be edited here");

        item.setItemName(normalizeRequiredText(request.getItemName()));
        item.setBuyPrice(request.getBuyPrice());
        item.setBuyTime(request.getBuyTime());
        item.setChannel(request.getChannel());
        item.setCategory(request.getCategory());
        item.setRemark(normalizeNullableText(request.getRemark()));
        item.setImageFileId(normalizeNullableText(request.getImageFileId()));
        item.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(item);
        return new InventoryItemIdVO(item.getId());
    }

    @Override
    @Transactional
    public InventoryItemIdVO markItemSold(Long userId, Long itemId, InventoryMarkSoldRequest request) {
        validateMarkSoldRequest(request);
        operationGuardService.assertMutationAllowed(userId, "markInventoryItemSold", request.getRequestId());

        InventoryItem item = getOwnedInventoryItem(userId, itemId);
        ensureUnsold(item, "Only unsold inventory items can be marked as sold");
        if (request.getSellTime().isBefore(item.getBuyTime())) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Sell time cannot be earlier than buy time");
        }

        item.setSellPrice(request.getSellPrice());
        item.setSellTime(request.getSellTime());
        item.setTradeTime(request.getSellTime());
        item.setStatus(STATUS_SOLD);
        item.setProfitAmount(request.getSellPrice().subtract(item.getBuyPrice()));
        item.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(item);
        return new InventoryItemIdVO(item.getId());
    }

    @Override
    @Transactional
    public InventoryTogglePublicVO togglePublic(Long userId, Long itemId, InventoryTogglePublicRequest request) {
        InventoryItem item = getOwnedInventoryItem(userId, itemId);
        ensureUnsold(item, "Only unsold inventory items can be toggled here");

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
        return new InventoryTogglePublicVO(result.getPublicPosted(), result.getPostId());
    }

    @Override
    @Transactional
    public void deleteInventoryItem(Long userId, Long itemId, String requestId) {
        operationGuardService.assertMutationAllowed(userId, "deleteInventoryItem", requestId);
        InventoryItem item = getOwnedInventoryItem(userId, itemId);
        cleanupLinkedPublicPosts(item);
        baseMapper.deleteById(item.getId());
    }

    @Override
    @Transactional
    public void batchDeleteInventoryItems(Long userId, InventoryBatchDeleteRequest request) {
        List<Long> itemIds = normalizeIds(request.getIds());
        for (int index = 0; index < itemIds.size(); index++) {
            deleteInventoryItem(userId, itemIds.get(index), buildBatchRequestId(request.getRequestId(), "batchDeleteInventory", itemIds.get(index), index));
        }
    }

    @Override
    @Transactional
    public void batchTogglePublic(Long userId, InventoryBatchTogglePublicRequest request) {
        List<Long> itemIds = normalizeIds(request.getIds());
        for (int index = 0; index < itemIds.size(); index++) {
            Long itemId = itemIds.get(index);
            InventoryItem item = getOwnedInventoryItem(userId, itemId);
            ensureUnsold(item, "Only unsold inventory items can be toggled here");
            publicPostService.toggleSourceItemPublicPost(
                userId,
                item.getId(),
                item.getBuyPrice(),
                item.getBuyTime(),
                "sell",
                item.getRemark(),
                item.getImageFileId(),
                buildBatchRequestId(request.getRequestId(), "batchToggleInventoryPublic", itemId, index)
            );
        }
    }

    private void applyWarehouseFilters(
        LambdaQueryWrapper<InventoryItem> queryWrapper,
        String keyword,
        String category,
        PriceRange priceRange,
        String sortType
    ) {
        if (keyword != null) {
            queryWrapper.like(InventoryItem::getItemName, keyword);
        }
        if (category != null) {
            queryWrapper.eq(InventoryItem::getCategory, category);
        }
        if (priceRange != null) {
            if (priceRange.minPrice() != null) {
                queryWrapper.ge(InventoryItem::getBuyPrice, priceRange.minPrice());
            }
            if (priceRange.maxPrice() != null) {
                queryWrapper.le(InventoryItem::getBuyPrice, priceRange.maxPrice());
            }
        }
        if ("buyPriceDesc".equals(sortType)) {
            queryWrapper.orderByDesc(InventoryItem::getBuyPrice).orderByDesc(InventoryItem::getCreatedAt);
            return;
        }
        queryWrapper.orderByDesc(InventoryItem::getBuyTime).orderByDesc(InventoryItem::getCreatedAt);
    }

    private InventorySummaryVO calculateSummary(Long userId, String keyword, PriceRange priceRange, String category) {
        InventorySummaryVO summary = new InventorySummaryVO();
        // 总买入价为全量不过滤分类
        summary.setTotalBuyPrice(sumWarehouseBuyPrice(userId, keyword, priceRange, null));
        // obi/magic 分类小计始终是全量分类值
        summary.setObiCount(countWarehouseItems(userId, keyword, priceRange, CATEGORY_OBI));
        summary.setObiBuyPrice(sumWarehouseBuyPrice(userId, keyword, priceRange, CATEGORY_OBI));
        summary.setMagicCount(countWarehouseItems(userId, keyword, priceRange, CATEGORY_MAGIC));
        summary.setMagicBuyPrice(sumWarehouseBuyPrice(userId, keyword, priceRange, CATEGORY_MAGIC));
        return summary;
    }

    private BigDecimal sumWarehouseBuyPrice(Long userId, String keyword, PriceRange priceRange, String category) {
        QueryWrapper<InventoryItem> summaryWrapper = new QueryWrapper<>();
        summaryWrapper.select("COALESCE(SUM(buy_price), 0)");
        summaryWrapper.eq("user_id", userId).eq("status", STATUS_UNSOLD);
        if (keyword != null) {
            summaryWrapper.like("item_name", keyword);
        }
        if (category != null) {
            summaryWrapper.eq("category", category);
        }
        if (priceRange != null) {
            if (priceRange.minPrice() != null) {
                summaryWrapper.ge("buy_price", priceRange.minPrice());
            }
            if (priceRange.maxPrice() != null) {
                summaryWrapper.le("buy_price", priceRange.maxPrice());
            }
        }
        List<Object> results = baseMapper.selectObjs(summaryWrapper);
        if (results.isEmpty() || results.get(0) == null) {
            return BigDecimal.ZERO;
        }
        Object raw = results.get(0);
        if (raw instanceof BigDecimal value) {
            return value;
        }
        return new BigDecimal(raw.toString());
    }

    private int countWarehouseItems(Long userId, String keyword, PriceRange priceRange, String category) {
        QueryWrapper<InventoryItem> countWrapper = new QueryWrapper<>();
        countWrapper.eq("user_id", userId).eq("status", STATUS_UNSOLD).eq("category", category);
        if (keyword != null) {
            countWrapper.like("item_name", keyword);
        }
        if (priceRange != null) {
            if (priceRange.minPrice() != null) {
                countWrapper.ge("buy_price", priceRange.minPrice());
            }
            if (priceRange.maxPrice() != null) {
                countWrapper.le("buy_price", priceRange.maxPrice());
            }
        }
        return Math.toIntExact(baseMapper.selectCount(countWrapper));
    }

    private InventoryItem getOwnedInventoryItem(Long userId, Long itemId) {
        InventoryItem item = baseMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Inventory item not found");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(BusinessCode.FORBIDDEN, "You can only access your own inventory item");
        }
        return item;
    }

    private void cleanupLinkedPublicPosts(InventoryItem item) {
        List<PublicPost> linkedPosts = publicPostMapper.selectList(
            new LambdaQueryWrapper<PublicPost>()
                .eq(PublicPost::getSourceInventoryItemId, item.getId())
        );

        List<Long> postIds = new ArrayList<>(linkedPosts.stream().map(PublicPost::getId).collect(Collectors.toList()));
        if (item.getPublicPostId() != null && !postIds.contains(item.getPublicPostId())) {
            postIds.add(item.getPublicPostId());
        }
        if (postIds.isEmpty()) {
            return;
        }

        publicPostFlagMapper.delete(new QueryWrapper<io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPostFlag>().in("post_id", postIds));
        publicPostMapper.delete(new QueryWrapper<PublicPost>().in("id", postIds));
    }

    private void validateUpsertRequest(InventoryUpsertRequest request) {
        validateDateNotFuture(request.getBuyTime(), "Buy time cannot be later than today");
        validateChannel(request.getChannel());
        validateCategory(request.getCategory());
    }

    private void validateMarkSoldRequest(InventoryMarkSoldRequest request) {
        validateDateNotFuture(request.getSellTime(), "Sell time cannot be later than today");
        if (request.getSellPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Sell price must be greater than 0");
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

    private String normalizeSortType(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return "buyTimeDesc";
        }
        if (!ALLOWED_SORT_TYPES.contains(sortType)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported sort type");
        }
        return sortType;
    }

    private String normalizeCategoryFilter(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported category");
        }
        return category;
    }

    private PriceRange parsePriceRange(String priceRange) {
        if (priceRange == null || priceRange.isBlank()) {
            return null;
        }
        String[] parts = priceRange.trim().split("-", -1);
        if (parts.length != 2) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "priceRange format is invalid");
        }

        BigDecimal minPrice = parsePriceBoundary(parts[0]);
        BigDecimal maxPrice = parsePriceBoundary(parts[1]);
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "priceRange min cannot be greater than max");
        }
        return new PriceRange(minPrice, maxPrice);
    }

    private BigDecimal parsePriceBoundary(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            BigDecimal boundary = new BigDecimal(normalized);
            if (boundary.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(BusinessCode.PARAM_INVALID, "priceRange cannot contain negative values");
            }
            return boundary;
        } catch (NumberFormatException exception) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "priceRange format is invalid");
        }
    }

    private void ensureUnsold(InventoryItem item, String message) {
        if (!STATUS_UNSOLD.equals(item.getStatus())) {
            throw new BusinessException(BusinessCode.STATE_INVALID, message);
        }
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

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "ids cannot be empty");
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    private String buildBatchRequestId(String baseRequestId, String action, Long itemId, int index) {
        String prefix = normalizeNullableText(baseRequestId);
        if (prefix == null) {
            prefix = action + "-" + System.currentTimeMillis();
        }
        return prefix + "-" + index + "-" + itemId;
    }

    private InventoryListItemVO toListItem(InventoryItem item) {
        InventoryListItemVO vo = new InventoryListItemVO();
        vo.setId(item.getId());
        vo.setItemName(item.getItemName());
        vo.setBuyPrice(item.getBuyPrice());
        vo.setBuyTime(item.getBuyTime());
        vo.setChannel(item.getChannel());
        vo.setCategory(item.getCategory());
        vo.setRemark(item.getRemark());
        vo.setImageFileId(item.getImageFileId());
        vo.setStatus(item.getStatus());
        vo.setPublicPosted(Boolean.TRUE.equals(item.getPublicPosted()));
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }

    private InventoryDetailVO toDetailVO(InventoryItem item) {
        InventoryDetailVO vo = new InventoryDetailVO();
        vo.setId(item.getId());
        vo.setItemName(item.getItemName());
        vo.setBuyPrice(item.getBuyPrice());
        vo.setBuyTime(item.getBuyTime());
        vo.setSellPrice(item.getSellPrice());
        vo.setSellTime(item.getSellTime());
        vo.setTradeTime(item.getTradeTime());
        vo.setChannel(item.getChannel());
        vo.setCategory(item.getCategory());
        vo.setStatus(item.getStatus());
        vo.setProfitAmount(item.getProfitAmount());
        vo.setPublicPosted(Boolean.TRUE.equals(item.getPublicPosted()));
        vo.setPublicPostId(item.getPublicPostId());
        vo.setPublicPostedAt(item.getPublicPostedAt());
        vo.setRemark(item.getRemark());
        vo.setImageFileId(item.getImageFileId());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }

    private record PriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
    }
}
