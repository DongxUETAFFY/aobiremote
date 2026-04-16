# 项目真实进度

最后更新时间：`2026-04-16`

## 使用规则

1. 这里只记录已经真实落地并验证过的内容。
2. 只写了文档、还没写代码，或代码没验证过的内容，不能写成“已完成”。
3. 每完成一个明确功能切片后，必须同步更新本文件。
4. 如果文档与代码冲突，以代码和真实验证结果为准，并立刻修正文档。

## 已完成

- Web 项目独立目录已搭建完成，与旧微信小程序项目解耦：
  - `frontend`
  - `backend`
  - `deploy`
  - `scripts`
  - `sql`
- 核心文档已建立：
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
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/auth/me`
  - `POST /api/auth/change-password`
  - `POST /api/auth/logout`
- 本地轻量联调方案已建立：
  - `application-dev.yml` 使用 MySQL + 内存缓存 + 本地文件存储
  - 当前本地运行不依赖 Redis / MinIO / 真实邮件服务
  - 已有后端启动脚本
- Maven 全局环境已可用：
  - `Maven 3.9.14`
  - `JAVA_HOME=E:\develop`
  - `MAVEN_HOME=E:\tools\apache-maven-3.9.14`
- 数据库脚本已落地：
  - `backend/src/main/resources/schema.sql`
  - `backend/src/main/resources/schema-h2.sql`
- 图片上传最小闭环已完成并已验证：
  - 后端接口：`POST /api/files/images`
  - 后端接口：`GET /api/files/{fileId}/preview`
  - 图片元数据写入 `file_asset`
  - 文件落地到本地目录 `./.local-data/uploads`
  - 支持 `image/jpeg`、`image/png`、`image/webp`
  - 后端大小限制为 `614400 bytes`
  - 前端会先压缩，目标 `400KB` 内，复杂图片允许落在 `500KB` 内
  - 已实现方形缩略图与点击查看完整图片
  - 已修复私有页公开联动时图片未跟随带入公开记录的问题
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
- 三大业务页前端已经接入真实接口，不再是骨架页：
  - `WarehouseView.vue`：列表 + 表单 + 图片上传 + 分页
  - `ProfitView.vue`：列表 + 表单 + 图片上传 + 分页
  - `PublicZoneView.vue`：列表 + 表单 + 图片上传 + 分页
- 分页条组件已完成并接入三页：
  - `frontend/src/components/common/PaginationBar.vue`
  - 支持首页 / 末页 / 上一页 / 下一页 / 页码窗口省略号
  - 已在页面顶部和底部接入
- 关于页已有真实内容页：
  - 版本信息
  - 更新日志区块
  - 作者说明区块
- 当前项目本地已可运行：
  - 前端开发服务可启动
  - 后端 API 可启动
- 编译验证已通过：
  - 后端：`mvn -q -DskipTests compile`
  - 前端：`npm run build`

## 未完成、待开发

### 我的仓库

- 搜索条：未做
  - 输入框
  - 搜索触发按钮
  - 筛选触发按钮
- 筛选弹层：未做
  - 标题 / 副标题
  - 条件 chip
  - 重置 / 应用按钮

### 盈亏统计

- 筛选弹层：未做

### 公开交易区

- 备注完整预览弹层：未做
  - 当前列表里备注不是完整弹层预览交互

### 关于页

- 赞赏码弹层：未做
- 创作者说明弹层：未做
- 更新日志弹层：未做
- 当前是静态区块展示，不是弹层交互

### 其他技术项

- MinIO 存储实现：未做
  - 当前仍是本地文件存储
- Docker Compose / Nginx 联调部署：未做
- 单元测试：未做
- 回归测试清单：未做
- 前后端联调清单：未做
- 备份与监控方案：未做

## 当前状态判断

- 项目已经不是“只有骨架”，而是已经进入“可跑、可用、但还没收口”的阶段。
- 目前最主要的缺口不是核心 CRUD，而是：
  - 三页细节交互补齐
  - 关于页弹层化
  - 存储与部署收口
  - 测试与交付保障
- 三页存在基础响应式处理，但还不能算“完整移动端适配完成”。

## 开发顺序（强制执行）

1. 补齐三大业务页剩余缺口：
   - 我的仓库搜索条与筛选弹层
   - 盈亏统计筛选弹层
   - 公开交易区备注完整预览弹层
2. 补齐关于页三个弹层：
   - 赞赏码
   - 创作者说明
   - 更新日志
3. 做三页移动端适配收口。
4. 做 MinIO 存储实现与替换策略。
5. 做 Docker Compose / Nginx 联调部署。
6. 最后补测试、回归清单、交付文档。

## 进度核对优先级

1. `PROJECT_STATUS.md`
2. `guide.md`
3. `AOBIDAO_WEB_SDD.md`
4. `DB_SCHEMA.md`
5. `API_DESIGN.md`
6. 真实代码与真实联调结果
