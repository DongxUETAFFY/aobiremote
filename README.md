# aobidao-web

本项目本地测试时，前后端分开启动。

## 环境要求

- Node.js `20+`
- npm `10+`
- JDK `17+`
- Maven `3.9+`

## 最推荐的本地启动方式

这是最适合测试的组合：

- 前端走 `npm`
- 后端走 `maven`
- 后端使用 `local-lite` 配置

`local-lite` 会使用：

- H2 本地文件数据库
- 内存验证码缓存
- 本地文件上传目录

所以本地测试时不需要先准备 MySQL、Redis、MinIO。

## 启动前端

在仓库根目录打开一个终端，执行：

```powershell
cd frontend
npm install
npm run dev -- --host 127.0.0.1 --port 5173
```

也可以直接运行脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-frontend.ps1
```

## 启动后端

再打开一个终端，执行：

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local-lite"
```

也可以直接运行脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-backend-lite.ps1
```

说明：

- `local-lite` 配置文件在 `backend/src/main/resources/application-local-lite.yml`
- 后端默认端口是 `8080`
- 前端默认请求 `http://localhost:8080/api`

## 你测试时该访问的地址

前端页面：

- [http://127.0.0.1:5173/](http://127.0.0.1:5173/)

后端接口基地址：

- [http://127.0.0.1:8080/api](http://127.0.0.1:8080/api)

后端健康检查：

- [http://127.0.0.1:8080/actuator/health](http://127.0.0.1:8080/actuator/health)

H2 控制台：

- [http://127.0.0.1:8080/h2-console](http://127.0.0.1:8080/h2-console)

## 当前这台机器的注意事项

如果直接运行 `backend/mvnw.cmd`，可能会因为 Windows 用户目录包含特殊字符而启动失败。

仓库里的 `scripts/dev-backend-lite.ps1` 和 `scripts/dev-backend.ps1` 已经做了兼容处理：

- 优先尝试 `mvnw.cmd`
- 如果失败，自动回退到全局 `mvn`
- 同时把 `TEMP/TMP` 指向仓库下的 `.tmp`

所以在这台机器上，优先用上面的脚本或直接用全局 `mvn`。
