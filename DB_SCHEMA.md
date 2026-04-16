# 数据库设计文档

本文档定义 `aobidao-web` 的第一版数据库结构。

目标：

- 尽量保留原小程序业务语义
- 适配 `MySQL 8`
- 便于后端单体服务开发
- 便于后续上线维护

## 1. 设计原则

当前数据库设计遵循：

1. 优先保留原业务语义，不做过度抽象
2. 优先保证三大业务页联动简单、可维护
3. 优先让统计、公开状态、删除回写等逻辑容易实现
4. 图片本体不入库，只存元数据和对象路径

## 2. 原小程序集合到 Web 表的映射

原集合：

- `users`
- `user_stats`
- `inventory_items`
- `public_posts`
- `public_post_flags`
- `mutation_guards`
- `feedback`

Web 版建议映射为：

- `user_account`
- `user_stats`
- `inventory_item`
- `public_post`
- `public_post_flag`
- `operation_guard`
- `feedback`
- `file_asset`

说明：

- 原小程序把未卖出与已卖出记录都放在 `inventory_items`
- Web 版继续沿用这个思路，只使用一张 `inventory_item` 表，通过 `status` 区分
- 这样最接近现有业务，也最利于公开状态联动和统计同步

## 3. 核心枚举约定

### 3.1 时装分类 `category`

- `magic`
- `obi`

### 3.2 渠道 `channel`

- `xianyu`
- `tieba`
- `other`

### 3.3 库存状态 `status`

- `unsold`
- `sold`

### 3.4 公开区交易方向 `direction`

- `buy`
- `sell`

### 3.5 用户状态 `status`

- `active`
- `disabled`

## 4. 表结构

## 4.1 `user_account`

用途：

- 保存站内账号
- 替代原 `users` 集合与微信 `openid` 身份

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint PK | 用户主键 |
| `email` | varchar(128) | 登录邮箱，唯一 |
| `password_hash` | varchar(255) | BCrypt 密码哈希 |
| `nickname` | varchar(40) | 昵称 |
| `avatar_url` | varchar(255) | 头像 URL，可为空 |
| `status` | varchar(20) | `active / disabled` |
| `last_login_at` | datetime | 最近登录时间 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

索引：

- 唯一索引：`uk_user_account_email(email)`
- 普通索引：`idx_user_account_status(status)`

## 4.2 `user_stats`

用途：

- 保存用户汇总统计
- 对应原 `user_stats`

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `user_id` | bigint PK | 对应 `user_account.id` |
| `total_profit` | decimal(12,2) | 总盈利 |
| `total_loss` | decimal(12,2) | 总亏损 |
| `sold_count` | int | 已卖出数量 |
| `sold_buy_total` | decimal(12,2) | 已卖出总买入 |
| `sold_sell_total` | decimal(12,2) | 已卖出总卖出 |
| `unsold_count` | int | 未卖出数量 |
| `unsold_buy_total` | decimal(12,2) | 未卖出总买入 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

说明：

- 该表可作为业务汇总缓存
- 真实统计口径仍应以 `inventory_item` 为准

## 4.3 `inventory_item`

用途：

- 保存仓库记录与已卖出记录
- 对应原 `inventory_items`
- 支撑“我的仓库”和“盈亏统计”两页

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint PK | 记录主键 |
| `user_id` | bigint | 所属用户 |
| `item_name` | varchar(40) | 物品名称 |
| `buy_price` | decimal(12,2) | 买入价格 |
| `buy_time` | date | 买入日期 |
| `sell_price` | decimal(12,2) | 卖出价格，未卖出可空 |
| `sell_time` | date | 卖出日期，未卖出可空 |
| `trade_time` | date | 交易时间，已卖出时等于 `sell_time` |
| `channel` | varchar(20) | 渠道 |
| `category` | varchar(20) | 分类 |
| `status` | varchar(20) | `unsold / sold` |
| `profit_amount` | decimal(12,2) | 盈亏金额，已卖出时有效 |
| `public_posted` | tinyint(1) | 是否已公开 |
| `public_post_id` | bigint | 关联公开记录 ID |
| `public_posted_at` | datetime | 公开时间 |
| `remark` | varchar(60) | 备注，业务限制 15 字 |
| `image_file_id` | varchar(255) | 对象存储文件标识 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

索引建议：

- `idx_inventory_user_status(user_id, status)`
- `idx_inventory_user_buy_time(user_id, buy_time desc)`
- `idx_inventory_user_buy_price(user_id, buy_price desc)`
- `idx_inventory_user_public(user_id, public_posted)`
- `idx_inventory_public_post_id(public_post_id)`

说明：

- 手动录入“已完成交易”时，直接插入 `status = sold`
- 仓库卖出时，原记录从 `unsold` 更新为 `sold`
- 这样可以最大程度贴近小程序当前实现

## 4.4 `public_post`

用途：

- 保存公开交易区记录
- 对应原 `public_posts`

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint PK | 公开记录主键 |
| `user_id` | bigint | 发布人 |
| `source_inventory_item_id` | bigint | 来源私有记录，可空 |
| `item_name` | varchar(40) | 物品名称 |
| `price` | decimal(12,2) | 价格 |
| `trade_time` | date | 交易时间 |
| `direction` | varchar(20) | `buy / sell` |
| `channel` | varchar(20) | 渠道 |
| `category` | varchar(20) | 分类 |
| `remark` | varchar(60) | 备注 |
| `image_file_id` | varchar(255) | 图片标识 |
| `untrusted_count` | int | 不可信计数 |
| `publisher_name` | varchar(40) | 展示用昵称快照 |
| `publisher_avatar` | varchar(255) | 展示用头像快照 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

索引建议：

- `idx_public_post_trade_time(trade_time desc)`
- `idx_public_post_price(price desc)`
- `idx_public_post_user(user_id)`
- `idx_public_post_source_item(source_inventory_item_id)`
- `idx_public_post_category(category)`

说明：

- 若来源于私有记录，则 `source_inventory_item_id` 必填
- 若是公开区独立发布，则该字段为空

## 4.5 `public_post_flag`

用途：

- 保存用户对公开记录的“不可信”标记
- 对应原 `public_post_flags`

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint PK | 主键 |
| `user_id` | bigint | 标记者 |
| `post_id` | bigint | 公开记录 ID |
| `created_at` | datetime | 创建时间 |

约束与索引：

- 唯一索引：`uk_public_post_flag_user_post(user_id, post_id)`
- 普通索引：`idx_public_post_flag_post(post_id)`

说明：

- 用唯一索引确保同一用户不能重复标记同一条记录

## 4.6 `operation_guard`

用途：

- 保存防重复提交、防频繁操作的保护信息
- 对应原 `mutation_guards`

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint PK | 主键 |
| `user_id` | bigint | 所属用户 |
| `action` | varchar(64) | 动作名 |
| `guard_key` | varchar(128) | 唯一防重键 |
| `last_request_id` | varchar(128) | 最近一次请求 ID |
| `last_submit_at` | datetime | 最近提交时间 |
| `recent_times_json` | text | 最近时间窗口，可先 JSON 存储 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

索引建议：

- 唯一索引：`uk_operation_guard_guard_key(guard_key)`
- 普通索引：`idx_operation_guard_user_action(user_id, action)`

说明：

- 第一版允许 `recent_times_json` 先用 JSON 文本存
- 后续若需要更强可观测性，可拆成独立操作日志表

## 4.7 `feedback`

用途：

- 保存用户反馈
- 对应原 `feedback`

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint PK | 主键 |
| `user_id` | bigint | 提交用户 |
| `content` | varchar(500) | 反馈内容 |
| `submit_date` | date | 提交日期 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

索引建议：

- `idx_feedback_user(user_id)`
- `idx_feedback_submit_date(submit_date desc)`

## 4.8 `file_asset`

用途：

- 保存文件元数据
- 为图片上传提供统一资产表

关键字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint PK | 主键 |
| `user_id` | bigint | 上传用户 |
| `storage_provider` | varchar(30) | 当前默认 `minio` |
| `bucket_name` | varchar(100) | bucket |
| `object_key` | varchar(255) | 对象路径 |
| `original_name` | varchar(255) | 原文件名 |
| `content_type` | varchar(100) | MIME 类型 |
| `file_size` | bigint | 文件大小 |
| `is_public` | tinyint(1) | 是否公开访问 |
| `created_at` | datetime | 创建时间 |
| `updated_at` | datetime | 更新时间 |

索引建议：

- `idx_file_asset_user(user_id)`
- `idx_file_asset_object_key(object_key)`

说明：

- 第一版业务表可继续直接存 `image_file_id`
- 若后续统一文件管理，可逐步迁到 `file_asset.id`

## 5. 第一版不落库的内容

以下内容第一版不强制建表：

- 邮箱验证码
  - 直接存 Redis
- 登录失败计数
  - 可先存 Redis

理由：

- 这些更适合做短期缓存和限流控制
- 不必一开始就持久化到 MySQL

## 6. 关键关系说明

核心关系如下：

- `user_account 1 - 1 user_stats`
- `user_account 1 - n inventory_item`
- `user_account 1 - n public_post`
- `user_account 1 - n feedback`
- `public_post 1 - n public_post_flag`
- `inventory_item 0..1 - 1 public_post`

说明：

- 一个私有记录最多关联一个当前有效公开记录
- 一个公开记录可以来源于私有记录，也可以是公开区独立发布

## 7. 关键业务约束

必须通过数据库约束 + 业务层共同保证：

1. 邮箱唯一
2. 同一用户不可重复标记同一公开记录“不可信”
3. 私有记录公开状态与 `public_post_id` 要一致
4. 已卖出记录必须有 `sell_price / sell_time / profit_amount`
5. 未卖出记录不应强制要求 `sell_price / sell_time`

## 8. 当前结论

当前第一版数据库设计采用：

- 一个用户表
- 一个统计表
- 一个库存 / 交易统一表
- 一个公开记录表
- 一个公开标记表
- 一个防重保护表
- 一个反馈表
- 一个文件资产表

这套结构足以支撑：

- 登录注册体系
- 我的仓库
- 盈亏统计
- 公开交易区
- 统计汇总
- 图片上传
- 防重复提交与频繁操作保护
