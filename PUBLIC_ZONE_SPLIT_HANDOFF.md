# 公开交易区拆分交接说明

本文给“没有上下文的另一个 AI”使用。

目标不是重做页面，而是把【公开交易区】改成：

- 共享逻辑
- 分离桌面/移动展示组件

并且尽量不影响现在已经可用的桌面端效果。

---

## 先读什么

开始前先读：

1. `guide.md`
2. `PROJECT_STATUS.md`
3. `frontend/src/views/public-zone/PublicZoneView.vue`
4. 参考已完成拆分：
   - `frontend/src/views/warehouse/WarehouseView.vue`
   - `frontend/src/views/warehouse/WarehouseDesktopContent.vue`
   - `frontend/src/views/warehouse/WarehouseMobileContent.vue`
   - `frontend/src/views/profit/ProfitView.vue`
   - `frontend/src/views/profit/ProfitDesktopContent.vue`
   - `frontend/src/views/profit/ProfitMobileContent.vue`

---

## 这次任务真正要做什么

把当前：

- `frontend/src/views/public-zone/PublicZoneView.vue`

改成和【我的仓库】/【盈亏统计】一样的结构：

- `PublicZoneView.vue` 只负责共享逻辑
- `PublicZoneDesktopContent.vue` 负责桌面端展示
- `PublicZoneMobileContent.vue` 负责移动端展示

推荐文件名：

- `frontend/src/views/public-zone/PublicZoneDesktopContent.vue`
- `frontend/src/views/public-zone/PublicZoneMobileContent.vue`

---

## 最重要的原则

### 1. 桌面端优先“保留现状”，不是重新设计

之前【我的仓库】拆分时出过问题：

- 结构拆开是对的
- 但桌面端被顺手重排后，用户明确觉得“越改越丑”

所以这次做【公开交易区】时：

- 先把当前 `PublicZoneView.vue` 的桌面模板几乎原样搬进 `PublicZoneDesktopContent.vue`
- 不要借拆分之机顺手改 Web 视觉
- 不要发明新排版、新按钮风格、新信息层级

一句话：

- “先无损拆开，再谈优化”

### 2. 父页只保留逻辑，不保留桌面/移动专属展示结构

父页 `PublicZoneView.vue` 应只保留：

- 状态
- 计算属性
- 接口请求
- 筛选逻辑
- 发布/编辑/删除/不可信逻辑
- 图片上传逻辑
- 公共弹窗
- 回顶部与悬浮按钮
- `isMobile` 断点判断

桌面/移动具体列表结构、筛选区布局、头图区布局，应放进对应展示组件。

### 3. 移动端可以重新整理，但不要改业务链路

移动端布局可以单独优化，但不能改掉这些已有功能：

- 顶部标题卡
- 名称搜索
- 价格区间查询
- 范围筛选
- 方向筛选
- 渠道筛选
- 分类筛选
- 顶部和底部分页
- 发布/编辑/删除
- 不可信切换
- 时间格式化
- 图片预览

---

## 当前 `PublicZoneView.vue` 里有哪些逻辑必须留在父页

这些都应该继续放在父页，不要复制两份到桌面/移动组件中：

- `authStore` 登录态判断
- `items / totalCount / currentPage / loading`
- `filterScope / filterDirection / filterChannel / filterCategory`
- `filterKeyword / filterMinPrice / filterMaxPrice`
- `dialogVisible / dialogTitle / editingId / formLoading`
- 发布表单 `form`
- 图片上传相关：
  - `fileInputRef`
  - `compressionResult`
  - `uploadingImage`
  - `uploadProgress`
  - `compressionSummary`
- `loadData`
- `handleFilterChange`
- `handleKeywordSearch`
- `handleKeywordClear`
- `handlePriceSearch`
- `handlePriceClear`
- `handlePageChange`
- `openAddDialog`
- `openEditDialog`
- `resetForm`
- `handlePickImage`
- `handleFileChange`
- `handleSubmitForm`
- `handleDelete`
- `handleToggleUntrusted`
- `channelLabel`
- `categoryLabel`
- `formatDate`
- `directionLabel`
- `showBackToTop / handleScroll / scrollToTop`

新增的响应式拆分逻辑也应该放在父页：

- `const MOBILE_BREAKPOINT = 768`
- `const isMobile = ref(false)`
- `handleResize`
- `window.addEventListener('resize', handleResize, { passive: true })`

可直接参考：

- `frontend/src/views/warehouse/WarehouseView.vue`
- `frontend/src/views/profit/ProfitView.vue`

---

## 建议怎么拆

### 第一步：先复制桌面模板

先从当前 `PublicZoneView.vue` 中提取：

- 头部区块
- 筛选区块
- 顶部分页
- 列表卡片
- 底部分页

放进 `PublicZoneDesktopContent.vue`。

要求：

- 桌面端视觉尽量不变
- 桌面端 class 名尽量不变
- 桌面端 CSS 尽量直接搬走，不要随手重写

### 第二步：再做移动端组件

再新建 `PublicZoneMobileContent.vue`。

移动端可以参考：

- `WarehouseMobileContent.vue`
- `ProfitMobileContent.vue`

建议保留这些信息优先级：

1. 物品名
2. 方向
3. 价格
4. 图片
5. 渠道/分类/交易时间
6. 发布时间
7. `不可信 × N`
8. 编辑/删除

### 第三步：父页改成切换渲染

在 `PublicZoneView.vue` 中：

- 引入 `PublicZoneDesktopContent`
- 引入 `PublicZoneMobileContent`
- 增加 `isMobile`
- 模板里改成：
  - `v-if="isMobile"` 渲染移动端
  - `v-else` 渲染桌面端

共享弹窗、浮动按钮、回顶部按钮继续留在父页。

---

## 组件边界建议

### 桌面/移动子组件只做这些事

- 接收 props
- 抛出事件
- 渲染结构

不要在子组件里直接调用接口。

### 建议的 props

至少会用到：

- `loading`
- `items`
- `totalCount`
- `currentPage`
- `pageSize`
- `filterScope`
- `filterDirection`
- `filterChannel`
- `filterCategory`
- `filterKeyword`
- `filterMinPrice`
- `filterMaxPrice`
- `authStore.isAuthenticated` 对应的布尔值
- `channelOptions`
- `categoryOptions`
- `channelLabel`
- `categoryLabel`
- `formatDate`
- `directionLabel`

### 建议的 emits

至少会用到：

- `open-add`
- `page-change`
- `update:filterKeyword`
- `update:filterMinPrice`
- `update:filterMaxPrice`
- `update:filterScope`
- `update:filterDirection`
- `update:filterChannel`
- `update:filterCategory`
- `keyword-search`
- `keyword-clear`
- `price-search`
- `price-clear`
- `filter-change`
- `edit`
- `delete`
- `toggle-untrusted`

---

## 特别容易踩坑的地方

### 1. 不可信交互不能丢

现在公开交易区里：

- 所有人都能看到 `不可信 × N`
- 登录且不是自己发布的记录，可以点击这行文案
- 同一用户再次点击，会撤销自己的不可信

所以拆分时要保留现有行为，不要退回到旧逻辑。

### 2. 时间格式不要改坏

当前已经把：

- `2026-04-16T20:30:47.792647`

显示成：

- `2026-04-16 20:30:47`

依赖的是父页里的 `formatDate`，拆分时继续复用，不要各写一套。

### 3. 顶部和底部分页都要保留

当前三页统一要求：

- 每页最多 30 条
- 顶部和底部都显示分页
- 即使只有 1 页，也显示 `第 1 / 1 页`

不要因为移动端紧凑，就删掉顶部分页。

### 4. 浮动按钮先不要拆进子组件

当前 `PublicZoneView.vue` 里有：

- 回顶部按钮
- 刷新按钮
- 发布按钮

这几个先留在父页最稳，避免桌面/移动双份维护。

### 5. `hasMore` 现在基本没在界面里发挥作用

当前公开交易区已经是标准分页，不是无限加载。

所以拆分时：

- 不要重新做“加载更多”
- 不要因为看到 `hasMore` 就脑补恢复无限滚动

### 6. 只做拆分，不顺手改交互文案

这次的目标不是再改：

- 按钮位置
- 标签文案
- 列表信息顺序
- 公开区视觉风格

除非拆分时发现编译或显示错误，否则尽量不动。

---

## 推荐执行顺序

1. 新建 `PublicZoneDesktopContent.vue`
2. 先把当前桌面模板和样式搬进去
3. 新建 `PublicZoneMobileContent.vue`
4. 用更适合手机的结构组织移动端卡片
5. 改 `PublicZoneView.vue` 为共享逻辑父页
6. 加 `isMobile` 和 `resize` 监听
7. 编译验证
8. 更新 `PROJECT_STATUS.md`
9. 提交到本地 `dev`

---

## 验证要求

至少做：

1. `npm run build`
2. 手动检查：
   - `/public-zone` 桌面端是否和拆分前接近
   - 移动端是否能正常筛选、翻页、发布、编辑、删除、不可信
   - 顶部和底部分页是否都在
   - 时间格式是否还是 `YYYY-MM-DD HH:mm:ss`
   - 未登录和已登录两种状态下按钮是否正常

---

## 成功标准

如果满足下面这些，就算这次拆分成功：

- `PublicZoneView.vue` 只保留共享逻辑
- 新增桌面/移动两个展示组件
- 桌面端外观没有明显变丑
- 移动端可以单独改布局
- 功能链路不丢
- `npm run build` 通过

---

## 一句话总结

这次不要“借拆分顺手重做页面”。

最优解是：

- 父页收逻辑
- 桌面端尽量原样搬走
- 移动端单独排版
- 功能和交互一个都别丢
