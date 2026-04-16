package io.github.dongxuetaffy.aobihelper.publiczone.vo;

public class PublicSourceToggleVO {
    private Boolean publicPosted;
    private Long postId;

    public PublicSourceToggleVO() {
    }

    public PublicSourceToggleVO(Boolean publicPosted, Long postId) {
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
