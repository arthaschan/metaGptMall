# Redis Key 约定

## 1. 前缀
- 本项目统一前缀：`ecom:`

## 2. 商品详情缓存
- Key：`ecom:product:{productId}`
- Value：JSON（ProductDto）
- TTL：60 秒（可通过 `app.redis.productTtlSeconds` 修改）

字段建议：
- id, title, description, priceCents, currency, stock, imageUrl, active

## 3. 失效策略（MVP）
- 商品后台更新时：删除对应 key（后续实现 admin ��块时补）