package io.github.dongxuetaffy.aobihelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file")
public class FileProperties {
    private String localRoot = "./.local-data/uploads";
    private long maxImageBytes = 614400;
    private int maxConcurrentImageStores = 20;

    public String getLocalRoot() {
        return localRoot;
    }

    public void setLocalRoot(String localRoot) {
        this.localRoot = localRoot;
    }

    public long getMaxImageBytes() {
        return maxImageBytes;
    }

    public void setMaxImageBytes(long maxImageBytes) {
        this.maxImageBytes = maxImageBytes;
    }

    public int getMaxConcurrentImageStores() {
        return maxConcurrentImageStores;
    }

    public void setMaxConcurrentImageStores(int maxConcurrentImageStores) {
        this.maxConcurrentImageStores = maxConcurrentImageStores;
    }
}
