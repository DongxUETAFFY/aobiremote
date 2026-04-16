package io.github.dongxuetaffy.aobihelper.inventory.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryMarkSoldRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryBatchDeleteRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryBatchTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryPageQuery;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.inventory.dto.InventoryUpsertRequest;
import io.github.dongxuetaffy.aobihelper.inventory.service.InventoryItemService;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryDetailVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryItemIdVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryPageResponseVO;
import io.github.dongxuetaffy.aobihelper.inventory.vo.InventoryTogglePublicVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SaCheckLogin
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryItemService inventoryItemService;

    public InventoryController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @GetMapping("/page")
    public ApiResponse<InventoryPageResponseVO> pageWarehouseItems(@ModelAttribute InventoryPageQuery query) {
        return ApiResponse.success(inventoryItemService.pageWarehouseItems(currentUserId(), query));
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryDetailVO> getInventoryItemDetail(@PathVariable Long id) {
        return ApiResponse.success(inventoryItemService.getInventoryItemDetail(currentUserId(), id));
    }

    @PostMapping
    public ApiResponse<InventoryItemIdVO> createWarehouseItem(@Valid @RequestBody InventoryUpsertRequest request) {
        return ApiResponse.success("Inventory item created", inventoryItemService.createWarehouseItem(currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryItemIdVO> updateWarehouseItem(
        @PathVariable Long id,
        @Valid @RequestBody InventoryUpsertRequest request
    ) {
        return ApiResponse.success("Inventory item updated", inventoryItemService.updateWarehouseItem(currentUserId(), id, request));
    }

    @PostMapping("/{id}/mark-sold")
    public ApiResponse<InventoryItemIdVO> markItemSold(
        @PathVariable Long id,
        @Valid @RequestBody InventoryMarkSoldRequest request
    ) {
        return ApiResponse.success("Inventory item marked as sold", inventoryItemService.markItemSold(currentUserId(), id, request));
    }

    @PostMapping("/{id}/toggle-public")
    public ApiResponse<InventoryTogglePublicVO> togglePublic(
        @PathVariable Long id,
        @Valid @RequestBody InventoryTogglePublicRequest request
    ) {
        return ApiResponse.success("Public status toggled", inventoryItemService.togglePublic(currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteInventoryItem(
        @PathVariable Long id,
        @RequestParam(required = false) String requestId
    ) {
        inventoryItemService.deleteInventoryItem(currentUserId(), id, requestId);
        return ApiResponse.success("Inventory item deleted", null);
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDeleteInventoryItems(@Valid @RequestBody InventoryBatchDeleteRequest request) {
        inventoryItemService.batchDeleteInventoryItems(currentUserId(), request);
        return ApiResponse.success("Inventory items deleted", null);
    }

    @PostMapping("/batch-toggle-public")
    public ApiResponse<Void> batchToggleInventoryPublic(@Valid @RequestBody InventoryBatchTogglePublicRequest request) {
        inventoryItemService.batchTogglePublic(currentUserId(), request);
        return ApiResponse.success("Inventory public status toggled", null);
    }

    private Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
