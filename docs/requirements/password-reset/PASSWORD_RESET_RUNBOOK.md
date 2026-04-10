# 用户重置密码实操 Runbook

本文档记录“用户重置密码”这个真实需求在当前仓库中的推荐执行方式。

目标不是直接一把梭 overwrite，而是先把需求写清楚，再让 MetaGPT 以更稳定的方式生成。

## 1. 需求结论

当前仓库中，“重置密码”采用本地最小可实现版本：

- 接口：`POST /api/auth/password/reset`
- 请求字段：
  - `email`
  - `newPassword`
  - `confirmPassword`
- 本地不依赖邮件、短信、验证码或 token
- 后端需要更新 `users.password_hash`
- 前端需要一个简单重置密码页面

正式需求定义见：

- [PASSWORD_RESET_REQUIREMENT.md](PASSWORD_RESET_REQUIREMENT.md)

## 2. 为什么不要先跑一个“大而全”任务

本次已经实际验证：

- [metagpt_outputs/20260410_195358/SUMMARY.md](metagpt_outputs/20260410_195358/SUMMARY.md)

这次 full feature dry-run 的方向是对的，但模型在后半段开始出现不稳定：

- 后端文件生成方向正确
- 测试输出在尾部附近开始变脆弱
- 前端部分没有稳定落盘

结论：

- 对“重置密码”这类跨后端、前端、测试的需求，不建议先跑一个完整任务
- 更稳的方式是拆成后端任务和前端任务分别执行

## 3. 推荐执行顺序

### 第一步：后端 dry-run

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/password-reset/METAGPT_TASK_SERVER_PASSWORD_RESET_ONLY.md --mode impl --dry-run
```

本次对应输出：

- [metagpt_outputs/20260410_195645/SUMMARY.md](metagpt_outputs/20260410_195645/SUMMARY.md)
- [metagpt_outputs/20260410_195645/IMPL_RAW.md](metagpt_outputs/20260410_195645/IMPL_RAW.md)

重点检查：

- 是否生成 `UserRepository`
- 是否生成 `PasswordResetRequest`
- 是否生成 `PasswordResetResponse`
- 是否生成 `PasswordResetService`
- 是否生成 `AuthController`
- 是否放通 `POST /api/auth/password/reset`
- 是否仍保持 H2 + MyBatis
- service 是否依赖 `PasswordEncoder`，而不是把密码明文落库

### 第二步：前端 dry-run

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/password-reset/METAGPT_TASK_WEB_PASSWORD_RESET_ONLY.md --mode impl --dry-run
```

本次对应输出：

- [metagpt_outputs/20260410_195652/SUMMARY.md](metagpt_outputs/20260410_195652/SUMMARY.md)
- [metagpt_outputs/20260410_195652/IMPL_RAW.md](metagpt_outputs/20260410_195652/IMPL_RAW.md)

重点检查：

- 是否生成 `web/src/views/ResetPasswordView.vue`
- 是否生成 `web/src/api/auth.ts`
- 路由是否是在现有结构上新增 `/reset-password`
- 是否保留已有：
  - `HealthView`
  - `ProductListView`
  - `ProductDetailView`
- 不要虚构 `HomeView.vue`、`ProductsView.vue`

## 4. 本次实际观察到的收敛情况

### 后端任务

后端拆分任务已经明显比 full feature 任务稳定。

它已经稳定朝这些方向生成：

- `UserRepository`
- `PasswordResetRequest`
- `PasswordResetResponse`
- `PasswordResetService`
- `AuthController`
- 基于 H2 + MyBatis 的 `users` 表

当前剩余的小偏差：

- 有时仍会改写 `WebSecurityConfig` 的细节风格
- 有时测试文件在 dry-run 摘要里不完全落盘，但 raw output 方向是对的

### 前端任务

前端拆分任务在收紧约束后，已经不再虚构 `HomeView` / `ProductsView`。

当前它能稳定朝这些方向生成：

- `ResetPasswordView.vue`
- `web/src/api/auth.ts`
- 在现有 router 上新增 `/reset-password`

当前剩余的小偏差：

- 它仍可能把 `App.vue` 外层 shell 样式改得比当前仓库更激进

这类偏差属于可 review 的 UI 结构偏差，不再是功能契约偏差。

## 5. 为什么 `SUMMARY.md` 里文件数可能比你预期少

因为 dry-run 默认不会覆盖已存在文件。

例如本次前端任务里：

- `ResetPasswordView.vue` 和 `web/src/api/auth.ts` 是新文件，所以会出现在摘要里
- `router/index.ts` 和 `App.vue` 已存在，因此 dry-run 会跳过写入，摘要里不一定列出来

这不代表模型没有生成这些内容。

所以必须同时查看：

- `SUMMARY.md`
- `IMPL_RAW.md`

## 6. 下一步如何真正执行 overwrite

建议顺序：

1. 先执行 server password reset task 的 `--overwrite`
2. 本地编译、启动、接口验证
3. 再执行 web password reset task 的 `--overwrite`
4. 再做前端构建验证

建议命令：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/password-reset/METAGPT_TASK_SERVER_PASSWORD_RESET_ONLY.md --mode impl --overwrite --auto-fix --max-fix-rounds 2

PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/password-reset/METAGPT_TASK_WEB_PASSWORD_RESET_ONLY.md --mode impl --overwrite
```

## 7. overwrite 后的本地验证

后端：

```bash
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml test
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml -Dmaven.test.skip=true spring-boot:run
curl -sf http://localhost:8080/api/auth/password/reset \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","newPassword":"new-password-123","confirmPassword":"new-password-123"}'
```

前端：

```bash
cd /Users/arthas/git/metaGptMall/web
npm install
npm run build
```

## 8. 给新手的最短建议

如果你是第一次做这个需求，只记住这四步：

1. 先看 [PASSWORD_RESET_REQUIREMENT.md](PASSWORD_RESET_REQUIREMENT.md)
2. 先跑后端拆分 dry-run
3. 再跑前端拆分 dry-run
4. 检查 `IMPL_RAW.md` 没明显漂移后，再 overwrite