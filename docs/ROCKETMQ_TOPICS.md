# RocketMQ 约定（RocketMQ 4.9.x）

## 1. order.created
- Topic：`order.created`
- Tag：`v1`
- ProducerGroup：`ecommerce-backend-producer`
- ConsumerGroup：`ecommerce-backend-consumer`

## 2. 消息体（JSON）
```json
{
  "orderId": 10001,
  "orderNo": "20260409ABCDEF12",
  "userId": 1,
  "totalCents": 9999,
  "currency": "USD",
  "createdAt": "2026-04-09T12:00:00Z"
}
```

## 3. 幂等建议
- 消费者侧以 `orderId` 为幂等键（可记录 DB/Redis set）
- 失败重试：消费失败返回 RECONSUME_LATER（需监控死信/重试次数）