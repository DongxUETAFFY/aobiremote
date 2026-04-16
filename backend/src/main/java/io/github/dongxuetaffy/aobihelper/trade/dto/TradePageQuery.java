package io.github.dongxuetaffy.aobihelper.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradePageQuery {
    private Long pageNo;
    private Long pageSize;
    private String scope;

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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
