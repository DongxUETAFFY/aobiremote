# 性能风险接口改造清单

本文档记录当前项目中在压力测试下风险较高、建议优先改造的接口和代码路径。目标是先固化问题和改造方向，后续再逐个接口落地修改。

## 1. 总体判断

当前后端已经完成主要业务闭环，但部分接口仍是第一版实现方式：列表查询和统计查询耦合、写入后同步重算统计、批量接口循环调用单条逻辑、图片上传在请求线程内同步读写文件。

这些问题在小数据量和低并发下不明显，但在以下场景会被放大：

- 公开交易区数据达到 5 万条以上。
- 单个用户库存或交易记录达到几千条以上。
- 多用户同时刷新库存、盈亏、公开交易区列表。
- 多用户同时新增、删除、公开交易记录。
- 图片上传和图片预览并发增高。

## 2. 优先级说明

| 优先级 | 含义 |
| --- | --- |
| P1 | 压测时很可能首先暴露，建议先改 |
| P2 | 有明确风险，但可以在 P1 后改 |
| P3 | 中长期优化，可结合压测结果决定 |

## 3. 风险接口总览

| 优先级 | 接口 | 代码位置 | 主要问题 | 建议改法 |
| --- | --- | --- | --- | --- |
| P1 | `GET /api/inventory/page` | `InventoryItemServiceImpl#pageWarehouseItems` | 分页查询同步执行多次库存汇总 `SUM/COUNT` | 拆分 summary、缓存统计或合并聚合查询 |
| P1 | `GET /api/trade/page` | `TradeServiceImpl#pageTradeItems` | 分页查询同步执行多次盈亏汇总 `SUM/COUNT` | 拆分 summary、缓存统计或合并聚合查询 |
| P1 | `POST /api/trade` | `TradeServiceImpl#createTrade` | 新增交易后整用户重算统计 | 改成增量更新 `user_stats` |
| P1 | `PUT /api/trade/{id}` | `TradeServiceImpl#updateTrade` | 价格变化后整用户重算统计 | 用差值增量更新，避免全量聚合 |
| P1 | `DELETE /api/trade/{id}` | `TradeServiceImpl#deleteTrade` | 删除交易后整用户重算统计，并清理公开帖 | 用删除前快照做增量扣减 |
| P1 | `GET /api/public-post/page` | `PublicPostServiceImpl#pagePublicPosts` | 匿名高频入口，组合筛选和排序缺少组合索引支撑 | 增加组合索引，必要时加缓存 |
| P2 | `POST /api/trade/batch-delete` | `TradeServiceImpl#batchDeleteTradeItems` | 循环调用单条删除，重复重算统计 | 批量查、批量删、最后一次更新统计 |
| P2 | `POST /api/trade/batch-toggle-public` | `TradeServiceImpl#batchTogglePublic` | 循环公开/取消公开，多次查写 | 批量读取源记录，批量插入/删除公开帖 |
| P2 | `POST /api/inventory/batch-delete` | `InventoryItemServiceImpl#batchDeleteInventoryItems` | 循环调用单条删除，多次清理关联公开帖 | 批量清理公开帖和 flag，再批量删除库存 |
| P2 | `POST /api/inventory/batch-toggle-public` | `InventoryItemServiceImpl#batchTogglePublic` | 循环公开/取消公开，多次事务内查询 | 批量读取库存，批量处理公开状态 |
| P2 | `POST /api/files/images` | `FileAssetServiceImpl#uploadImage` | 事务内同步读取完整文件并写磁盘 | 缩短事务边界，限制上传并发，后续迁移对象存储 |
| P2 | `GET /api/files/{fileId}/preview` | `FileAssetServiceImpl#loadPreview` | 每次预览先查 DB 再读本地文件 | 公开图片交给 Nginx/CDN，私有图片保留鉴权 |

## 4. 详细问题与修改方案

### 4.1 库存分页接口

接口：

```http
GET /api/inventory/page
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/inventory/service/impl/InventoryItemServiceImpl.java
pageWarehouseItems
calculateSummary
sumWarehouseBuyPrice
countWarehouseItems
```

当前问题：

- 一次列表请求会执行分页查询。
- 分页后又调用 `calculateSummary`。
- `calculateSummary` 内部会额外执行多次 `SUM(buy_price)` 和 `COUNT`。
- 如果带 `keyword`，会使用 `LIKE item_name`，容易变成大范围扫描。
- 当前 `inventory_item` 只有部分单列或较弱组合索引，无法很好覆盖 `user_id + status + category + price + sort`。

压测风险：

- 多用户同时刷新仓库时，MySQL 会承受远高于列表本身的查询压力。
- 单用户库存数据越多，接口响应时间越不稳定。
- 带关键词搜索时更容易出现慢查询。

建议方案：

1. 第一阶段：给接口增加 `includeSummary` 参数，默认列表请求不返回 summary。
2. 第二阶段：新增独立接口，例如 `GET /api/inventory/summary`，只在页面初始化或筛选变化时调用。
3. 第三阶段：将多次汇总查询合并为一条条件聚合 SQL。
4. 第四阶段：补充组合索引。

建议索引方向：

```sql
CREATE INDEX idx_inventory_user_status_buy_time
ON inventory_item(user_id, status, buy_time DESC, created_at DESC);

CREATE INDEX idx_inventory_user_status_buy_price
ON inventory_item(user_id, status, buy_price DESC, created_at DESC);

CREATE INDEX idx_inventory_user_status_category
ON inventory_item(user_id, status, category);
```

后续如名称搜索成为核心能力，再考虑全文索引或独立物品字典表。

### 4.2 盈亏分页接口

接口：

```http
GET /api/trade/page
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/trade/service/impl/TradeServiceImpl.java
pageTradeItems
calculateSummary
sumByScope
sumByScopeAndCategory
countByScopeAndCategory
```

当前问题：

- 一次交易分页请求会执行分页查询。
- 同一个请求内又执行多次交易汇总查询。
- `scope=profit/loss` 使用 `profit_amount > 0` 或 `< 0`。
- `sortType=profitDesc` 会按 `profit_amount` 排序，但现有索引不能很好覆盖。
- 带关键词时同样存在 `LIKE item_name` 风险。

压测风险：

- 盈亏页是用户高频访问页，刷新一次等于多次数据库聚合。
- 当用户交易记录达到几千条以上，分页和汇总都会变慢。
- 大量用户同时访问时，聚合查询容易造成数据库 CPU 压力。

建议方案：

1. 参考库存页，拆分列表和 summary。
2. 优先复用 `user_stats` 中已经维护的全量统计。
3. 对筛选后的临时 summary，改成一条条件聚合 SQL。
4. 给交易查询补组合索引。

建议索引方向：

```sql
CREATE INDEX idx_trade_user_status_sell_time
ON inventory_item(user_id, status, sell_time DESC, updated_at DESC);

CREATE INDEX idx_trade_user_status_profit
ON inventory_item(user_id, status, profit_amount DESC, sell_time DESC);

CREATE INDEX idx_trade_user_status_category
ON inventory_item(user_id, status, category);
```

### 4.3 交易写入与统计重算

接口：

```http
POST /api/trade
PUT /api/trade/{id}
DELETE /api/trade/{id}
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/trade/service/impl/TradeServiceImpl.java
createTrade
updateTrade
deleteTrade
recalculateUserStats
```

当前问题：

- `createTrade` 插入一条交易后调用 `updateUserStatsAfterCreate`。
- `updateTrade` 在价格变化后调用 `updateUserStatsAfterUpdate`。
- `deleteTrade` 删除后调用 `updateUserStatsAfterDelete`。
- 这些方法最终都会进入 `recalculateUserStats`。
- `recalculateUserStats` 会对该用户所有 sold/unsold 记录做多次 `SUM/COUNT`。

压测风险：

- 单条写入成本随用户历史数据量增长。
- 用户记录越多，新增一条交易越慢。
- 批量删除时会重复触发全量重算，风险更大。

建议方案：

1. 新增交易：根据新记录直接增量更新 `user_stats`。
2. 修改交易：读取旧记录，计算新旧差值，增量更新。
3. 删除交易：删除前保留旧记录快照，按旧值扣减统计。
4. 保留一个后台或管理员用的“全量校准统计”方法，但不放在高频请求路径里。

增量更新示例思路：

```text
新增已售记录：
sold_count += 1
sold_buy_total += buy_price
sold_sell_total += sell_price
total_profit += max(profit_amount, 0)
total_loss += abs(min(profit_amount, 0))
```

```text
删除已售记录：
sold_count -= 1
sold_buy_total -= old_buy_price
sold_sell_total -= old_sell_price
total_profit -= max(old_profit_amount, 0)
total_loss -= abs(min(old_profit_amount, 0))
```

```text
修改已售记录：
sold_buy_total += new_buy_price - old_buy_price
sold_sell_total += new_sell_price - old_sell_price
total_profit += new_positive_profit - old_positive_profit
total_loss += new_abs_loss - old_abs_loss
```

### 4.4 公开交易区分页接口

接口：

```http
GET /api/public-post/page
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/publiczone/service/impl/PublicPostServiceImpl.java
pagePublicPosts
```

当前问题：

- 这是匿名可访问接口，天然比个人库存接口更容易被高频访问。
- 支持 `scope`、`direction`、`category`、`channel`、`keyword`、`priceRange`、`sortType` 组合查询。
- 当前 schema 里 `public_post` 主要是单列索引，例如 `trade_time`、`price`、`user_id`、`category`。
- 单列索引很难同时覆盖筛选和排序。
- MyBatis Plus `selectPage` 通常会执行列表 SQL 和 count SQL。

压测风险：

- 数据量上来后，公开区首页、翻页、筛选都会变慢。
- 匿名接口可能成为最高流量入口。
- 带关键词搜索时慢查询风险更高。

建议方案：

1. 先根据页面最常用排序补组合索引。
2. 对首页和热门筛选加短 TTL 缓存。
3. `keyword` 搜索后续迁移到物品字典或全文搜索。
4. 如果数据量继续增长，考虑游标分页替代深分页。

建议索引方向：

```sql
CREATE INDEX idx_public_post_trade_feed
ON public_post(trade_time DESC, created_at DESC);

CREATE INDEX idx_public_post_price_feed
ON public_post(price DESC, created_at DESC);

CREATE INDEX idx_public_post_filters_trade
ON public_post(category, direction, channel, trade_time DESC, created_at DESC);

CREATE INDEX idx_public_post_filters_price
ON public_post(category, direction, channel, price DESC, created_at DESC);

CREATE INDEX idx_public_post_user_trade
ON public_post(user_id, trade_time DESC, created_at DESC);
```

实际落地时需要结合 `EXPLAIN` 结果调整，避免盲目创建过多索引。

### 4.5 交易批量接口

接口：

```http
POST /api/trade/batch-delete
POST /api/trade/batch-toggle-public
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/trade/service/impl/TradeServiceImpl.java
batchDeleteTradeItems
batchTogglePublic
```

当前问题：

- `batchDeleteTradeItems` 循环调用 `deleteTrade`。
- `deleteTrade` 内部会单条查记录、清理公开帖、删除记录、重算统计。
- `batchTogglePublic` 循环读取每条记录，再调用公开区逻辑。
- 批量接口没有真正批量化。

压测风险：

- 批量删除 30 条可能放大成几十到上百次 SQL。
- 大事务时间变长，锁持有时间变长。
- 如果同时多人批量操作，数据库压力和锁等待会明显增加。

建议方案：

1. 批量读取当前用户的目标交易记录。
2. 一次性校验权限、状态和数量。
3. 批量查询关联公开帖。
4. 批量删除 `public_post_flag`。
5. 批量删除 `public_post`。
6. 批量删除 `inventory_item`。
7. 根据删除记录快照一次性增量更新 `user_stats`。

### 4.6 库存批量接口

接口：

```http
POST /api/inventory/batch-delete
POST /api/inventory/batch-toggle-public
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/inventory/service/impl/InventoryItemServiceImpl.java
batchDeleteInventoryItems
batchTogglePublic
```

当前问题：

- 批量删除循环调用单条删除。
- 单条删除会查 owned item，并清理关联公开帖。
- 批量公开/取消公开循环调用公开帖创建/删除逻辑。

压测风险：

- 批量操作越大，SQL 数量线性增长。
- 如果公开状态变更很多，会同时影响 `inventory_item`、`public_post`、`public_post_flag`。

建议方案：

1. 批量读取当前用户库存记录。
2. 校验全部是 `unsold`。
3. 批量处理关联公开帖。
4. 批量删除或批量更新库存公开状态。
5. 控制单次批量大小，例如最多 30 条。

### 4.7 图片上传接口

接口：

```http
POST /api/files/images
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/file/service/impl/FileAssetServiceImpl.java
uploadImage
```

当前问题：

- 在 `@Transactional` 方法里读取完整文件到 `byte[]`。
- 同步校验文件头。
- 同步创建目录并写入本地磁盘。
- 写完文件后插入 `file_asset`。
- 当前 600KB 限制让内存风险可控，但高并发上传时线程会被 IO 阻塞。

压测风险：

- 上传并发上来后，Tomcat 工作线程容易被文件 IO 占用。
- 磁盘写入抖动会直接影响接口响应时间。
- 如果 DB 插入失败，已写入文件可能成为孤儿文件。

建议方案：

1. 保留 600KB 限制，并增加上传频率限制。
2. 先将文件写到临时路径，校验成功后短事务插入 DB。
3. DB 插入成功后再移动到正式路径，或失败时清理临时文件。
4. 中长期迁移到对象存储或 MinIO，并让后端只保存元数据。
5. 压测时单独测上传链路，不和普通接口混在一起。

### 4.8 图片预览接口

接口：

```http
GET /api/files/{fileId}/preview
```

代码：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/file/service/impl/FileAssetServiceImpl.java
loadPreview
```

当前问题：

- 每次预览都先查 `file_asset`。
- 公开图片和私有图片都经后端读取本地文件。
- 公开图片虽然设置了缓存头，但首轮请求仍会打到应用和数据库。

压测风险：

- 公开交易区如果大量显示图片，图片预览会带来额外 DB 查询和文件 IO。
- 图片请求数量通常远高于 API 请求数量。

建议方案：

1. 公开图片使用 Nginx 静态目录、对象存储或 CDN。
2. 后端返回可直接访问的公开 URL。
3. 私有图片继续走后端鉴权。
4. 对 `file_asset` 元数据加本地缓存或 Redis 缓存。

## 5. 建议改造顺序

### 第一轮：先保护高频读接口

1. 改 `GET /api/public-post/page` 的索引和查询策略。
2. 改 `GET /api/inventory/page`，拆出或可选关闭 summary。
3. 改 `GET /api/trade/page`，拆出或可选关闭 summary。

### 第二轮：降低写接口数据库压力

1. 改 `POST /api/trade` 为增量更新统计。
2. 改 `PUT /api/trade/{id}` 为差值更新统计。
3. 改 `DELETE /api/trade/{id}` 为删除快照扣减统计。
4. 保留全量统计校准工具，但不要在高频请求中调用。

### 第三轮：处理批量接口

1. 改 `POST /api/trade/batch-delete`。
2. 改 `POST /api/trade/batch-toggle-public`。
3. 改 `POST /api/inventory/batch-delete`。
4. 改 `POST /api/inventory/batch-toggle-public`。

### 第四轮：图片链路优化

1. 上传接口缩短事务边界。
2. 增加上传并发和频率限制。
3. 公开图片改为静态资源或对象存储直出。
4. 私有图片保留后端鉴权。

## 6. 压测前建议补充的观测指标

- JVM CPU、内存、GC。
- Tomcat 当前线程数、忙线程数。
- Hikari 连接池活跃连接、等待连接。
- MySQL 慢查询日志。
- MySQL `EXPLAIN` 公开区、库存、盈亏分页 SQL。
- 磁盘 IO 和上传目录增长速度。
- 接口 p50、p95、p99、错误率。

## 7. 暂不建议优先压测的接口

以下接口更偏安全和功能验证，不建议作为普通吞吐压测主场景：

```http
POST /api/auth/send-register-code
POST /api/auth/send-reset-password-code
POST /api/auth/register
POST /api/auth/reset-password
```

原因：

- 生产环境会涉及 SMTP 邮件发送。
- 本身应该有限流，不应追求高 QPS。
- 压测这些接口容易污染验证码缓存和邮件服务。

建议只做低频安全验证和限流验证。

## 8. 后续落地记录

后续每完成一个接口改造，在这里补充状态。

| 接口 | 状态 | 备注 |
| --- | --- | --- |
| `GET /api/public-post/page` | 已完成第一步 | 已补公开区分页/筛选/排序组合索引，后续压测后再决定是否加缓存或改分页策略 |
| `GET /api/inventory/page` | 已完成第一步 | 已将库存 summary 从 5 次聚合查询合并为 1 次条件聚合查询，并补充库存分页/筛选/排序组合索引；接口功能和返回结构不变 |
| `GET /api/trade/page` | 已完成第一步 | 已将盈亏 summary 从多次聚合查询合并为 1 次条件聚合查询，并补充盈亏分页/筛选/排序组合索引；接口功能和返回结构不变 |
| `POST /api/trade` | 已完成第一步 | 新增交易已改为基于新记录快照增量更新 `user_stats`，统计行缺失时回退全量校准；接口功能和返回结构不变 |
| `PUT /api/trade/{id}` | 已完成第一步 | 价格变化时已改为根据新旧交易快照差值增量更新 `user_stats`，统计行缺失时回退全量校准；接口功能和返回结构不变 |
| `DELETE /api/trade/{id}` | 已完成第一步 | 删除交易后已改为根据被删记录快照扣减 `user_stats`，统计行缺失时回退全量校准；接口功能、公开帖清理和返回结构不变 |
| `POST /api/trade/batch-delete` | 已完成第一步 | 已改为批量读取并按原 ids 顺序校验、批量清理公开帖和 flag、批量删除交易记录，并一次性扣减 `user_stats`；接口功能和返回结构不变 |
| `POST /api/trade/batch-toggle-public` | 已完成第一步 | 已改为批量读取并校验交易记录，公开区服务新增批量切换入口以复用公开/取消公开规则并只做一次防重复提交校验；接口功能和返回结构不变 |
| `POST /api/inventory/batch-delete` | 已完成第一步 | 已改为批量读取并按原 ids 顺序校验、批量清理公开帖和 flag、批量删除库存记录；接口功能和返回结构不变 |
| `POST /api/inventory/batch-toggle-public` | 已完成第一步 | 已改为批量读取并校验库存记录，复用公开区批量切换入口并只做一次防重复提交校验；接口功能和返回结构不变 |
| `POST /api/files/images` | 已完成第一步 | 已移除上传方法大事务，文件校验和磁盘写入在 DB 事务外完成，DB 插入使用短事务，失败时清理已写入文件；接口功能和返回结构不变 |
| `GET /api/files/{fileId}/preview` | 已完成第一步 | 私有图片仍每次查 DB 并校验权限，公开图片预览增加本地元数据缓存以减少重复 DB 查询；接口功能、响应头和鉴权语义不变 |
