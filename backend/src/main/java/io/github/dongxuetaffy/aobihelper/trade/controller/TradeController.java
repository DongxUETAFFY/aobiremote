package io.github.dongxuetaffy.aobihelper.trade.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradePageQuery;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeUpsertRequest;
import io.github.dongxuetaffy.aobihelper.trade.service.TradeService;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeDetailVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeIdVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradePageResponseVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeTogglePublicVO;
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
@RequestMapping("/api/trade")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/page")
    public ApiResponse<TradePageResponseVO> pageTradeItems(@ModelAttribute TradePageQuery query) {
        return ApiResponse.success(tradeService.pageTradeItems(currentUserId(), query));
    }

    @GetMapping("/{id}")
    public ApiResponse<TradeDetailVO> getTradeDetail(@PathVariable Long id) {
        return ApiResponse.success(tradeService.getTradeDetail(currentUserId(), id));
    }

    @PostMapping
    public ApiResponse<TradeIdVO> createTrade(@Valid @RequestBody TradeUpsertRequest request) {
        return ApiResponse.success("Trade record created", tradeService.createTrade(currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TradeIdVO> updateTrade(
        @PathVariable Long id,
        @Valid @RequestBody TradeUpsertRequest request
    ) {
        return ApiResponse.success("Trade record updated", tradeService.updateTrade(currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTrade(
        @PathVariable Long id,
        @RequestParam(required = false) String requestId
    ) {
        tradeService.deleteTrade(currentUserId(), id, requestId);
        return ApiResponse.success("Trade record deleted", null);
    }

    @PostMapping("/{id}/toggle-public")
    public ApiResponse<TradeTogglePublicVO> togglePublic(
        @PathVariable Long id,
        @Valid @RequestBody TradeTogglePublicRequest request
    ) {
        return ApiResponse.success("Public status toggled", tradeService.togglePublic(currentUserId(), id, request));
    }

    private Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
