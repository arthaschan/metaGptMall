只生成前端 Vue 3 项目代码，写入 web/ 目录；不要生成任何 server/ 文件。

强制输出格式（非常重要）：你必须对每个文件使用且只使用如下 fenced block 格式输出，否则程序不会写入磁盘：

```file path=web/<relative-path>
<full file content>
```

禁止输出 ```vue / ```ts / ```js 等其它类型代码块；禁止用 Markdown 标题包裹文件内容；禁止省略任何必须文件。

必须生成的文件清单（每个都要输出一个 ` ```file path=... ` 块）：
- web/package.json
- web/vite.config.ts
- web/index.html
- web/src/main.ts
- web/src/App.vue
- web/src/router/index.ts
- web/src/api/http.ts
- web/src/views/HealthView.vue
- web/README.md

功能要求：
- 前端使用 Vue 3（Composition API）+ Vite。
- HealthView.vue 必须请求后端 GET /api/health 并展示返回内容。
- 需提供本地联调方案（二选一即可）：
  A) 在 web/vite.config.ts 配置 devServer proxy，把 /api 转发到后端（例如 http://localhost:8080）
  B) 或在 README 说明后端需要开启全局 CORS

验收要求（输出末尾用普通文本列出检查清单即可）：
- [ ] 上述文件全部已输出为 web/ 路径的 file blocks
- [ ] npm install && npm run dev 可以启动
