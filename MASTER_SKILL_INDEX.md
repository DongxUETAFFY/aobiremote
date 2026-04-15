# 主 Skill 索引

本文档是 `aobidao-web` 的开发索引入口。

它不是新的大规范，也不重复描述已有文档内容。  
它只做一件事：

根据当前任务，告诉 AI 应该优先阅读哪一份文档，避免一次性加载全部文档，减少上下文膨胀、降低幻觉、提升工程执行稳定性。

## 1. 使用原则

后续开发时，默认不要每次都完整阅读所有文档。

应遵循：

1. 先看本索引
2. 根据任务类型，只读取必要文档
3. 如果任务扩大，再追加读取相关文档
4. 不要把多个文档中的同类内容重复搬进上下文

## 2. 文档职责总览

- `AOBIDAO_WEB_SDD.md`
  - 主规范
  - 红线
  - 当前阶段开发顺序
  - 不可违反的项目底线

- `ENGINEERING_BASELINE.md`
  - 上线工程基线
  - 认证、邮件、环境变量、CORS、HTTPS、上传、日志、备份

- `PROJECT_STRUCTURE.md`
  - 目录结构
  - 前后端模块划分
  - 文档、部署、脚本、SQL 目录布局

- `PAGE_REPLICATION_CHECKLIST.md`
  - 页面复刻目标
  - 三大业务页和关于页的高还原要求
  - 全局视觉与交互复刻要点

- `AI_DEVELOPMENT_SKILL.md`
  - 当前只是说明页
  - 不再作为独立规范使用

- `DB_SCHEMA.md`
  - 数据库表结构、字段、索引、约束
  - 当前待创建

- `API_DESIGN.md`
  - 接口路径、请求参数、响应结构、鉴权要求
  - 当前待创建

## 3. 任务到文档的映射

### 3.1 搭项目骨架

先读：

1. `AOBIDAO_WEB_SDD.md`
2. `PROJECT_STRUCTURE.md`
3. `ENGINEERING_BASELINE.md`

适用任务：

- 初始化 `frontend`
- 初始化 `backend`
- 建目录
- 配环境变量样例
- 建部署目录

### 3.2 登录、注册、修改密码

先读：

1. `AOBIDAO_WEB_SDD.md`
2. `ENGINEERING_BASELINE.md`
3. `DB_SCHEMA.md`（如果已存在）
4. `API_DESIGN.md`（如果已存在）

适用任务：

- 邮箱注册
- 验证码发送
- 登录态
- 修改密码

### 3.3 数据库设计

先读：

1. `AOBIDAO_WEB_SDD.md`
2. `ENGINEERING_BASELINE.md`
3. 原小程序业务代码与 README

写入目标：

- `DB_SCHEMA.md`

适用任务：

- 设计表
- 设计索引
- 设计约束
- 设计状态字段

### 3.4 接口设计

先读：

1. `AOBIDAO_WEB_SDD.md`
2. `DB_SCHEMA.md`
3. `ENGINEERING_BASELINE.md`
4. 原小程序相关页面逻辑

写入目标：

- `API_DESIGN.md`

适用任务：

- 设计分页接口
- 设计认证接口
- 设计上传接口
- 设计三大业务页接口

### 3.5 页面复刻

先读：

1. `AOBIDAO_WEB_SDD.md`
2. `PAGE_REPLICATION_CHECKLIST.md`
3. 原小程序对应页面代码

如涉及数据联动，再补读：

4. `API_DESIGN.md`

适用任务：

- 复刻我的仓库
- 复刻盈亏统计
- 复刻公开交易区
- 复刻关于页
- 复刻共享弹层、搜索条、分页条、悬浮按钮

### 3.6 上线部署

先读：

1. `AOBIDAO_WEB_SDD.md`
2. `ENGINEERING_BASELINE.md`
3. `PROJECT_STRUCTURE.md`

适用任务：

- Docker Compose
- Nginx
- HTTPS
- 环境变量
- 生产部署

### 3.7 排错与重构

先读：

1. `AOBIDAO_WEB_SDD.md`
2. 与问题最相关的专项文档
3. 相关代码

规则：

- 不要为了排查问题把所有文档全读一遍
- 只读与当前故障最相关的部分

## 4. 最小读取策略

为了减少幻觉和上下文膨胀，默认采用以下策略：

### 4.1 默认最小集

开始任何任务时，默认先只看：

1. `MASTER_SKILL_INDEX.md`
2. `AOBIDAO_WEB_SDD.md`

然后根据任务追加读取。

### 4.2 禁止整包加载

禁止一上来就把以下全部文档完整重复加载到上下文：

- `AOBIDAO_WEB_SDD.md`
- `PROJECT_STRUCTURE.md`
- `PAGE_REPLICATION_CHECKLIST.md`
- `ENGINEERING_BASELINE.md`
- `DB_SCHEMA.md`
- `API_DESIGN.md`

除非任务本身就是“审视和重构文档体系”。

### 4.3 优先读“最权威、最贴近任务”的那一份

例如：

- 改登录，不先去看页面复刻清单
- 改页面样式，不先去看部署文档
- 改目录结构，不先去看登录流程细节

## 5. 冲突处理

若文档冲突，按以下顺序处理：

1. `AOBIDAO_WEB_SDD.md`
2. `ENGINEERING_BASELINE.md`
3. `DB_SCHEMA.md` / `API_DESIGN.md`
4. `PROJECT_STRUCTURE.md`
5. `PAGE_REPLICATION_CHECKLIST.md`

若仍无法判断：

- 回到原小程序代码确认
- 必要时向用户说明差异

## 6. 开发时的引用要求

后续每次做重要开发任务时，AI 应在内部遵循：

- 先判断当前任务属于哪一类
- 再按本索引决定读取哪些文档
- 只用必要上下文工作

如果任务跨越多个领域，例如：

- “复刻公开交易区并接入真实接口”

则推荐读取顺序为：

1. `AOBIDAO_WEB_SDD.md`
2. `PAGE_REPLICATION_CHECKLIST.md`
3. `API_DESIGN.md`
4. 原小程序 `public-zone` 页面代码

## 7. 当前结论

本索引的目标不是增加一份新规范，而是减少无效阅读。

后续正确做法应是：

- 先看索引
- 再按任务精确读文档
- 不整包加载
- 不重复引用

这样能明显降低：

- 上下文爆炸
- 文档互相污染
- 规则记混
- 细节幻觉
