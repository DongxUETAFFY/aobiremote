package io.github.dongxuetaffy.aobihelper.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryMarkSoldRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryPageQuery;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryUpsertRequest;
import io.github.dongxuetaffy.aobihelper.inventory.entity.InventoryItem;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryDetailVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryItemIdVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryPageResponseVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryTogglePublicVO;

public interface InventoryItemService extends IService<InventoryItem> {
    InventoryPageResponseVO pageWarehouseItems(Long userId, InventoryPageQuery query);

    InventoryDetailVO getInventoryItemDetail(Long userId, Long itemId);

    InventoryItemIdVO createWarehouseItem(Long userId, InventoryUpsertRequest request);

    InventoryItemIdVO updateWarehouseItem(Long userId, Long itemId, InventoryUpsertRequest request);

    InventoryItemIdVO markItemSold(Long userId, Long itemId, InventoryMarkSoldRequest request);

    InventoryTogglePublicVO togglePublic(Long userId, Long itemId, InventoryTogglePublicRequest request);

    void deleteInventoryItem(Long userId, Long itemId, String requestId);
}
