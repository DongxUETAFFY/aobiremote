package io.github.dongxuetaffy.aobihelper.publiczone.vo;

public class PublicPostToggleUntrustedVO {
    private Long postId;
    private Boolean flagged;
    private Integer untrustedCount;

    public PublicPostToggleUntrustedVO() {
    }

    public PublicPostToggleUntrustedVO(Long postId, Boolean flagged, Integer untrustedCount) {
        this.postId = postId;
        this.flagged = flagged;
        this.untrustedCount = untrustedCount;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }

    public Integer getUntrustedCount() {
        return untrustedCount;
    }

    public void setUntrustedCount(Integer untrustedCount) {
        this.untrustedCount = untrustedCount;
    }
}
