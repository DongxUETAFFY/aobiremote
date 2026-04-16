package io.github.dongxuetaffy.aobihelper.trade.service;

import io.github.dongxuetaffy.aobihelper.trade.dto.TradePageQuery;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeTogglePublicRequest;
import io.github.dongxuetaffy.aobihelper.trade.dto.TradeUpsertRequest;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeDetailVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeIdVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradePageResponseVO;
import io.github.dongxuetaffy.aobihelper.trade.vo.TradeTogglePublicVO;

public interface TradeService {
    TradePageResponseVO pageTradeItems(Long userId, TradePageQuery query);

    TradeDetailVO getTradeDetail(Long userId, Long itemId);

    TradeIdVO createTrade(Long userId, TradeUpsertRequest request);

    TradeIdVO updateTrade(Long userId, Long itemId, TradeUpsertRequest request);

    void deleteTrade(Long userId, Long itemId, String requestId);

    TradeTogglePublicVO togglePublic(Long userId, Long itemId, TradeTogglePublicRequest request);
}
