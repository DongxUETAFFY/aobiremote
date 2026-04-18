<script setup lang="ts">
import { h } from 'vue'
import { ElImage, ElMessageBox } from 'element-plus'

type AboutItemKey = 'version' | 'creator' | 'log' | 'reward'

type AboutItem = {
  key: AboutItemKey
  title: string
  desc: string
}

const version = 'v1.0.0'
const updatedAt = '2026-04-16'
const rewardImageUrl = '/reward-code.png'

const aboutItems: AboutItem[] = [
  {
    key: 'version',
    title: '版本信息',
    desc: '查看当前版本号和更新时间。',
  },
  {
    key: 'creator',
    title: '关于作者',
    desc: '查看创作者信息。',
  },
  {
    key: 'log',
    title: '更新日志',
    desc: '查看最近的版本更新记录。',
  },
  {
    key: 'reward',
    title: '赞赏作者',
    desc: '预览赞赏码，并可保存图片。',
  },
]

const creatorText = [
  '本项目由个人创作者持续开发与维护。',
  '',
  '贴吧 ID：鹤箫雪',
  '奥比岛 ID：纯在做梦',
  '',
  '为爱发电，自费完成开发，无盈利行为。',
].join('\n')

const changeLogText = [
  '更新日志',
  '',
  '2026-04-16 v1.0.0',
  '新增：',
  '1. Web 版关于页改为与原小程序更接近的条目式结构。',
  '2. 补齐了版本信息、创作者说明、更新日志和赞赏码弹层。',
  '',
  '2026-04-15 v1.0.5',
  '修复：',
  '1. 修复了北京时间凌晨时日期选择上限显示为前一天的问题。',
  '2. 修复了跨天后部分页面日期校验不同步的问题。',
  '',
  '优化：',
  '1. 优化了日期选择与日期校验的稳定性。',
  '2. 优化了公开交易区备注的浏览体验，支持点击查看完整备注。',
  '3. 优化了三个分区上传物品时的分类顺序，并将默认分类调整为奥比时装。',
  '4. 优化了关于作者信息的显示方式，换行展示更清晰。',
  '',
  '2026-04-14 v1.0.4',
  '修复：',
  '1. 修复了公开状态与页面按钮显示不同步的问题。',
  '2. 修复了公开交易区图片显示异常的问题。',
  '3. 修复了部分页面文案显示异常的问题。',
  '',
  '优化：',
  '1. 优化了图片显示与加载体验。',
  '2. 优化了仓库、盈亏统计和公开交易区之间的数据同步体验。',
  '',
  '2026-04-13 v1.0.3',
  '修复：',
  '1. 修复了日期填写的问题。',
  '2. 修复了物品时间排序按交易时间排序。',
  '3. 修复了翻到底部时会自动跳转到下一页的问题。',
  '4. 进行了接口请求和图片存储的性能优化。',
  '',
  '新增：',
  '1. 新增了分页功能，并进行了性能优化。',
  '2. 新增了一键公开的功能。',
  '3. 新增了返回顶部的功能。',
  '4. 新增了公开交易区价格筛选的功能。',
  '5. 新增了添加物品备注的功能。',
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
        `最后更新：${updatedAt}`,
        '',
        '奥比岛助手 Web 版当前已支持仓库管理、盈亏统计、公开交易区和关于页基础功能。',
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
    title: '赞赏作者',
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
        '点击赞赏码小图可直接预览大图，交互与物品列表点击图片查看大图一致。',
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
        在这里查看版本信息、创作者说明、更新日志和赞赏码。
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
