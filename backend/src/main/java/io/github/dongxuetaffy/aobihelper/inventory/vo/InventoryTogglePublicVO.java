package io.github.dongxuetaffy.aobihelper.inventory.vo;

public class InventoryTogglePublicVO {
    private Boolean publicPosted;
    private Long postId;

    public InventoryTogglePublicVO() {
    }

    public InventoryTogglePublicVO(Boolean publicPosted, Long postId) {
        this.publicPosted = publicPosted;
        this.postId = postId;
    }

    public Boolean getPublicPosted() {
        return publicPosted;
    }

    public void setPublicPosted(Boolean publicPosted) {
        this.publicPosted = publicPosted;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
