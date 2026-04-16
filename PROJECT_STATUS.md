# 项目真实进度

最后更新时间：`2026-04-16`

## 使用规则

1. 这里只记录已经真实落地并验证过的内容。
2. 只写了文档、还没写代码，或代码没验证过的内容，不能写成“已完成”。
3. 每完成一个明确的小功能切片后，必须同步更新本文件。
4. 如果文档与代码冲突，以真实代码和真实验证结果为准，并立刻修正文档。

## 已完成

- Web 项目已独立建立，和旧微信小程序项目解耦：
  - `frontend`
  - `backend`
  - `deploy`
  - `scripts`
  - `sql`
- 核心工程文档已建立：
  - `MASTER_SKILL_INDEX.md`
  - `AOBIDAO_WEB_SDD.md`
  - `guide.md`
  - `ENGINEERING_BASELINE.md`
  - `PROJECT_STRUCTURE.md`
  - `PAGE_REPLICATION_CHECKLIST.md`
  - `DB_SCHEMA.md`
  - `API_DESIGN.md`
- 项目执行标准已明确：
  - 个人项目，可上线
  - 目标用户量 `1000 ~ 2000`
  - 目标并发 `200` 以内
  - 采用轻量、干净、可维护方案
  - 一次只推进一个最小功能闭环
- 前端骨架已初始化：
  - `Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus`
  - 已有基础路由、全局布局、状态管理、HTTP 封装
- 后端骨架已初始化：
  - `Spring Boot 3.5.x`
  - `MyBatis-Plus`
  - `Sa-Token`
  - 全局响应结构
  - 全局异常处理
- 认证闭环已完成并可本地联调：
  - `POST /api/auth/send-register-code`
  - `POST /api/auth/send-reset-password-code`
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/auth/me`
  - `POST /api/auth/change-password`
  - `POST /api/auth/reset-password`
  - `POST /api/auth/logout`
  - 登录页已增加“忘记密码”入口并接入邮箱验证码重置密码页面
- 本地轻量联调方案已建立：
  - `application-dev.yml` 使用 MySQL + 内存缓存 + 本地文件存储
  - 当前本地运行不依赖真实 Redis / MinIO / 邮件服务
  - 已有后端启动脚本
- Maven 全局环境可用：
  - `Maven 3.9.14`
  - `JAVA_HOME=E:\develop`
  - `MAVEN_HOME=E:\tools\apache-maven-3.9.14`
- 数据库脚本已落地：
  - `backend/src/main/resources/schema.sql`
  - `backend/src/main/resources/schema-h2.sql`
- 图片上传最小闭环已完成并验证：
  - `POST /api/files/images`
  - `GET /api/files/{fileId}/preview`
  - 图片元数据写入 `file_asset`
  - 文件落地到本地目录 `./.local-data/uploads`
  - 支持 `image/jpeg`、`image/png`、`image/webp`
  - 后端大小限制 `614400 bytes`
  - 前端先压缩，目标 `400KB` 内，复杂图片允许落在 `500KB` 内
  - 已实现方形缩略图与点击查看完整图片
  - 三大业务页已改为“选图后自动压缩并自动上传”
  - 已修复私有页公开联动时图片未随公开记录带入的问题
- 我的仓库后端接口已完成并通过真实回归：
  - `GET /api/inventory/page`
  - `GET /api/inventory/{id}`
  - `POST /api/inventory`
  - `PUT /api/inventory/{id}`
  - `POST /api/inventory/{id}/mark-sold`
  - `DELETE /api/inventory/{id}`
  - `POST /api/inventory/{id}/toggle-public`
- 盈亏统计后端接口已完成并通过真实回归：
  - `GET /api/trade/page`
  - `GET /api/trade/{id}`
  - `POST /api/trade`
  - `PUT /api/trade/{id}`
  - `DELETE /api/trade/{id}`
  - `POST /api/trade/{id}/toggle-public`
- 公开交易区后端接口已完成并通过真实回归：
  - `GET /api/public-post/page`
  - `GET /api/public-post/{id}`
  - `POST /api/public-post`
  - `PUT /api/public-post/{id}`
  - `POST /api/public-post/{id}/toggle-untrusted`
  - `DELETE /api/public-post/{id}`
- 私有页与公开交易区联动已完成：
  - 私有页可公开 / 取消公开
  - 删除公开记录后会回写私有页公开状态
  - 已公开记录再次点击时会直接取消公开，不再弹出公开设置窗口
- 防重复与防频繁操作保护已完成并接入主要写接口：
  - `requestId` 防重复提交
  - 过快操作拦截
  - 高频操作窗口限制
- 前端类型与 API 封装已接入三大业务域：
  - `frontend/src/types/inventory.ts`
  - `frontend/src/api/inventory.ts`
  - `frontend/src/types/trade.ts`
  - `frontend/src/api/trade.ts`
  - `frontend/src/types/public-post.ts`
  - `frontend/src/api/public-post.ts`
- 三大业务页前端已接入真实接口，不再是骨架页：
  - `WarehouseView.vue`：列表 + 表单 + 图片上传 + 分页
  - `ProfitView.vue`：列表 + 表单 + 图片上传 + 分页
  - `PublicZoneView.vue`：列表 + 表单 + 图片上传 + 分页
- 我的仓库搜索与筛选已完成：
  - 顶部搜索条已接入 `keyword`
  - 筛选弹层已接入价格区间与排序方式
  - 筛选摘要会随当前条件更新
- 盈亏统计筛选已完成：
  - 已新增筛选弹层
  - 已接入盈亏范围与排序方式
  - 后端已支持 `sortType`
- 分页条组件已完成并接入三页：
  - `frontend/src/components/common/PaginationBar.vue`
  - 支持首页 / 末页 / 上一页 / 下一页 / 页码窗口省略号
  - 已在页面顶部和底部接入
- 公开交易区备注预览已完成：
  - 列表内备注改为截断显示
  - 点击备注可弹出完整备注详情
- 公开交易区方向文案已与仓库/盈亏统计公开链路统一：
  - 筛选、卡片角标、发布/编辑弹层统一使用“买入 / 卖出”
- 公开交易区名称筛选已接入：
  - 前端新增名称搜索栏
  - 复用已有 `GET /api/public-post/page` 的 `keyword` 查询参数
- 公开交易区自定义价格区间筛选已接入：
  - 前端新增最低价 / 最高价输入
  - 复用已有 `GET /api/public-post/page` 的 `minPrice / maxPrice` 查询参数
- 公开交易区“不可信”链路已修复并调整展示：
  - 复用已有 `POST /api/public-post/{id}/toggle-untrusted` 接口
  - 前端改为按接口要求提交 `requestId` 请求体
  - 不可信总数固定展示在列表时间下方，所有人都可见
  - 不可信交互已收拢为卡片左下角单一的“不可信 × N”入口，点击该文案即可切换标记
  - 同一用户再次点击时会撤销自己的“不可信”标记并回退对应计数
- 三大业务页分类文案与默认项已统一：
  - `magic` 显示为“魔力时装”
  - `obi` 显示为“奥比时装”
  - 新增记录时默认分类改为“奥比时装”，并排在分类选项前面
- 我的仓库已接入批量公开最小闭环：
  - 前端新增本页勾选与“批量公开”入口
  - 优先复用已有 `POST /api/inventory/batch-toggle-public` 接口
  - 已公开或已卖出的物品在仓库页直接不可选，避免误触取消公开
  - 批量公开按钮已移到批量选择工具条右侧，和“全选 / 已选件数”放在同一区域
- 我的仓库 / 盈亏统计 分类统计与筛选已接入：
  - 优先复用已有仓库 / 盈亏分页接口返回的 `obi` / `magic` 汇总字段
  - 页面新增奥比总价格、奥比总件数、魔力总价格、魔力总件数展示
  - 点击奥比 / 魔力总价格可直接复用已有 `category` 查询参数筛选对应分类列表
- 我的仓库 / 盈亏统计 名称搜索已接入：
  - 仓库页优先复用已有 `GET /api/inventory/page` 的 `keyword` 查询参数
  - 盈亏页已补齐并接入 `GET /api/trade/page` 的 `keyword` 查询参数
  - 两页均支持输入名称、回车搜索、点击按钮搜索、清空恢复
- 三大业务页图片上传交互已收口：
  - 仓库、盈亏统计、公开交易区继续复用现有图片压缩与 `/api/files/images` 上传接口
  - 表单已移除单独的“上传图片”按钮
  - 用户点击“选择图片”后会直接完成压缩并上传成功
- 我的仓库 / 盈亏统计 公开交互已收口：
  - 两页继续复用现有 `toggle-public` 接口
  - 点击“公开”时直接沿用当前记录里的价格、交易时间、交易方向和备注
  - 两页已移除额外的公开设置弹层
- 公开交易区时间展示已收口：
  - 列表中的交易时间、发布时间已统一从 ISO 字符串显示为 `YYYY-MM-DD HH:mm:ss`
- 三大列表分页已统一收口：
  - 仓库、盈亏统计、公开交易区每页统一最多 30 条
  - 顶部与底部分页栏均显示“当前页 / 总页数”
  - 即使只有 1 页，也固定显示 `第 1 / 1 页`，并禁用 `<` `>` 与页码输入框
  - 支持输入页码跳转，且不会超过总页数
  - 支持点击 `<` / `>` 切换上一页、下一页
- 关于页已完成可交互版本：
  - 版本信息区块
  - 作者说明弹层
  - 更新日志弹层
  - 赞赏码弹层
  - 赞赏码图片已接入 Web 静态资源
  - 赞赏码小图已改为可直接预览大图，交互与物品列表图片预览一致
  - 已改为更贴近原小程序的顶部标题卡片 + 条目列表 + “查看”交互结构
  - 已补齐原小程序风格的版本信息弹窗与作者/日志/赞赏码查看弹窗
- 三大业务页移动端第一轮排版收口已完成：
  - 顶部区块在窄屏下改为更适合单列阅读
  - 列表卡片在手机下改成纵向堆叠
  - 主要操作按钮与分页区在手机下更易点击
  - 顶部头图右上角已收口账号操作，改为更贴近现有页面风格的胶囊式轻量入口
  - 表单弹窗宽度已改为跟随视口收缩
- 我的仓库已改为“共享逻辑 + 分离展示层”结构：
  - `WarehouseView.vue` 负责接口、状态、弹窗与事件分发
  - `WarehouseDesktopContent.vue` 负责桌面端展示
  - `WarehouseMobileContent.vue` 负责移动端展示
  - 后续可分别修改桌面/移动布局，互相影响显著降低
- 盈亏统计已改为“共享逻辑 + 分离展示层”结构：
  - `ProfitView.vue` 负责接口、状态、弹窗与事件分发
  - `ProfitDesktopContent.vue` 负责桌面端展示
  - `ProfitMobileContent.vue` 负责移动端展示
  - 后续可分别修改桌面/移动布局，互相影响显著降低
- 三大业务页移动端第二轮细化已完成：
  - 核心价格与交易信息在手机下更突出
  - 备注展示在手机下更容易阅读
  - 分类/渠道/时间改成更清晰的标签化展示
  - 悬浮按钮已增加安全区避让
- 当前项目本地可运行：
  - 前端开发服务可启动
  - 后端 API 可启动
- 编译验证已通过：
  - 后端：`mvn -q -DskipTests compile`
  - 前端：`npm run build`

## 未完成、待开发

### 其他技术项

- MinIO 存储实现：未做
  - 当前仍是本地文件存储
- Docker Compose / Nginx 联调部署：未做
- 单元测试：未做
- 回归测试清单：未做
- 前后端联调清单：未做
- 备份与监控方案：未做

### 移动端适配执行清单

- 三大业务页顶部区块在窄屏下改为单列布局
- 列表卡片在手机下改成纵向堆叠，避免信息挤压
- 表单弹窗在手机下改成更窄宽度和更顺手的间距
- 主要按钮、分页、筛选区保证手指点击舒服
- 图片、备注、价格、时间这些核心信息优先级重新排布
- 回到顶部、悬浮新增按钮检查手机遮挡和误触问题

## 当前状态判断

- 项目已经不是“只有骨架”，而是进入“可跑、可用、但还没全部收口”的阶段。
- 当前最主要的缺口不是核心 CRUD，而是：
  - 移动端适配收口
  - 存储与部署收口
  - 测试与交付保障
- 三页已完成第一轮移动端排版优化，但还不能算“完整移动端适配完成”。
- 三页已完成第二轮移动端细化，但仍需真实手机回归确认最终体验。

## 开发顺序

1. 做三页移动端适配收口。
2. 做 MinIO 存储实现与替换策略。
3. 做 Docker Compose / Nginx 联调部署。
4. 最后补测试、回归清单、交付文档。

## 进度核对优先级

1. `PROJECT_STATUS.md`
2. `guide.md`
3. `AOBIDAO_WEB_SDD.md`
4. `DB_SCHEMA.md`
5. `API_DESIGN.md`
6. 真实代码与真实联调结果
