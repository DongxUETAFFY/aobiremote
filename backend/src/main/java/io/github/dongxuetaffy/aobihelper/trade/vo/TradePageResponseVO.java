package io.github.dongxuetaffy.aobihelper.trade.vo;

import java.util.List;

public class TradePageResponseVO {
    private List<TradeListItemVO> items;
    private Long pageNo;
    private Long pageSize;
    private Long totalCount;
    private Long totalPages;
    private Boolean hasMore;
    private TradeSummaryVO summary;

    public List<TradeListItemVO> getItems() {
        return items;
    }

    public void setItems(List<TradeListItemVO> items) {
        this.items = items;
    }

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public TradeSummaryVO getSummary() {
        return summary;
    }

    public void setSummary(TradeSummaryVO summary) {
        this.summary = summary;
    }
}
