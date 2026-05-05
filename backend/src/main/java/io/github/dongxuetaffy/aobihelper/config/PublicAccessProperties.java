package io.github.dongxuetaffy.aobihelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.public-access")
public class PublicAccessProperties {
    private int pageLimit = 30;
    private int pageWindowSeconds = 60;
    private int detailLimit = 60;
    private int detailWindowSeconds = 60;
    private int previewLimit = 120;
    private int previewWindowSeconds = 60;

    public int getPageLimit() {
        return pageLimit;
    }

    public void setPageLimit(int pageLimit) {
        this.pageLimit = pageLimit;
    }

    public int getPageWindowSeconds() {
        return pageWindowSeconds;
    }

    public void setPageWindowSeconds(int pageWindowSeconds) {
        this.pageWindowSeconds = pageWindowSeconds;
    }

    public int getDetailLimit() {
        return detailLimit;
    }

    public void setDetailLimit(int detailLimit) {
        this.detailLimit = detailLimit;
    }

    public int getDetailWindowSeconds() {
        return detailWindowSeconds;
    }

    public void setDetailWindowSeconds(int detailWindowSeconds) {
        this.detailWindowSeconds = detailWindowSeconds;
    }

    public int getPreviewLimit() {
        return previewLimit;
    }

    public void setPreviewLimit(int previewLimit) {
        this.previewLimit = previewLimit;
    }

    public int getPreviewWindowSeconds() {
        return previewWindowSeconds;
    }

    public void setPreviewWindowSeconds(int previewWindowSeconds) {
        this.previewWindowSeconds = previewWindowSeconds;
    }
}
