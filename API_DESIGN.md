# 接口设计文档

本文档定义 `aobidao-web` 第一版 REST API。

目标：

- 保留原小程序业务语义
- 转换为适合前后端分离的 HTTP 接口
- 让前后端可以并行开发

## 1. 设计原则

当前接口设计遵循：

1. 优先覆盖登录、三大业务页、图片上传
2. 保留原小程序的动作语义，但改成 REST 风格
3. 明确鉴权要求、请求参数、返回结构
4. 第一版先保证闭环，不追求过度抽象

## 2. 接口前缀与鉴权方式

统一前缀：

- `/api`

统一鉴权：

- 使用 `Sa-Token`
- 通过请求头携带 token

请求头：

```http
Authorization: Bearer <token>
```

## 3. 统一响应结构

成功响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

失败响应：

```json
{
  "code": 1,
  "message": "错误信息",
  "data": null
}
```

说明：

- 业务失败统一返回 `code != 0`
- HTTP 状态码仍应按语义使用
- 参数错误、未登录、无权限等不应全部返回 200

## 4. 通用错误码建议

建议第一版统一以下业务码：

| code | 含义 |
| --- | --- |
| `0` | 成功 |
| `1001` | 参数错误 |
| `1002` | 未登录 |
| `1003` | 无权限 |
| `1004` | 记录不存在 |
| `1005` | 重复提交 |
| `1006` | 操作过于频繁 |
| `1007` | 状态不合法 |
| `1008` | 上传失败 |
| `1009` | 验证码错误或失效 |
| `1010` | 登录失败 |
| `1011` | 邮箱已注册 |
| `1012` | 文件类型不支持 |

## 5. 认证模块

## 5.1 发送注册验证码

`POST /api/auth/send-register-code`

鉴权：

- 不需要登录

请求体：

```json
{
  "email": "user@example.com"
}
```

返回：

```json
{
  "code": 0,
  "message": "验证码已发送",
  "data": null
}
```

## 5.2 注册

`POST /api/auth/register`

鉴权：

- 不需要登录

请求体：

```json
{
  "email": "user@example.com",
  "code": "123456",
  "password": "abc12345"
}
```

返回：

```json
{
  "code": 0,
  "message": "注册成功",
  "data": {
    "userId": 1
  }
}
```

## 5.3 登录

`POST /api/auth/login`

鉴权：

- 不需要登录

请求体：

```json
{
  "email": "user@example.com",
  "password": "abc12345"
}
```

返回：

```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "xxx",
    "tokenName": "Authorization",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "nickname": "奥比玩家"
    }
  }
}
```

## 5.4 退出登录

`POST /api/auth/logout`

鉴权：

- 需要登录

返回：

```json
{
  "code": 0,
  "message": "已退出登录",
  "data": null
}
```

## 5.5 修改密码

`POST /api/auth/change-password`

鉴权：

- 需要登录

请求体：

```json
{
  "oldPassword": "abc12345",
  "newPassword": "new123456"
}
```

返回：

```json
{
  "code": 0,
  "message": "密码修改成功",
  "data": null
}
```

## 5.6 当前登录用户信息

`GET /api/auth/me`

鉴权：

- 需要登录

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "奥比玩家",
    "avatarUrl": ""
  }
}
```

## 6. 我的仓库模块

## 6.1 仓库分页列表

`GET /api/inventory/page`

鉴权：

- 需要登录

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `pageNo` | 否 | 页码，默认 1 |
| `pageSize` | 否 | 每页大小 |
| `keyword` | 否 | 名称搜索 |
| `priceRange` | 否 | 价格区间，如 `0-10` |
| `sortType` | 否 | `buyTimeDesc / buyPriceDesc` |

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "items": [],
    "pageNo": 1,
    "pageSize": 10,
    "totalCount": 0,
    "totalPages": 1,
    "hasMore": false,
    "summary": {
      "totalBuyPrice": "0.00"
    }
  }
}
```

## 6.2 创建仓库记录

`POST /api/inventory`

鉴权：

- 需要登录

请求体：

```json
{
  "itemName": "龙娃惊讶",
  "buyPrice": 12.5,
  "buyTime": "2026-04-15",
  "channel": "xianyu",
  "category": "obi",
  "remark": "备注",
  "imageFileId": "xxx",
  "requestId": "req_xxx"
}
```

返回：

```json
{
  "code": 0,
  "message": "创建成功",
  "data": {
    "itemId": 1001
  }
}
```

## 6.3 更新仓库记录

`PUT /api/inventory/{id}`

鉴权：

- 需要登录

请求体字段与创建一致。

## 6.4 标记卖出

`POST /api/inventory/{id}/mark-sold`

鉴权：

- 需要登录

请求体：

```json
{
  "sellPrice": 18.0,
  "sellTime": "2026-04-15",
  "requestId": "req_xxx"
}
```

返回：

```json
{
  "code": 0,
  "message": "已标记卖出",
  "data": {
    "itemId": 1001
  }
}
```

## 6.5 删除记录

`DELETE /api/inventory/{id}`

鉴权：

- 需要登录

请求体或查询中需带：

- `requestId`

说明：

- 可删除未卖出或已卖出记录
- 删除时需同步处理统计和公开状态

## 6.6 详情

`GET /api/inventory/{id}`

鉴权：

- 需要登录

返回：

- 当前用户自己的记录详情

## 6.7 切换公开状态

`POST /api/inventory/{id}/toggle-public`

鉴权：

- 需要登录

请求体：

```json
{
  "price": 20.0,
  "tradeTime": "2026-04-15",
  "direction": "buy",
  "remark": "备注",
  "imageFileId": "xxx",
  "requestId": "req_xxx"
}
```

说明：

- 若当前未公开，则创建公开记录
- 若当前已公开，则取消公开
- 最终由后端决定是“公开”还是“取消公开”

返回：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "publicPosted": true,
    "postId": 2001
  }
}
```

## 7. 盈亏统计模块

## 7.1 分页列表

`GET /api/trade/page`

鉴权：

- 需要登录

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `pageNo` | 否 | 页码 |
| `pageSize` | 否 | 每页大小 |
| `scope` | 否 | `all / profit / loss` |

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "items": [],
    "pageNo": 1,
    "pageSize": 10,
    "totalCount": 0,
    "totalPages": 1,
    "hasMore": false,
    "summary": {
      "totalBuyAmount": "0.00",
      "totalSellAmount": "0.00",
      "totalProfit": "0.00",
      "totalLoss": "0.00"
    }
  }
}
```

## 7.2 创建已完成交易

`POST /api/trade`

鉴权：

- 需要登录

请求体：

```json
{
  "itemName": "龙娃惊讶",
  "buyPrice": 12.5,
  "buyTime": "2026-04-10",
  "sellPrice": 18.0,
  "sellTime": "2026-04-15",
  "channel": "xianyu",
  "category": "obi",
  "remark": "备注",
  "imageFileId": "xxx",
  "requestId": "req_xxx"
}
```

## 7.3 更新已卖出记录

`PUT /api/trade/{id}`

鉴权：

- 需要登录

请求体字段与创建基本一致。

## 7.4 删除已卖出记录

`DELETE /api/trade/{id}`

鉴权：

- 需要登录

需带 `requestId`。

## 7.5 详情

`GET /api/trade/{id}`

鉴权：

- 需要登录

## 7.6 切换公开状态

`POST /api/trade/{id}/toggle-public`

鉴权：

- 需要登录

请求体与仓库切换公开类似。

## 8. 公开交易区模块

## 8.1 分页列表

`GET /api/public-post/page`

鉴权：

- 可匿名访问

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `pageNo` | 否 | 页码 |
| `pageSize` | 否 | 每页大小 |
| `scope` | 否 | `all / mine` |
| `direction` | 否 | `buy / sell`（买入 / 卖出） |
| `keyword` | 否 | 搜索名称 |
| `category` | 否 | `magic / obi` |
| `priceRange` | 否 | 预设价格区间 |
| `minPrice` | 否 | 最低价 |
| `maxPrice` | 否 | 最高价 |
| `sortType` | 否 | `tradeTimeDesc / priceDesc` |

说明：

- `scope=mine` 时必须登录

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "items": [],
    "pageNo": 1,
    "pageSize": 10,
    "totalCount": 0,
    "totalPages": 1,
    "hasMore": false
  }
}
```

## 8.2 独立创建公开记录

`POST /api/public-post`

鉴权：

- 需要登录

请求体：

```json
{
  "itemName": "龙娃惊讶",
  "price": 20.0,
  "tradeTime": "2026-04-15",
  "direction": "sell",
  "channel": "xianyu",
  "category": "obi",
  "remark": "备注",
  "imageFileId": "xxx",
  "requestId": "req_xxx"
}
```

## 8.3 更新公开记录

`PUT /api/public-post/{id}`

鉴权：

- 需要登录

## 8.4 删除公开记录

`DELETE /api/public-post/{id}`

鉴权：

- 需要登录

说明：

- 若该公开记录来源于私有记录，删除后需回写私有页公开状态

## 8.5 切换不可信标记

`POST /api/public-post/{id}/toggle-untrusted`

鉴权：

- 需要登录

请求体：

```json
{
  "requestId": "req_xxx"
}
```

返回：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "postId": 2001,
    "flagged": true
  }
}
```

## 9. 文件模块

## 9.1 上传图片

`POST /api/file/upload`

鉴权：

- 需要登录

请求方式：

- `multipart/form-data`

表单字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `file` | 是 | 图片文件 |
| `scene` | 是 | `private-images / public-images` |

返回：

```json
{
  "code": 0,
  "message": "上传成功",
  "data": {
    "fileId": "minio://bucket/object-key",
    "url": "https://...",
    "objectKey": "private-images/1/xxx.png"
  }
}
```

## 9.2 删除文件

`DELETE /api/file/{fileId}`

鉴权：

- 需要登录

说明：

- 第一版可作为保留接口
- 真正删除时需校验文件归属与引用关系

## 10. 反馈模块

## 10.1 提交反馈

`POST /api/feedback`

鉴权：

- 需要登录

请求体：

```json
{
  "content": "这里是反馈内容",
  "requestId": "req_xxx"
}
```

返回：

```json
{
  "code": 0,
  "message": "提交成功",
  "data": {
    "feedbackId": 3001
  }
}
```

说明：

- 当前前端未接入，但后端接口建议预留

## 11. 健康检查

`GET /api/health`

鉴权：

- 不需要登录

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "status": "UP"
  }
}
```

## 12. 与原小程序动作的映射

### 原 `tradeService`

- `listWarehouseItems` -> `GET /api/inventory/page`
- `createWarehouseItem` -> `POST /api/inventory`
- `updateWarehouseItem` -> `PUT /api/inventory/{id}`
- `markItemSold` -> `POST /api/inventory/{id}/mark-sold`
- `createCompletedTrade` -> `POST /api/trade`
- `updateSoldItem` -> `PUT /api/trade/{id}`
- `deleteItem` -> `DELETE /api/inventory/{id}` 或 `DELETE /api/trade/{id}`
- `listProfitItems` -> `GET /api/trade/page`
- `getItemDetail` -> `GET /api/inventory/{id}` / `GET /api/trade/{id}`

### 原 `publicZone`

- `listPosts` -> `GET /api/public-post/page`
- `createPost` -> `POST /api/public-post`
- `toggleSourceItemPublicPost` -> `POST /api/inventory/{id}/toggle-public` / `POST /api/trade/{id}/toggle-public`
- `updatePost` -> `PUT /api/public-post/{id}`
- `deletePost` -> `DELETE /api/public-post/{id}`
- `togglePostUntrusted` -> `POST /api/public-post/{id}/toggle-untrusted`

## 13. 当前结论

当前第一版 API 已经覆盖：

- 认证闭环
- 我的仓库
- 盈亏统计
- 公开交易区
- 图片上传
- 反馈预留
- 健康检查

这份文档足以支撑下一步：

- 搭前后端脚手架
- 建 controller/service 空壳
- 建前端 `api` 请求层
