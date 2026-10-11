# 节点管理小站 — Plan（v1）

## 1. 技术选型与架构
- Java 17 + Spring Boot 3.x，仅 `spring-boot-starter-web`
- **隐私架构**：vless 解析 + YAML 生成在浏览器本地完成（JS），节点信息默认不出用户设备；后端只做订阅文件的托管与静态页面服务
- Maven 构建，打包为可执行 jar；无数据库，订阅文件存本地 `./data/{id}.yaml`
- 前端：单个静态 HTML（`src/main/resources/static/index.html`），JS 部分可让 AI 生成
- 开源 + Dockerfile，支持一键自部署

## 2. 接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/sub | 用户主动生成订阅：body 为 YAML 文本，返回订阅 ID |
| GET | /sub/{id} | 订阅接口，返回 YAML（`Content-Type: text/yaml`），供 OpenClash 订阅 |
| DELETE | /sub/{id} | 删除订阅（用户可随时删除服务端数据） |
| GET | / | 静态前端页面（本地转换全部在浏览器完成） |

POST /api/sub：
- 请求：YAML 文本（`Content-Type: text/plain`，上限 200KB）
- 响应：`{"id": "aB3x9Q...", "subUrl": "https://host/sub/aB3x9Q..."}`

安全：`{id}` 只允许 16 位 `[A-Za-z0-9]`，防止路径穿越；请求体限制 200KB。

## 3. 核心模块设计

### 前端 JS（浏览器本地执行，零上传）
- `parseVless(link, lineNo)`：解析 `vless://uuid@server:port?params#remark`，用 `URL` API 拆解，失败返回 `{line, message}`，不中断整体
- `generateMihomoYaml(nodes)`：拼 Mihomo 格式 YAML（proxies + proxy-groups(url-test, 300s) + rules(GEOIP,CN,DIRECT / MATCH,PROXY)）
- 错误行标红展示（行号+原因）

### 后端 Java（只做订阅托管）
- `SubService`：`SecureRandom` 生成 16 位 ID，YAML 写入 `./data/{id}.yaml`，支持删除
- `NodeController`：POST /api/sub、GET /sub/{id}、DELETE /sub/{id} + 全局异常处理
- 安全：`{id}` 只允许 16 位 `[A-Za-z0-9]`（防路径穿越）；请求体上限 200KB；日志脱敏

## 4. 前端（单 HTML）
- textarea 粘贴链接 → 转换按钮 → 结果预览
- 错误行标红（显示行号+原因），其余正常展示
- 展示订阅 URL（一键复制）+ 下载 YAML 按钮

## 5. 部署
- `mvn package` → `java -jar` 跑在 Oracle VPS（Ubuntu 24.04），端口 8080
- UFW 放行 8080（或后续加 Nginx 反代+域名，v1 先裸跑）

## 6. 任务拆分（生成代码时按此顺序）
1. 工程骨架：pom + Application 启动类
2. 后端：`SubService`（ID 生成 + 文件存取/删除）+ `NodeController`（3 接口 + 异常处理）
3. 前端：`index.html`（JS 本地解析 + YAML 生成 + 错误标红 + 订阅/下载按钮）
4. Dockerfile（一键自部署）+ README（含隐私架构说明、打包部署命令）
