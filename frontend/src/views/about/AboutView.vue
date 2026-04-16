<script setup lang="ts">
</script>

<template>
  <div class="ah-page-shell about-page">
    <section class="about-card ah-glass-card ah-page-section">
      <p class="about-card__eyebrow">Aobi Helper Web</p>
      <h1 class="about-card__title">关于项目</h1>
      <p class="about-card__desc">
        面向独立部署的奥比岛助手 Web 版，支持仓库管理、盈亏统计与公开交易。
      </p>
    </section>

    <section class="about-card ah-glass-card ah-page-section">
      <h2 class="about-card__section-title">版本信息</h2>
      <div class="about-info-grid">
        <div class="about-info-item">
          <span class="about-info-item__label">当前版本</span>
          <span class="about-info-item__value">v1.0.0</span>
        </div>
        <div class="about-info-item">
          <span class="about-info-item__label">技术栈</span>
          <span class="about-info-item__value">Vue 3 + TypeScript + Vite + Element Plus / Spring Boot + MyBatis-Plus + Sa-Token</span>
        </div>
        <div class="about-info-item">
          <span class="about-info-item__label">最后更新</span>
          <span class="about-info-item__value">2026-04-16</span>
        </div>
      </div>
    </section>

    <section class="about-card ah-glass-card ah-page-section">
      <h2 class="about-card__section-title">更新日志</h2>
      <div class="about-changelog">
        <div class="about-changelog__item">
          <span class="about-changelog__date">2026-04-16</span>
          <span class="about-changelog__tag">新增</span>
          <span class="about-changelog__text">三大业务页（仓库/盈亏统计/公开交易区）前端接入完成，图片上传正式接入业务表单</span>
        </div>
        <div class="about-changelog__item">
          <span class="about-changelog__date">2026-04-16</span>
          <span class="about-changelog__tag">新增</span>
          <span class="about-changelog__text">图片上传最小闭环（前端压缩 + 后端存储 + 预览接口）</span>
        </div>
        <div class="about-changelog__item">
          <span class="about-changelog__date">2026-04-16</span>
          <span class="about-changelog__tag">新增</span>
          <span class="about-changelog__text">认证模块完整闭环（登录/注册/改密/登出）</span>
        </div>
        <div class="about-changelog__item">
          <span class="about-changelog__date">2026-04-15</span>
          <span class="about-changelog__tag">新增</span>
          <span class="about-changelog__text">项目骨架初始化，前后端基础架构搭建完成</span>
        </div>
      </div>
    </section>

    <section class="about-card ah-glass-card ah-page-section">
      <h2 class="about-card__section-title">关于作者</h2>
      <p class="about-card__text">
        个人项目，目标是为奥比岛玩家提供轻量、可独立部署的资产管理工具。
      </p>
      <p class="about-card__text">
        如果对你有帮助，欢迎使用和反馈。
      </p>
    </section>
  </div>
</template>

<style scoped>
.about-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 0 40px;
}

.about-card__eyebrow {
  margin: 0 0 8px;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.about-card__title {
  margin: 0 0 10px;
  color: var(--ah-title);
  font-size: 32px;
}

.about-card__desc {
  margin: 0;
  color: var(--ah-text);
  line-height: 1.6;
}

.about-card__section-title {
  margin: 0 0 16px;
  color: var(--ah-title);
  font-size: 20px;
}

.about-card__text {
  margin: 0 0 10px;
  color: var(--ah-text);
  line-height: 1.6;
}

.about-card__text:last-child {
  margin-bottom: 0;
}

.about-info-grid {
  display: grid;
  gap: 12px;
}

.about-info-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.about-info-item__label {
  flex-shrink: 0;
  width: 80px;
  color: #8d7080;
  font-size: 14px;
}

.about-info-item__value {
  color: var(--ah-title);
  font-size: 14px;
  word-break: break-all;
}

.about-changelog {
  display: grid;
  gap: 12px;
}

.about-changelog__item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.about-changelog__date {
  flex-shrink: 0;
  color: #8d7080;
  font-size: 13px;
  width: 80px;
}

.about-changelog__tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.about-changelog__text {
  flex: 1;
  color: var(--ah-text);
  font-size: 14px;
  min-width: 0;
}

@media (max-width: 600px) {
  .about-card__title {
    font-size: 24px;
  }
}
</style>
