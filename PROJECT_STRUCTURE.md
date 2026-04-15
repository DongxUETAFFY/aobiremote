# Web 版项目目录规划

本文档只回答一个问题：

`aobidao-web` 应该如何组织目录，才能便于前后端开发、部署和后续维护。

## 1. 根目录规划

推荐结构：

```text
E:\AAAwechatBeifen\aobidao-web
├─ frontend/                  # Vue 3 前端工程
├─ backend/                   # Spring Boot 后端工程
├─ docs/                      # 设计与说明文档
├─ deploy/                    # Docker / Nginx / 环境示例
├─ sql/                       # 初始化与迁移脚本
├─ scripts/                   # 本地辅助脚本
├─ README.md
└─ AOBIDAO_WEB_SDD.md
```

说明：

- 根目录只保留最核心入口文件
- 设计文档最终都应收敛进 `docs/`

## 2. 各目录职责

- `frontend/`
  - 页面、交互、状态管理、接口请求、样式复刻
- `backend/`
  - 认证、业务逻辑、文件上传、数据库访问、缓存控制
- `docs/`
  - 方案、接口、表结构、复刻清单、迁移说明
- `deploy/`
  - Docker Compose、Nginx、Dockerfile、环境示例
- `sql/`
  - 初始化 SQL、索引、迁移脚本
- `scripts/`
  - 本地开发辅助脚本

## 3. frontend 结构

建议结构：

```text
frontend/
├─ public/
├─ src/
│  ├─ api/
│  ├─ assets/
│  ├─ components/
│  ├─ layout/
│  ├─ router/
│  ├─ stores/
│  ├─ styles/
│  ├─ types/
│  ├─ utils/
│  ├─ views/
│  ├─ App.vue
│  └─ main.ts
├─ index.html
├─ package.json
├─ tsconfig.json
├─ vite.config.ts
└─ .env.example
```

### 3.1 views 规划

```text
frontend/src/views/
├─ auth/
│  ├─ LoginView.vue
│  ├─ RegisterView.vue
│  └─ ChangePasswordView.vue
├─ warehouse/
│  └─ WarehouseView.vue
├─ profit/
│  └─ ProfitView.vue
├─ public-zone/
│  └─ PublicZoneView.vue
├─ about/
│  └─ AboutView.vue
└─ not-found/
   └─ NotFoundView.vue
```

### 3.2 components 规划

建议优先拆这几类：

```text
frontend/src/components/
├─ app/
├─ common/
├─ inventory/
├─ trade/
└─ public-zone/
```

组件原则：

- 先按业务语义拆
- 重复两次以上再抽
- 不过早做全局抽象

## 4. backend 结构

建议结构：

```text
backend/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/yourdomain/aobidao/
│  │  │     ├─ AobidaoWebApplication.java
│  │  │     ├─ common/
│  │  │     ├─ config/
│  │  │     ├─ auth/
│  │  │     ├─ user/
│  │  │     ├─ inventory/
│  │  │     ├─ trade/
│  │  │     ├─ publiczone/
│  │  │     ├─ file/
│  │  │     └─ stats/
│  │  └─ resources/
│  │     ├─ mapper/
│  │     ├─ application.yml
│  │     ├─ application-dev.yml
│  │     ├─ application-prod.yml
│  │     └─ db/
│  └─ test/
├─ pom.xml
└─ .env.example
```

要求：

- 不再使用 `com.example`
- 正式开发前定下真实包名

### 4.1 模块内部结构

以 `inventory` 为例：

```text
inventory/
├─ controller/
├─ service/
├─ service/impl/
├─ mapper/
├─ entity/
├─ dto/
├─ vo/
├─ convert/
└─ enums/
```

职责：

- `controller`：收参与返回
- `service`：业务逻辑
- `mapper`：数据库访问
- `entity`：表映射
- `dto`：请求对象
- `vo`：响应对象
- `convert`：对象转换
- `enums`：枚举

## 5. docs 规划

建议至少包含：

```text
docs/
├─ WEB_VERSION_PLAN_A.md
├─ PROJECT_STRUCTURE.md
├─ ENGINEERING_BASELINE.md
├─ DB_SCHEMA.md
├─ API_DESIGN.md
├─ PAGE_REPLICATION_CHECKLIST.md
└─ MIGRATION_NOTES.md
```

## 6. deploy 规划

```text
deploy/
├─ docker-compose.yml
├─ nginx/
│  └─ default.conf
├─ backend/
│  └─ Dockerfile
├─ frontend/
│  └─ Dockerfile
└─ env/
   ├─ dev.env.example
   └─ prod.env.example
```

## 7. sql 规划

```text
sql/
├─ init/
│  ├─ 001_create_tables.sql
│  ├─ 002_create_indexes.sql
│  └─ 003_seed_data.sql
└─ migration/
   ├─ V1__init.sql
   └─ V2__alter_xxx.sql
```

建议：

- 初始化脚本与迁移脚本分开
- 后续接入 `Flyway`

## 8. 第一阶段落地顺序

建议顺序：

1. 创建 `frontend/`
2. 创建 `backend/`
3. 创建 `docs/`
4. 创建 `deploy/`
5. 创建 `sql/`
6. 创建 `scripts/`
7. 搭前端脚手架
8. 搭后端脚手架
9. 再补接口与数据库文档

## 9. 当前结论

当前项目应按以下原则组织：

- 前后端分离
- 单体后端
- 文档先行
- 目录清晰分层
- 先搭骨架，再迁移业务
