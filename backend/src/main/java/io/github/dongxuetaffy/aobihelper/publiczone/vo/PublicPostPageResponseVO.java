package io.github.dongxuetaffy.aobihelper.publiczone.vo;

import java.util.List;

public class PublicPostPageResponseVO {
    private List<PublicPostListItemVO> items;
    private Long pageNo;
    private Long pageSize;
    private Long totalCount;
    private Long totalPages;
    private Boolean hasMore;

    public List<PublicPostListItemVO> getItems() {
        return items;
    }

    public void setItems(List<PublicPostListItemVO> items) {
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
}
