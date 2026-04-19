# 图片上传接口性能修复报告

## 1. 文档范围

本文档记录 `POST /api/files/images` 图片上传接口本次性能问题的定位、修复、测试流程和最终回归验证结果。

本次文档只覆盖图片上传链路，不包含其它接口的优化记录。

## 2. 问题背景

Review finding 指出图片上传接口存在以下风险：

```text
图片上传在事务内做同步磁盘 IO。

上传接口会把整个 MultipartFile 读入 byte[]，校验后同步写磁盘，再插入数据库。
600KB 限制让风险可控，但并发上传时线程会被文件 IO 占住，事务边界也包含了磁盘操作。
建议先落临时文件或对象存储，再短事务入库，并限制上传并发。
```

在前一轮修复中，上传方法已经去掉大事务，改成先写本地文件，再用短事务插入数据库记录。当前继续排查后确认，剩余主要风险已经从“磁盘 IO 包在数据库事务内”收敛为“请求线程内同步执行本地文件系统操作，且缺少并发保护”。

## 3. Systematic Debugging 排查流程

本次排查按 `superpowers:systematic-debugging` 执行，遵循“先找根因，再修复”的流程。

### 3.1 请求链路确认

上传入口：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/file/controller/FileController.java
POST /api/files/images
```

核心服务：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/file/service/impl/FileAssetServiceImpl.java
uploadImage
```

请求链路：

1. Controller 接收 `MultipartFile` 和 `scene` 参数。
2. Sa-Token 校验登录态。
3. Service 校验文件是否为空、类型是否合法、大小是否超过限制。
4. Service 读取整个上传文件为 `byte[]`。
5. Service 校验图片魔数。
6. Service 生成对象路径。
7. Service 写临时文件，再移动到正式文件。
8. Service 用短事务插入 `file_asset` 数据库记录。
9. 返回 `fileId`、`previewUrl`、`fileSize`、`contentType`。

### 3.2 根因假设

排查前建立了四类假设：

| 假设 | 判断方式 |
| --- | --- |
| 数据库插入是瓶颈 | 线程栈大量停在 JDBC、H2、MyBatis、Hikari 路径 |
| 本地磁盘写入是瓶颈 | 线程栈大量停在 `Files.write`、`Files.move`、`Files.createDirectories` |
| multipart 解析是瓶颈 | 线程栈大量停在 Tomcat multipart、请求体读取相关路径 |
| 服务整体吞吐不足 | `/api/health` 在同并发下也明显变慢 |

### 3.3 复现方式

复现环境：

```text
Windows 11
Spring Boot local-lite profile
端口：8080
上传根目录：target/perf/uploads、target/perf/uploads-diagnose、target/perf/uploads-fix
认证方式：真实注册、真实登录、Bearer token
上传文件：带 PNG 文件头的 600KB 测试文件
```

复现步骤：

1. 启动后端：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local-lite
```

2. 调用注册验证码接口：

```http
POST /api/auth/send-register-code
```

3. 从 `local-lite` 日志读取验证码。

4. 调用注册接口：

```http
POST /api/auth/register
```

5. 调用登录接口，获取 token：

```http
POST /api/auth/login
```

6. 使用 token 上传图片：

```bash
curl -H "Authorization: Bearer <token>" \
  -F "file=@large-600kb.png;type=image/png" \
  -F "scene=private" \
  http://localhost:8080/api/files/images
```

7. 使用 `jcmd <pid> Thread.print -l` 在压力过程中采样线程栈。

## 4. 修复前测试结果

### 4.1 基准压测结果

第一轮小规模 benchmark：

| 场景 | 并发 | 请求数 | 成功数 | RPS | 平均耗时 | p50 | p95 | p99 | 最大耗时 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 10KB 图片上传 | 1 | 20 | 20 | 32.58 | 30.22ms | 31.36ms | 32.55ms | 33.87ms | 33.87ms |
| 10KB 图片上传 | 5 | 50 | 50 | 158.89 | 28.71ms | 31.23ms | 42.61ms | 49.33ms | 49.33ms |
| 10KB 图片上传 | 10 | 80 | 80 | 269.28 | 34.46ms | 34.06ms | 59.42ms | 59.85ms | 72.31ms |
| 600KB 图片上传 | 1 | 20 | 20 | 29.11 | 33.15ms | 31.54ms | 43.87ms | 47.30ms | 47.30ms |
| 600KB 图片上传 | 5 | 50 | 50 | 153.12 | 28.96ms | 28.70ms | 47.18ms | 54.32ms | 54.32ms |
| 600KB 图片上传 | 10 | 80 | 80 | 224.56 | 39.03ms | 38.44ms | 66.77ms | 72.75ms | 73.85ms |

第二轮提高 600KB 上传并发：

| 场景 | 并发 | 请求数 | 成功数 | RPS | 平均耗时 | p50 | p95 | p99 | 最大耗时 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 600KB 图片上传 | 20 | 200 | 200 | 235.82 | 79.41ms | 77.67ms | 113.88ms | 125.41ms | 152.33ms |
| 600KB 图片上传 | 50 | 300 | 300 | 222.91 | 203.10ms | 208.90ms | 273.46ms | 289.97ms | 326.19ms |

对照健康检查：

| 场景 | 并发 | 请求数 | 成功数 | RPS | 平均耗时 | p50 | p95 | p99 | 最大耗时 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `/api/health` | 50 | 300 | 300 | 1868.52 | 19.87ms | 14.78ms | 39.14ms | 43.58ms | 50.98ms |

结论：

同样 50 并发下，健康检查 p95 约 `39.14ms`，600KB 上传 p95 约 `273.46ms`。因此瓶颈不是服务基础吞吐本身，而是上传链路内部的文件处理、请求体处理或数据库插入路径。

### 4.2 修复前线程栈诊断结果

在 50 并发持续上传 600KB 图片时，采集 8 次线程栈。

请求结果：

| 指标 | 结果 |
| --- | ---: |
| 请求数 | 2909 |
| 成功数 | 2909 |
| 平均耗时 | 212.23ms |
| p50 | 210.21ms |
| p95 | 281.82ms |
| p99 | 327.21ms |
| 最大耗时 | 397.71ms |

线程栈分类汇总：

| 线程状态分类 | 采样次数 |
| --- | ---: |
| 请求线程总采样 | 414 |
| 正在上传服务内部 | 345 |
| 正在文件写入、移动或目录操作 | 345 |
| multipart 或请求体读取 | 62 |
| 数据库、H2、JDBC 插入 | 6 |
| 空闲等待 | 24 |

典型栈位置：

```text
sun.nio.fs.WindowsNativeDispatcher.CreateDirectory0
sun.nio.fs.WindowsFileSystemProvider.createDirectory
java.nio.file.Files.createDirectories
FileAssetServiceImpl.storeImageFile
```

```text
sun.nio.fs.WindowsNativeDispatcher.MoveFileEx0
sun.nio.fs.WindowsFileCopy.move
java.nio.file.Files.move
FileAssetServiceImpl.moveTempFile
FileAssetServiceImpl.storeImageFile
```

诊断结论：

请求线程主要停在本地文件系统操作上，数据库插入采样很少。根因确定为：上传接口在 HTTP 请求线程内同步执行本地文件保存，且缺少并发保护，导致 50 并发时大量请求线程同时进入文件系统操作路径。

## 5. 修复目标

本次修复目标：

1. 不改变接口 URL、请求参数、返回结构。
2. 不改变登录鉴权语义。
3. 不改变私有图片和公开图片预览语义。
4. 不改变文件落盘成功后再插入数据库记录的顺序。
5. 限制同时进入本地文件保存阶段的请求数量。
6. 减少每次上传重复调用目录创建带来的文件系统元数据压力。

本次未做的事项：

1. 未改为异步上传。
2. 未迁移到对象存储。
3. 未取消 `MultipartFile.getBytes()` 的整文件读取。
4. 未改变图片大小限制。

## 6. 代码改动内容

### 6.1 新增本地文件保存并发闸门

新增文件：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/file/service/impl/LocalFileStoreGuard.java
```

职责：

1. 使用公平 `Semaphore` 限制同时进入本地文件保存阶段的请求数。
2. 默认并发数至少为 `1`，避免配置为 `0` 或负数导致上传不可用。
3. 对已经成功创建过的目录做进程内缓存。
4. 如果目录创建失败，从缓存中移除，允许后续请求重新创建。
5. 如果线程等待上传闸门时被中断，恢复中断状态并抛出业务异常。

核心逻辑：

```java
private final Semaphore fileStorePermits;
private final ConcurrentMap<Path, Boolean> createdDirectories = new ConcurrentHashMap<>();
```

```java
void execute(FileStoreOperation operation) throws IOException {
    acquirePermit();
    try {
        operation.run();
    } finally {
        fileStorePermits.release();
    }
}
```

```java
void ensureDirectoryExists(Path directory) throws IOException {
    Path normalizedDirectory = directory.toAbsolutePath().normalize();
    if (createdDirectories.putIfAbsent(normalizedDirectory, Boolean.TRUE) != null) {
        return;
    }
    try {
        directoryCreator.createDirectories(normalizedDirectory);
    } catch (IOException | RuntimeException exception) {
        createdDirectories.remove(normalizedDirectory);
        throw exception;
    }
}
```

### 6.2 上传保存文件时接入闸门

修改文件：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/file/service/impl/FileAssetServiceImpl.java
```

新增字段：

```java
private final LocalFileStoreGuard localFileStoreGuard;
```

构造函数中初始化：

```java
this.localFileStoreGuard = new LocalFileStoreGuard(fileProperties.getMaxConcurrentImageStores());
```

原本文件保存阶段直接执行：

```java
Files.createDirectories(tempPath.getParent());
Files.createDirectories(targetPath.getParent());
Files.write(tempPath, fileBytes);
moveTempFile(tempPath, targetPath);
```

现在改为：

```java
localFileStoreGuard.execute(() -> writeImageFile(tempPath, targetPath, fileBytes));
```

`writeImageFile` 会先尝试使用缓存目录写入。如果目录缓存已存在但目录被外部删除，`NoSuchFileException` 会触发缓存失效，并重试一次：

```java
private void writeImageFile(Path tempPath, Path targetPath, byte[] fileBytes) throws IOException {
    try {
        writeImageFileOnce(tempPath, targetPath, fileBytes);
    } catch (NoSuchFileException exception) {
        localFileStoreGuard.forgetDirectory(tempPath.getParent());
        localFileStoreGuard.forgetDirectory(targetPath.getParent());
        writeImageFileOnce(tempPath, targetPath, fileBytes);
    }
}
```

### 6.3 新增配置项

修改文件：

```text
backend/src/main/java/io/github/dongxuetaffy/aobihelper/config/FileProperties.java
```

新增配置字段：

```java
private int maxConcurrentImageStores = 20;
```

对应配置：

```yaml
app:
  file:
    max-concurrent-image-stores: 20
```

涉及配置文件：

| 文件 | 配置值 |
| --- | --- |
| `backend/src/main/resources/application-dev.yml` | `20` |
| `backend/src/main/resources/application-local.yml` | `20` |
| `backend/src/main/resources/application-local-lite.yml` | `20` |
| `backend/src/main/resources/application-prod.yml` | `${APP_FILE_MAX_CONCURRENT_IMAGE_STORES:20}` |

生产环境可通过环境变量调整：

```text
APP_FILE_MAX_CONCURRENT_IMAGE_STORES
```

### 6.4 新增单元测试

新增文件：

```text
backend/src/test/java/io/github/dongxuetaffy/aobihelper/file/service/impl/LocalFileStoreGuardTest.java
```

测试内容：

| 测试 | 验证点 |
| --- | --- |
| `executeLimitsConcurrentFileStoreWork` | 当闸门大小为 1 时，第二个文件保存任务必须等待第一个任务释放许可 |
| `ensureDirectoryExistsCachesSuccessfulDirectoryCreation` | 同一个目录创建成功后，重复调用不会再次触发目录创建 |

## 7. TDD 测试流程

### 7.1 RED

先新增 `LocalFileStoreGuardTest`，此时生产代码还没有 `LocalFileStoreGuard`。

执行：

```powershell
mvn -q -Dtest=LocalFileStoreGuardTest test
```

预期失败：

```text
找不到符号
符号: 类 LocalFileStoreGuard
```

说明测试确实覆盖了新增并发闸门能力。

### 7.2 GREEN

新增 `LocalFileStoreGuard`，并在 `FileAssetServiceImpl` 的本地文件保存阶段接入。

再次执行：

```powershell
mvn -q -Dtest=LocalFileStoreGuardTest test
```

结果：

```text
退出码 0
```

### 7.3 上传相关测试

执行：

```powershell
mvn -q -Dtest=FileAssetServiceImplTest,LocalFileStoreGuardTest test
```

结果：

```text
退出码 0
```

覆盖内容：

1. 图片上传后文件和数据库记录存在。
2. 私有图片预览保留鉴权。
3. 公开图片允许匿名预览。
4. 本地文件保存闸门限制并发。
5. 目录创建缓存生效。

## 8. 修复后压测验证结果

修复后使用相同方式启动 `local-lite`，使用真实注册、登录、Bearer token，50 并发持续上传 600KB 图片。

修复后请求结果：

| 指标 | 结果 |
| --- | ---: |
| 请求数 | 3419 |
| 成功数 | 3419 |
| 平均耗时 | 179.87ms |
| p50 | 177.42ms |
| p95 | 227.96ms |
| p99 | 268.37ms |
| 最大耗时 | 438.19ms |

修复后线程栈分类汇总：

| 线程状态分类 | 采样次数 |
| --- | ---: |
| 请求线程总采样 | 413 |
| 正在上传服务内部 | 346 |
| 正在文件写入、移动或目录操作 | 185 |
| 正在等待本地文件保存闸门 | 203 |
| 数据库、H2、JDBC 插入 | 1 |
| 空闲等待 | 33 |

修复前后核心数据对比：

| 指标 | 修复前 | 修复后 |
| --- | ---: | ---: |
| 请求数 | 2909 | 3419 |
| 成功数 | 2909 | 3419 |
| 平均耗时 | 212.23ms | 179.87ms |
| p50 | 210.21ms | 177.42ms |
| p95 | 281.82ms | 227.96ms |
| p99 | 327.21ms | 268.37ms |
| 文件写入、移动或目录操作线程采样 | 345 | 185 |
| 数据库、H2、JDBC 插入线程采样 | 6 | 1 |

修复后现象：

1. 请求仍然全部成功。
2. p95 从 `281.82ms` 降到 `227.96ms`。
3. p99 从 `327.21ms` 降到 `268.37ms`。
4. 同时停在文件系统操作上的请求线程明显减少。
5. 一部分请求线程改为等待本地文件保存闸门，说明并发冲击被控制在服务内部。
6. 数据库仍然不是主要瓶颈。

## 9. 最终功能回归验证

最终回归验证按用户要求只做功能回归和单元测试，没有再次执行大规模并发压测。

### 9.1 启动方式

使用 `local-lite` 启动后端，并将上传根目录指向临时目录：

```powershell
mvn spring-boot:run `
  -Dspring-boot.run.profiles=local-lite `
  -Dspring-boot.run.arguments=--app.file.local-root=../target/final-verification/uploads
```

### 9.2 功能回归步骤

1. 调用健康检查：

```http
GET /api/health
```

2. 调用注册验证码接口：

```http
POST /api/auth/send-register-code
```

3. 从 `local-lite` 日志读取验证码。

4. 调用注册接口：

```http
POST /api/auth/register
```

5. 调用登录接口：

```http
POST /api/auth/login
```

6. 使用 token 上传私有图片：

```http
POST /api/files/images
scene=private
```

7. 使用 token 预览私有图片：

```http
GET /api/files/{privateFileId}/preview
Authorization: Bearer <token>
```

8. 匿名预览私有图片：

```http
GET /api/files/{privateFileId}/preview
```

9. 使用 token 上传公开图片：

```http
POST /api/files/images
scene=public
```

10. 匿名预览公开图片：

```http
GET /api/files/{publicFileId}/preview
```

### 9.3 功能回归结果

| 验证项 | 结果 |
| --- | --- |
| 健康检查 | `200` |
| 登录 token 获取 | 成功 |
| 私有图片上传 | `200` |
| 私有图片带 token 预览 | `200` |
| 私有图片匿名预览 | `400`，符合未登录不可访问私有图片的语义 |
| 公开图片上传 | `200` |
| 公开图片匿名预览 | `200` |

实际回归输出：

```json
{
  "health": "200",
  "privateUpload": "200",
  "privatePreviewWithToken": "200",
  "privatePreviewAnonymous": "400",
  "publicUpload": "200",
  "publicPreviewAnonymous": "200",
  "privateFileId": "7233",
  "publicFileId": "7234"
}
```

回归后清理：

1. 停止本次启动的 `8080` 后端进程。
2. 删除 `target/final-verification` 临时文件目录。
3. 删除本次回归创建的临时测试账号。
4. 删除本次回归创建的 `file_asset` 记录。
5. 删除本次回归创建的 `user_stats` 记录。

清理确认：

```text
remaining_users = 0
remaining_stats = 0
```

## 10. 最终单元测试结果

最终执行完整后端单元测试：

```powershell
cd backend
mvn -q test
```

结果：

```text
退出码 0
```

说明：

1. 上传相关测试通过。
2. 库存相关测试通过。
3. 交易相关测试通过。
4. 本次新增的本地文件保存闸门测试通过。

## 11. 工作区检查

执行：

```powershell
git diff --check
```

结果：

```text
退出码 0
```

输出中仅有 Windows 换行提示：

```text
LF will be replaced by CRLF the next time Git touches it
```

该提示不是空白错误，不影响本次代码改动。

## 12. 最终结论

本次修复没有改变 `POST /api/files/images` 的接口功能、返回结构、鉴权逻辑和预览语义。

本次修复解决的是上传链路在并发情况下大量请求线程同时进入本地文件系统操作的问题。通过新增本地文件保存并发闸门和目录创建缓存，修复后同时停在文件系统写入、移动、目录创建路径上的请求线程明显减少，p95 和 p99 均有下降。

本次最终验证包含完整后端单元测试和真实 API 功能回归验证，结果均通过。
