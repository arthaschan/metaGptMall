# 用户重置密码需求

本文档定义当前仓库中“用户重置密码”功能的最小可实现版本。

## 1. 目标

在当前本地基线下，实现一个可运行、可测试、可由 MetaGPT 生成的密码重置能力。

当前本地基线：

- 后端：Spring Boot 3.x + H2 + MyBatis
- 前端：Vue 3 + Vite
- 不依赖邮件、短信、验证码、Redis、RocketMQ

## 2. 当前采用的最小实现

当前不做“忘记密码邮件找回平台”。

当前只实现本地最小版本：

- 用户提交邮箱
- 用户输入新密码
- 用户再次确认新密码
- 后端校验后，直接更新该用户的 `password_hash`

## 3. 接口契约

- `POST /api/auth/password/reset`

Request:

```json
{
  "email": "user@example.com",
  "newPassword": "new-password-123",
  "confirmPassword": "new-password-123"
}
```

Success Response:

```json
{
  "message": "Password reset successful",
  "email": "user@example.com"
}
```

## 4. 业务规则

- `email` 必填，且格式合法。
- `newPassword` 必填，长度至少 8 位。
- `confirmPassword` 必填，且必须与 `newPassword` 一致。
- 如果用户不存在，返回明确业务错误即可。
- 数据库存储密码时必须写入哈希值，不允许明文。

## 5. 后端约束

- 包结构建议：
  - `com.ecommerce.auth.controller`
  - `com.ecommerce.auth.service`
  - `com.ecommerce.auth.dto`
  - `com.ecommerce.repository.UserRepository`
- `UserRepository` 采用注解式 MyBatis mapper。
- 本地 H2 初始化需要补充 `users` 表。
- 本地 H2 初始化需要补充至少一个测试用户。
- `POST /api/auth/password/reset` 应在当前本地基线中允许匿名访问。

## 6. 前端约束

- 新增一个简单重置密码页面即可。
- 页面至少包含：
  - email
  - newPassword
  - confirmPassword
  - submit button
- 页面提交成功后，应展示明确成功消息。
- 页面提交失败后，应展示后端返回的错误信息。
- 前端应保留当前已有页面与路由：
  - `HealthView`
  - `ProductListView`
  - `ProductDetailView`
- 只在现有路由结构中新增 `ResetPasswordView`，不要把现有应用替换成新的 Home/Products 路由体系。

## 7. 测试约束

- 至少覆盖：
  - controller 测试
  - service 测试
  - repository 测试
- 不要求邮件发送测试。
- 不要求 Redis、RocketMQ、JWT 刷新等扩展测试。

## 7.1 推荐生成顺序

基于本仓库此前 dry-run 经验，完整功能不要一开始就让模型一次性重写后端、前端和测试。

推荐顺序：

1. 先跑 `metagpt_tasks/password-reset/METAGPT_TASK_SERVER_PASSWORD_RESET_ONLY.md`
2. 检查后端 dry-run 输出是否收敛
3. 再跑 `metagpt_tasks/password-reset/METAGPT_TASK_WEB_PASSWORD_RESET_ONLY.md`
4. 最后再决定是否执行 `--overwrite`

实际执行步骤与本次 dry-run 结果可参考：

- [PASSWORD_RESET_RUNBOOK.md](PASSWORD_RESET_RUNBOOK.md)

## 8. 后续扩展（非当前最小实现）

以下能力属于后续集成阶段，不应覆盖当前最小可运行版本：

- 邮件验证码
- 重置 token
- token 过期机制
- 短信验证码
- 风控限流
- 图形验证码