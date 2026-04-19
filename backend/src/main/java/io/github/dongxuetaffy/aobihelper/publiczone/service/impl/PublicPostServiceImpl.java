package io.github.dongxuetaffy.aobihelper.publiczone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.common.guard.service.OperationGuardService;
import io.github.dongxuetaffy.aobihelper.file.entity.FileAsset;
import io.github.dongxuetaffy.aobihelper.file.mapper.FileAssetMapper;
import io.github.dongxuetaffy.aobihelper.inventory.entity.InventoryItem;
import io.github.dongxuetaffy.aobihelper.inventory.mapper.InventoryItemMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostCreateRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostPageQuery;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostToggleUntrustedRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostUpdateRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.SourceItemPublicToggleCommand;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPostFlag;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostFlagMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.service.PublicPostService;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostDetailVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostIdVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostListItemVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostPageResponseVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicSourceToggleVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostToggleUntrustedVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicPostServiceImpl extends ServiceImpl<PublicPostMapper, PublicPost> implements PublicPostService {
    private static final String SCOPE_ALL = "all";
    private static final String SCOPE_MINE = "mine";
    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("buy", "sell");
    private static final Set<String> ALLOWED_CHANNELS = Set.of("xianyu", "tieba", "other");
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("magic", "obi");
    private static final Set<String> ALLOWED_SORT_TYPES = Set.of("tradeTimeDesc", "priceDesc");
    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 30L;
    private static final long MAX_PAGE_SIZE = 30L;

    private final UserAccountMapper userAccountMapper;
    private final PublicPostFlagMapper publicPostFlagMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final FileAssetMapper fileAssetMapper;
    private final OperationGuardService operationGuardService;

    public PublicPostServiceImpl(
        UserAccountMapper userAccountMapper,
        PublicPostFlagMapper publicPostFlagMapper,
        InventoryItemMapper inventoryItemMapper,
        FileAssetMapper fileAssetMapper,
        OperationGuardService operationGuardService
    ) {
        this.userAccountMapper = userAccountMapper;
        this.publicPostFlagMapper = publicPostFlagMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.operationGuardService = operationGuardService;
    }

    @Override
    public PublicPostPageResponseVO pagePublicPosts(Long currentUserId, PublicPostPageQuery query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        String scope = normalizeScope(query.getScope(), currentUserId);
        String direction = normalizeOptionalDirection(query.getDirection());
        String category = normalizeCategory(query.getCategory());
        String channel = normalizeChannel(query.getChannel());
        String keyword = normalizeNullableText(query.getKeyword());
        String sortType = normalizeSortType(query.getSortType());
        PriceRange priceRange = resolvePriceRange(query.getPriceRange(), query.getMinPrice(), query.getMaxPrice());

        Page<PublicPost> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<PublicPost> wrapper = new LambdaQueryWrapper<>();

        if (SCOPE_MINE.equals(scope)) {
            wrapper.eq(PublicPost::getUserId, currentUserId);
        }
        if (direction != null) {
            wrapper.eq(PublicPost::getDirection, direction);
        }
        if (keyword != null) {
            wrapper.like(PublicPost::getItemName, keyword);
        }
        if (category != null) {
            wrapper.eq(PublicPost::getCategory, category);
        }
        if (channel != null) {
            wrapper.eq(PublicPost::getChannel, channel);
        }
        if (priceRange != null) {
            if (priceRange.minPrice() != null) {
                wrapper.ge(PublicPost::getPrice, priceRange.minPrice());
            }
            if (priceRange.maxPrice() != null) {
                wrapper.le(PublicPost::getPrice, priceRange.maxPrice());
            }
        }
        if ("priceDesc".equals(sortType)) {
            wrapper.orderByDesc(PublicPost::getPrice).orderByDesc(PublicPost::getCreatedAt);
        } else {
            wrapper.orderByDesc(PublicPost::getTradeTime).orderByDesc(PublicPost::getCreatedAt);
        }

        Page<PublicPost> resultPage = baseMapper.selectPage(page, wrapper);
        List<PublicPost> posts = resultPage.getRecords();
        Set<Long> flaggedPostIds = getFlaggedPostIds(currentUserId, posts.stream().map(PublicPost::getId).toList());

        PublicPostPageResponseVO response = new PublicPostPageResponseVO();
        response.setItems(posts.stream().map(post -> toListItem(post, currentUserId, flaggedPostIds.contains(post.getId()))).toList());
        response.setPageNo(resultPage.getCurrent());
        response.setPageSize(resultPage.getSize());
        response.setTotalCount(resultPage.getTotal());
        response.setTotalPages(resultPage.getPages());
        response.setHasMore(resultPage.getCurrent() < resultPage.getPages());
        return response;
    }

    @Override
    public PublicPostDetailVO getPublicPostDetail(Long currentUserId, Long postId) {
        PublicPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Public post not found");
        }
        return toDetailVO(post, currentUserId, hasFlaggedPost(currentUserId, postId));
    }

    @Override
    @Transactional
    public PublicPostIdVO createPublicPost(Long currentUserId, PublicPostCreateRequest request) {
        operationGuardService.assertMutationAllowed(currentUserId, "createPublicPost", request.getRequestId());
        validatePostFields(
            request.getTradeTime(),
            request.getDirection(),
            request.getChannel(),
            request.getCategory()
        );

        UserAccount user = userAccountMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "User not found");
        }

        LocalDateTime now = LocalDateTime.now();
        PublicPost post = new PublicPost();
        post.setUserId(currentUserId);
        post.setSourceInventoryItemId(null);
        post.setItemName(normalizeRequiredText(request.getItemName()));
        post.setPrice(request.getPrice());
        post.setTradeTime(request.getTradeTime());
        post.setDirection(request.getDirection());
        post.setChannel(request.getChannel());
        post.setCategory(request.getCategory());
        post.setRemark(normalizeNullableText(request.getRemark()));
        post.setImageFileId(preparePublicImageFile(currentUserId, request.getImageFileId()));
        post.setUntrustedCount(0);
        post.setPublisherName(resolvePublisherName(user));
        post.setPublisherAvatar(normalizeNullableText(user.getAvatarUrl()));
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        baseMapper.insert(post);
        return new PublicPostIdVO(post.getId());
    }

    @Override
    @Transactional
    public PublicPostIdVO updatePublicPost(Long currentUserId, Long postId, PublicPostUpdateRequest request) {
        operationGuardService.assertMutationAllowed(currentUserId, "updatePublicPost", request.getRequestId());
        validatePostFields(
            request.getTradeTime(),
            request.getDirection(),
            request.getChannel(),
            request.getCategory()
        );

        PublicPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Public post not found");
        }
        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException(BusinessCode.FORBIDDEN, "You can only update your own public post");
        }

        post.setItemName(normalizeRequiredText(request.getItemName()));
        post.setPrice(request.getPrice());
        post.setTradeTime(request.getTradeTime());
        post.setDirection(request.getDirection());
        post.setChannel(request.getChannel());
        post.setCategory(request.getCategory());
        post.setRemark(normalizeNullableText(request.getRemark()));
        post.setImageFileId(preparePublicImageFile(currentUserId, request.getImageFileId()));
        post.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(post);
        return new PublicPostIdVO(post.getId());
    }

    @Override
    @Transactional
    public PublicPostToggleUntrustedVO togglePublicPostUntrusted(
        Long currentUserId,
        Long postId,
        PublicPostToggleUntrustedRequest request
    ) {
        operationGuardService.assertMutationAllowed(currentUserId, "togglePostUntrusted", request.getRequestId());
        PublicPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Public post not found");
        }

        PublicPostFlag existingFlag = publicPostFlagMapper.selectOne(
            new LambdaQueryWrapper<PublicPostFlag>()
                .eq(PublicPostFlag::getUserId, currentUserId)
                .eq(PublicPostFlag::getPostId, postId)
                .last("limit 1")
        );

        boolean flagged;
        LocalDateTime now = LocalDateTime.now();

        if (existingFlag != null) {
            publicPostFlagMapper.deleteById(existingFlag.getId());
            flagged = false;
            int nextCount = Math.max((post.getUntrustedCount() == null ? 0 : post.getUntrustedCount()) - 1, 0);
            post.setUntrustedCount(nextCount);
            post.setUpdatedAt(now);
            baseMapper.updateById(post);
            return new PublicPostToggleUntrustedVO(postId, false, nextCount);
        }

        PublicPostFlag newFlag = new PublicPostFlag();
        newFlag.setUserId(currentUserId);
        newFlag.setPostId(postId);
        newFlag.setCreatedAt(now);
        publicPostFlagMapper.insert(newFlag);

        flagged = true;
        int nextCount = (post.getUntrustedCount() == null ? 0 : post.getUntrustedCount()) + 1;
        post.setUntrustedCount(nextCount);
        post.setUpdatedAt(now);
        baseMapper.updateById(post);
        return new PublicPostToggleUntrustedVO(postId, flagged, nextCount);
    }

    @Override
    @Transactional
    public PublicSourceToggleVO toggleSourceItemPublicPost(
        Long currentUserId,
        Long sourceItemId,
        BigDecimal price,
        LocalDate tradeTime,
        String direction,
        String remark,
        String imageFileId,
        String requestId
    ) {
        operationGuardService.assertMutationAllowed(currentUserId, "toggleSourceItemPublicPost", requestId);

        InventoryItem item = inventoryItemMapper.selectById(sourceItemId);
        if (item == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Source item not found");
        }
        if (!item.getUserId().equals(currentUserId)) {
            throw new BusinessException(BusinessCode.FORBIDDEN, "You can only operate your own source item");
        }

        PublicPost linkedPost = findOwnedLinkedSourcePost(currentUserId, item);
        if (Boolean.TRUE.equals(item.getPublicPosted()) || linkedPost != null) {
            if (linkedPost != null) {
                publicPostFlagMapper.delete(new QueryWrapper<PublicPostFlag>().eq("post_id", linkedPost.getId()));
                baseMapper.deleteById(linkedPost.getId());
            }
            clearSourceItemPublicState(item);
            return new PublicSourceToggleVO(Boolean.FALSE, null);
        }

        validatePostFields(tradeTime, direction, item.getChannel(), item.getCategory());

        UserAccount user = userAccountMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "User not found");
        }

        LocalDateTime now = LocalDateTime.now();
        PublicPost post = new PublicPost();
        post.setUserId(currentUserId);
        post.setSourceInventoryItemId(item.getId());
        post.setItemName(item.getItemName());
        post.setPrice(price);
        post.setTradeTime(tradeTime);
        post.setDirection(direction);
        post.setChannel(item.getChannel());
        post.setCategory(item.getCategory());
        post.setRemark(normalizeNullableText(remark));
        post.setImageFileId(preparePublicImageFile(currentUserId, resolveSourceImageFileId(item, imageFileId)));
        post.setUntrustedCount(0);
        post.setPublisherName(resolvePublisherName(user));
        post.setPublisherAvatar(normalizeNullableText(user.getAvatarUrl()));
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        baseMapper.insert(post);

        item.setPublicPosted(Boolean.TRUE);
        item.setPublicPostId(post.getId());
        item.setPublicPostedAt(now);
        item.setUpdatedAt(now);
        inventoryItemMapper.updateById(item);
        return new PublicSourceToggleVO(Boolean.TRUE, post.getId());
    }

    @Override
    @Transactional
    public void batchToggleSourceItemPublicPosts(
        Long currentUserId,
        List<SourceItemPublicToggleCommand> commands,
        String requestId
    ) {
        operationGuardService.assertMutationAllowed(currentUserId, "toggleSourceItemPublicPost", requestId);
        if (commands.isEmpty()) {
            return;
        }

        List<Long> sourceItemIds = commands.stream().map(SourceItemPublicToggleCommand::sourceItemId).toList();
        Map<Long, InventoryItem> itemById = getSourceItemsById(sourceItemIds);
        List<InventoryItem> orderedItems = validateSourceItems(currentUserId, commands, itemById);
        Map<Long, PublicPost> linkedPostByItemId = getOwnedLinkedSourcePosts(currentUserId, orderedItems);

        UserAccount user = null;
        List<Long> postIdsToDelete = new ArrayList<>();
        for (int index = 0; index < commands.size(); index++) {
            SourceItemPublicToggleCommand command = commands.get(index);
            InventoryItem item = orderedItems.get(index);
            PublicPost linkedPost = linkedPostByItemId.get(item.getId());
            if (Boolean.TRUE.equals(item.getPublicPosted()) || linkedPost != null) {
                if (linkedPost != null) {
                    postIdsToDelete.add(linkedPost.getId());
                }
                clearSourceItemPublicState(item);
                continue;
            }

            validatePostFields(command.tradeTime(), command.direction(), item.getChannel(), item.getCategory());
            if (user == null) {
                user = getRequiredUser(currentUserId);
            }
            createSourcePublicPost(currentUserId, item, command, user);
        }

        deleteLinkedPosts(postIdsToDelete);
    }

    @Override
    @Transactional
    public void deletePublicPost(Long currentUserId, Long postId, String requestId) {
        operationGuardService.assertMutationAllowed(currentUserId, "deletePublicPost", requestId);
        PublicPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Public post not found");
        }
        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException(BusinessCode.FORBIDDEN, "You can only delete your own public post");
        }

        publicPostFlagMapper.delete(new QueryWrapper<PublicPostFlag>().eq("post_id", postId));
        baseMapper.deleteById(postId);
        rollbackSourceItemPublicState(post, currentUserId);
    }

    private PublicPostListItemVO toListItem(PublicPost post, Long currentUserId, boolean flagged) {
        PublicPostListItemVO vo = new PublicPostListItemVO();
        vo.setId(post.getId());
        vo.setItemName(post.getItemName());
        vo.setPrice(post.getPrice());
        vo.setTradeTime(post.getTradeTime());
        vo.setDirection(post.getDirection());
        vo.setChannel(post.getChannel());
        vo.setCategory(post.getCategory());
        vo.setRemark(post.getRemark());
        vo.setImageFileId(post.getImageFileId());
        vo.setUntrustedCount(post.getUntrustedCount());
        vo.setUntrusted(flagged);
        vo.setPublisherName(post.getPublisherName());
        vo.setPublisherAvatar(post.getPublisherAvatar());
        vo.setMine(currentUserId != null && currentUserId.equals(post.getUserId()));
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        return vo;
    }

    private PublicPostDetailVO toDetailVO(PublicPost post, Long currentUserId, boolean flagged) {
        PublicPostDetailVO vo = new PublicPostDetailVO();
        vo.setId(post.getId());
        vo.setSourceInventoryItemId(post.getSourceInventoryItemId());
        vo.setItemName(post.getItemName());
        vo.setPrice(post.getPrice());
        vo.setTradeTime(post.getTradeTime());
        vo.setDirection(post.getDirection());
        vo.setChannel(post.getChannel());
        vo.setCategory(post.getCategory());
        vo.setRemark(post.getRemark());
        vo.setImageFileId(post.getImageFileId());
        vo.setUntrustedCount(post.getUntrustedCount());
        vo.setUntrusted(flagged);
        vo.setPublisherName(post.getPublisherName());
        vo.setPublisherAvatar(post.getPublisherAvatar());
        vo.setMine(currentUserId != null && currentUserId.equals(post.getUserId()));
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        return vo;
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

    private String normalizeScope(String scope, Long currentUserId) {
        if (scope == null || scope.isBlank()) {
            return SCOPE_ALL;
        }
        if (!SCOPE_ALL.equals(scope) && !SCOPE_MINE.equals(scope)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported scope");
        }
        if (SCOPE_MINE.equals(scope) && currentUserId == null) {
            throw new BusinessException(BusinessCode.UNAUTHORIZED, "Login required for mine scope");
        }
        return scope;
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported category");
        }
        return category;
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return null;
        }
        if (!ALLOWED_CHANNELS.contains(channel)) {
            // Invalid channel value: treat as no filter instead of throwing
            return null;
        }
        return channel;
    }

    private String normalizeOptionalDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return null;
        }
        if (!ALLOWED_DIRECTIONS.contains(direction)) {
            // Invalid direction value: treat as no filter instead of throwing
            return null;
        }
        return direction;
    }

    private void validatePostFields(LocalDate tradeTime, String direction, String channel, String category) {
        validateDateNotFuture(tradeTime, "Trade time cannot be later than today");
        validateDirection(direction);
        validateChannel(channel);
        validateRequiredCategory(category);
    }

    private void validateDateNotFuture(LocalDate date, String message) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, message);
        }
    }

    private void validateDirection(String direction) {
        if (!ALLOWED_DIRECTIONS.contains(direction)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported direction");
        }
    }

    private void validateChannel(String channel) {
        if (!ALLOWED_CHANNELS.contains(channel)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported channel");
        }
    }

    private void validateRequiredCategory(String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported category");
        }
    }

    private String normalizeSortType(String sortType) {
        if (sortType == null || sortType.isBlank()) {
            return "tradeTimeDesc";
        }
        if (!ALLOWED_SORT_TYPES.contains(sortType)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported sort type");
        }
        return sortType;
    }

    private PriceRange resolvePriceRange(String priceRange, String minPrice, String maxPrice) {
        if (minPrice != null || maxPrice != null) {
            BigDecimal min = parsePriceBoundary(minPrice);
            BigDecimal max = parsePriceBoundary(maxPrice);
            validatePriceRange(min, max);
            return new PriceRange(min, max);
        }
        return parsePresetPriceRange(priceRange);
    }

    private PriceRange parsePresetPriceRange(String priceRange) {
        if (priceRange == null || priceRange.isBlank()) {
            return null;
        }
        String[] parts = priceRange.trim().split("-", -1);
        if (parts.length != 2) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "priceRange format is invalid");
        }
        BigDecimal min = parsePriceBoundary(parts[0]);
        BigDecimal max = parsePriceBoundary(parts[1]);
        validatePriceRange(min, max);
        return new PriceRange(min, max);
    }

    private BigDecimal parsePriceBoundary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            BigDecimal price = new BigDecimal(value.trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(BusinessCode.PARAM_INVALID, "Price cannot be negative");
            }
            return price;
        } catch (NumberFormatException exception) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Price format is invalid");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Minimum price cannot be greater than maximum price");
        }
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String resolveSourceImageFileId(InventoryItem item, String imageFileId) {
        String normalizedImageFileId = normalizeNullableText(imageFileId);
        if (normalizedImageFileId != null) {
            return normalizedImageFileId;
        }
        return normalizeNullableText(item.getImageFileId());
    }

    private String preparePublicImageFile(Long currentUserId, String imageFileId) {
        String normalizedImageFileId = normalizeNullableText(imageFileId);
        if (normalizedImageFileId == null) {
            return null;
        }

        Long parsedImageFileId = parseImageFileId(normalizedImageFileId);
        FileAsset fileAsset = fileAssetMapper.selectById(parsedImageFileId);
        if (fileAsset == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Image not found");
        }
        if (!fileAsset.getUserId().equals(currentUserId)) {
            throw new BusinessException(BusinessCode.FORBIDDEN, "You can only publish your own image");
        }

        if (!Boolean.TRUE.equals(fileAsset.getIsPublic())) {
            fileAsset.setIsPublic(Boolean.TRUE);
            fileAsset.setUpdatedAt(LocalDateTime.now());
            fileAssetMapper.updateById(fileAsset);
        }
        return normalizedImageFileId;
    }

    private Long parseImageFileId(String imageFileId) {
        try {
            return Long.parseLong(imageFileId);
        } catch (NumberFormatException exception) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Invalid image file id");
        }
    }

    private String normalizeRequiredText(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Required text field cannot be blank");
        }
        return normalized;
    }

    private String resolvePublisherName(UserAccount user) {
        String nickname = normalizeNullableText(user.getNickname());
        if (nickname != null) {
            return nickname;
        }
        return user.getEmail();
    }

    private UserAccount getRequiredUser(Long currentUserId) {
        UserAccount user = userAccountMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "User not found");
        }
        return user;
    }

    private Map<Long, InventoryItem> getSourceItemsById(List<Long> sourceItemIds) {
        List<InventoryItem> items = inventoryItemMapper.selectList(
            new LambdaQueryWrapper<InventoryItem>().in(InventoryItem::getId, sourceItemIds)
        );
        Map<Long, InventoryItem> itemById = new HashMap<>();
        for (InventoryItem item : items) {
            itemById.put(item.getId(), item);
        }
        return itemById;
    }

    private List<InventoryItem> validateSourceItems(
        Long currentUserId,
        List<SourceItemPublicToggleCommand> commands,
        Map<Long, InventoryItem> itemById
    ) {
        List<InventoryItem> orderedItems = new ArrayList<>(commands.size());
        for (SourceItemPublicToggleCommand command : commands) {
            InventoryItem item = itemById.get(command.sourceItemId());
            if (item == null) {
                throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Source item not found");
            }
            if (!item.getUserId().equals(currentUserId)) {
                throw new BusinessException(BusinessCode.FORBIDDEN, "You can only operate your own source item");
            }
            orderedItems.add(item);
        }
        return orderedItems;
    }

    private Map<Long, PublicPost> getOwnedLinkedSourcePosts(Long currentUserId, List<InventoryItem> items) {
        List<Long> sourceItemIds = items.stream().map(InventoryItem::getId).toList();
        List<Long> publicPostIds = items.stream()
            .map(InventoryItem::getPublicPostId)
            .filter(id -> id != null)
            .toList();

        Map<Long, PublicPost> linkedPostByItemId = new HashMap<>();
        if (!publicPostIds.isEmpty()) {
            Map<Long, Long> itemIdByPublicPostId = new HashMap<>();
            for (InventoryItem item : items) {
                if (item.getPublicPostId() != null) {
                    itemIdByPublicPostId.put(item.getPublicPostId(), item.getId());
                }
            }
            List<PublicPost> posts = baseMapper.selectList(
                new LambdaQueryWrapper<PublicPost>()
                    .eq(PublicPost::getUserId, currentUserId)
                    .in(PublicPost::getId, publicPostIds)
            );
            for (PublicPost post : posts) {
                Long itemId = itemIdByPublicPostId.get(post.getId());
                if (itemId != null) {
                    linkedPostByItemId.put(itemId, post);
                }
            }
        }

        List<PublicPost> sourceLinkedPosts = baseMapper.selectList(
            new LambdaQueryWrapper<PublicPost>()
                .eq(PublicPost::getUserId, currentUserId)
                .in(PublicPost::getSourceInventoryItemId, sourceItemIds)
        );
        for (PublicPost post : sourceLinkedPosts) {
            linkedPostByItemId.putIfAbsent(post.getSourceInventoryItemId(), post);
        }
        return linkedPostByItemId;
    }

    private void createSourcePublicPost(
        Long currentUserId,
        InventoryItem item,
        SourceItemPublicToggleCommand command,
        UserAccount user
    ) {
        LocalDateTime now = LocalDateTime.now();
        PublicPost post = new PublicPost();
        post.setUserId(currentUserId);
        post.setSourceInventoryItemId(item.getId());
        post.setItemName(item.getItemName());
        post.setPrice(command.price());
        post.setTradeTime(command.tradeTime());
        post.setDirection(command.direction());
        post.setChannel(item.getChannel());
        post.setCategory(item.getCategory());
        post.setRemark(normalizeNullableText(command.remark()));
        post.setImageFileId(preparePublicImageFile(currentUserId, resolveSourceImageFileId(item, command.imageFileId())));
        post.setUntrustedCount(0);
        post.setPublisherName(resolvePublisherName(user));
        post.setPublisherAvatar(normalizeNullableText(user.getAvatarUrl()));
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        baseMapper.insert(post);

        item.setPublicPosted(Boolean.TRUE);
        item.setPublicPostId(post.getId());
        item.setPublicPostedAt(now);
        item.setUpdatedAt(now);
        inventoryItemMapper.updateById(item);
    }

    private void deleteLinkedPosts(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return;
        }
        publicPostFlagMapper.delete(new QueryWrapper<PublicPostFlag>().in("post_id", postIds));
        baseMapper.delete(new QueryWrapper<PublicPost>().in("id", postIds));
    }

    private void rollbackSourceItemPublicState(PublicPost post, Long currentUserId) {
        if (post.getSourceInventoryItemId() == null) {
            return;
        }

        InventoryItem item = inventoryItemMapper.selectById(post.getSourceInventoryItemId());
        if (item == null) {
            return;
        }
        if (!item.getUserId().equals(currentUserId)) {
            return;
        }
        if (item.getPublicPostId() != null && !item.getPublicPostId().equals(post.getId())) {
            return;
        }

        clearSourceItemPublicState(item);
    }

    private PublicPost findOwnedLinkedSourcePost(Long currentUserId, InventoryItem item) {
        if (item.getPublicPostId() != null) {
            PublicPost linkedPost = baseMapper.selectById(item.getPublicPostId());
            if (linkedPost != null && linkedPost.getUserId().equals(currentUserId)) {
                return linkedPost;
            }
        }
        return baseMapper.selectOne(
            new LambdaQueryWrapper<PublicPost>()
                .eq(PublicPost::getUserId, currentUserId)
                .eq(PublicPost::getSourceInventoryItemId, item.getId())
                .last("limit 1")
        );
    }

    private void clearSourceItemPublicState(InventoryItem item) {
        LocalDateTime now = LocalDateTime.now();
        item.setPublicPosted(Boolean.FALSE);
        item.setPublicPostId(null);
        item.setPublicPostedAt(null);
        item.setUpdatedAt(now);
        inventoryItemMapper.update(
            null,
            new LambdaUpdateWrapper<InventoryItem>()
                .eq(InventoryItem::getId, item.getId())
                .set(InventoryItem::getPublicPosted, Boolean.FALSE)
                .set(InventoryItem::getPublicPostId, null)
                .set(InventoryItem::getPublicPostedAt, null)
                .set(InventoryItem::getUpdatedAt, now)
        );
    }

    private Set<Long> getFlaggedPostIds(Long currentUserId, List<Long> postIds) {
        if (currentUserId == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return publicPostFlagMapper.selectList(
            new LambdaQueryWrapper<PublicPostFlag>()
                .eq(PublicPostFlag::getUserId, currentUserId)
                .in(PublicPostFlag::getPostId, postIds)
        ).stream().map(PublicPostFlag::getPostId).collect(Collectors.toSet());
    }

    private boolean hasFlaggedPost(Long currentUserId, Long postId) {
        if (currentUserId == null) {
            return false;
        }
        Long count = publicPostFlagMapper.selectCount(
            new LambdaQueryWrapper<PublicPostFlag>()
                .eq(PublicPostFlag::getUserId, currentUserId)
                .eq(PublicPostFlag::getPostId, postId)
        );
        return count != null && count > 0;
    }

    private record PriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
    }
}
