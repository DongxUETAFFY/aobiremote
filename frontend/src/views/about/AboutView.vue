<script setup lang="ts">
import { h } from 'vue'
import { ElImage, ElMessageBox } from 'element-plus'

type AboutItemKey = 'version' | 'creator' | 'log' | 'reward'

type AboutItem = {
  key: AboutItemKey
  title: string
  desc: string
}

const version = 'v1.0.7'
const updatedAt = '2026-04-21'
const rewardImageUrl = '/reward-code.png'

const aboutItems: AboutItem[] = [
  {
    key: 'version',
    title: '版本信息',
    desc: '查看当前版本号、更新时间和功能范围。',
  },
  {
    key: 'creator',
    title: '关于作者',
    desc: '了解这个小助手的来历和设计初衷。',
  },
  {
    key: 'log',
    title: '更新日志',
    desc: '快速查看最近做了哪些优化和修复。',
  },
  {
    key: 'reward',
    title: '赞赏支持',
    desc: '如果这个工具帮到了你，可以请作者喝杯饮料。',
  },
]

const creatorText = [
  '这个小助手是为奥比岛玩家日常记账和整理物品信息做的。',
  '',
  '它会持续围绕几个方向打磨：',
  '1. 记账流程更顺手',
  '2. 图片和物品信息更好管理',
  '3. 公开交易区浏览体验更清楚',
  '',
  '感谢每一次反馈和建议，它们都会直接影响后续更新。',
].join('\n')

const changeLogText = [
  '更新日志',
  '',
  '2026-04-21 v1.0.7',
  '1. 仓库、盈亏统计和公开交易区支持按价格从低到高或从高到低排序。',
  '2. 新增记录时，分类可直接点击按钮选择，减少操作步骤。',
  '3. 买入日期默认填入当天，新增记录更省心。',
  '4. 优化电脑端顶部和筛选区排版，搜索、排序和筛选更清楚。',
  '5. 公开交易区电脑端的渠道和分类改为直接点击筛选。',
  '',
  '2026-04-20 v1.0.6',
  '1. 管理员端公开交易图片改成更清晰的大卡片显示，查看更方便。',
  '2. 电脑端上传支持点击、拖拽和粘贴，换图前会先确认。',
  '3. 图片会自动压缩后再上传，操作更省心。',
  '',
  '2026-04-18 v1.0.5',
  '1. 修复了部分公开交易图片显示失败的问题。',
  '2. 优化了公开和图片访问相关的稳定性。',
  '',
  '2026-04-16 v1.0.4',
  '1. 关于页补齐了版本信息、作者说明、更新日志和赞赏入口。',
  '2. 页面结构调整得更清楚，查看信息更直接。',
  '',
  '2026-04-14 v1.0.3',
  '1. 公开交易区支持更完整的筛选和浏览。',
  '2. 仓库、盈利统计等基础功能继续完善。',
].join('\n')

const openVersionInfo = async () => {
  await ElMessageBox({
    title: '版本信息',
    message: h(
      'div',
      {
        style: 'white-space: pre-line; line-height: 1.8; color: #694e5d;',
      },
      [
        `当前版本：${version}`,
        `更新时间：${updatedAt}`,
        '',
        '当前版本已支持仓库管理、盈利统计、公开交易区、管理员端和关于页等基础功能。',
      ].join('\n'),
    ),
    showCancelButton: false,
    confirmButtonText: '知道了',
    closeOnClickModal: true,
    closeOnPressEscape: true,
  })
}

const openTextPopup = async (title: string, text: string) => {
  await ElMessageBox({
    title,
    message: h(
      'pre',
      {
        style: [
          'margin: 0',
          'white-space: pre-wrap',
          'word-break: break-word',
          'line-height: 1.8',
          'font-size: 14px',
          'font-family: inherit',
          'color: #694e5d',
        ].join(';'),
      },
      text,
    ),
    showCancelButton: false,
    confirmButtonText: '关闭',
    closeOnClickModal: true,
    closeOnPressEscape: true,
    customClass: 'about-message-box',
  })
}

const openRewardPopup = async () => {
  await ElMessageBox({
    title: '赞赏支持',
    message: h('div', { class: 'about-popup-reward' }, [
      h(ElImage, {
        class: 'about-popup-reward__image',
        src: rewardImageUrl,
        previewSrcList: [rewardImageUrl],
        previewTeleported: true,
        fit: 'cover',
      }),
      h(
        'p',
        {
          class: 'about-popup-reward__tip',
        },
        '感谢你的支持。赞赏完全自愿，只要这个工具能帮到你，我就很开心。',
      ),
    ]),
    showCancelButton: false,
    confirmButtonText: '关闭',
    closeOnClickModal: true,
    closeOnPressEscape: true,
    customClass: 'about-message-box about-message-box--reward',
  })
}

const openItem = async (key: AboutItemKey) => {
  if (key === 'version') {
    await openVersionInfo()
    return
  }

  if (key === 'creator') {
    await openTextPopup('关于作者', creatorText)
    return
  }

  if (key === 'log') {
    await openTextPopup('更新日志', changeLogText)
    return
  }

  await openRewardPopup()
}
</script>

<template>
  <div class="ah-page-shell about-page">
    <section class="about-hero ah-glass-card ah-page-section">
      <p class="about-hero__eyebrow">Aobi Helper Web</p>
      <h2 class="about-hero__title">关于</h2>
      <p class="about-hero__subtitle">
        在这里查看版本信息、作者说明、更新日志和赞赏支持。
      </p>
    </section>

    <section class="about-panel ah-glass-card ah-page-section">
      <div class="about-list">
        <button
          v-for="item in aboutItems"
          :key="item.key"
          class="about-item"
          type="button"
          @click="openItem(item.key)"
        >
          <div class="about-item__main">
            <div class="about-item__title">{{ item.title }}</div>
            <div class="about-item__desc">{{ item.desc }}</div>
          </div>
          <span class="about-item__action">查看</span>
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 0 0 40px;
}

.about-hero__eyebrow {
  margin: 0 0 10px;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.about-hero__title {
  margin: 0;
  color: var(--ah-title);
  font-size: 32px;
  line-height: 1.1;
}

.about-hero__subtitle {
  margin: 12px 0 0;
  color: var(--ah-text);
  line-height: 1.7;
}

.about-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.about-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border: 1px solid rgba(172, 204, 150, 0.58);
  border-radius: 24px;
  background: rgba(255, 252, 245, 0.98);
  cursor: pointer;
  text-align: left;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.about-item:hover {
  transform: translateY(-1px);
  border-color: rgba(138, 179, 111, 0.72);
  box-shadow: 0 16px 28px rgba(170, 186, 136, 0.16);
}

.about-item__main {
  flex: 1;
  min-width: 0;
}

.about-item__title {
  color: var(--ah-title);
  font-size: 18px;
  font-weight: 700;
}

.about-item__desc {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
  line-height: 1.6;
}

.about-item__action {
  flex-shrink: 0;
  min-width: 82px;
  padding: 10px 16px;
  border-radius: 999px;
  background: linear-gradient(180deg, #9fcb87 0%, #79a966 100%);
  color: #fffdf8;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

@media (max-width: 768px) {
  .about-page {
    gap: 5px;
    padding-bottom: 12px;
  }

  .about-hero {
    display: none;
  }

  .about-hero__title {
    font-size: 18px;
  }

  .about-hero__eyebrow {
    margin-bottom: 4px;
    font-size: 11px;
  }

  .about-hero__subtitle {
    margin-top: 5px;
    font-size: 11px;
    line-height: 1.55;
  }

  .about-list {
    gap: 7px;
  }

  .about-item {
    padding: 9px;
    border-radius: 12px;
    align-items: center;
  }

  .about-item__title {
    font-size: 13px;
  }

  .about-item__desc {
    margin-top: 3px;
    font-size: 11px;
    line-height: 1.45;
  }

  .about-item__action {
    min-width: 48px;
    padding: 5px 8px;
    font-size: 11px;
  }
}
</style>

<style>
.about-message-box {
  width: min(560px, calc(100vw - 24px));
  border-radius: 28px;
  padding: 8px;
}

.about-message-box .el-message-box__title {
  color: #5f4252;
  font-size: 22px;
}

.about-message-box .el-message-box__content {
  padding-top: 8px;
}

.about-message-box .el-message-box__btns {
  padding-top: 18px;
}

.about-message-box .el-button--primary {
  border-radius: 999px;
  border-color: #88b26f;
  background: linear-gradient(180deg, #9fcb87 0%, #79a966 100%);
}

.about-popup-reward {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.about-popup-reward__image {
  display: block;
  width: min(300px, 100%);
  height: auto;
  border-radius: 22px;
  background: rgba(255, 254, 248, 0.98);
  border: 1px solid rgba(172, 204, 150, 0.64);
  box-shadow: inset 0 0 0 4px rgba(239, 246, 232, 0.72);
  overflow: hidden;
  cursor: zoom-in;
}

.about-popup-reward__tip {
  margin: 0;
  text-align: center;
  color: #694e5d;
  font-size: 14px;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .about-message-box {
    width: calc(100vw - 20px);
  }

  .about-message-box .el-message-box__title {
    font-size: 20px;
  }
}
</style>
