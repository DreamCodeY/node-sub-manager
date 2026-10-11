# 节点管理小站 — Spec（v1）

## 1. 背景与目标

手动把 `vless://` 节点链接转成 Clash/Mihomo 配置又慢又容易错，且节点信息（UUID、服务器 IP）属于敏感信息，很多人不愿贴给第三方 AI。本项目做一个自部署的节点管理小站：批量转换 + 生成订阅链接，软路由的 OpenClash 可直接订阅。

目标用户：自己（首要），以及玩 VPS/软路由的网友。

## 2. 功能需求

### F1 批量转换节点链接（纯本地，不经过服务端）
- 解析与 YAML 生成在浏览器本地完成（JS），节点信息不出用户浏览器
- 用户粘贴 1~50 个 `vless://` 链接（每行一个），点击转换
- 解析字段：uuid、server、port、encryption、sni、pbk、sid、fp、flow、network
- 生成 Mihomo 兼容的 YAML（proxy + proxy-groups + rules，参考现有模板）
- v1 只支持 VLESS（含 REALITY）；其他协议（vmess/trojan/ss）后续版本再加

### F2 非法链接标识
- 非法链接不导致整体失败，逐行标注：第 N 行格式错误
- 前端把错误行标红展示，其余正常转换

### F3 订阅链接（核心差异化，用户主动选择才上传）
- 转换成功后，用户主动点击"生成订阅链接"，前端才将 YAML POST 到后端
- 后端将 YAML 存为本地文件，返回订阅 ID（随机 16 位字符）
- `GET /sub/{id}` 返回 YAML 内容（`Content-Type: text/yaml`），OpenClash 可直接添加订阅
- `DELETE /sub/{id}` 删除订阅（用户可随时删除服务端数据）
- 页面明确告知：默认转换不上传任何数据；只有点"生成订阅"才会上传
- 订阅内容更新：用户重新转换会生成新 ID（v1 不做覆盖更新）

### F4 下载 YAML
- 转换结果页提供"下载 YAML"按钮，直接下载文件

## 3. 非功能需求（隐私架构）
- 默认零上传：转换全过程在浏览器本地完成，节点信息不出用户设备
- 订阅是用户主动选择：只有点击"生成订阅链接"才会上传 YAML；用户可随时删除
- 单 jar 部署，无数据库，订阅文件存本地 `./data/{id}.yaml`
- 日志中不对节点敏感字段（uuid、server）做全量打印（脱敏）
- 开源 + Docker 一键自部署：不信任演示站的用户可 5 分钟搭自己的
- 不做用户系统
- 50 个链接转换耗时 < 3 秒

## 4. 验收标准
- [ ] 粘 1 个合法 vless 链接 → 生成 YAML，可导入 Clash Verge 正常使用
- [ ] 粘 50 个合法链接 → 全部转换，耗时 < 3 秒
- [ ] 混入 2 个坏链接 → 坏行标红提示行号，其余 48 个正常转换
- [ ] 转换后拿到订阅 URL → 在 OpenClash 里添加订阅能拉取到配置
- [ ] 打包成单个 jar，在 Ubuntu 24.04 上 `java -jar` 直接跑起来

## 5. 不做（Out of Scope）
- 用户注册/登录系统
- vmess / trojan / ss 等其他协议（v2 再说）
- 订阅内容的增量更新、过期清理（v1 先跑起来）
- 管理后台

## 6. 技术约束
- Java 17 + Spring Boot 3.x（仅 `spring-boot-starter-web`，保持极简）
- Maven 构建，打包为可执行 jar
- 部署目标：Oracle VPS（Ubuntu 24.04，1 核 1G），默认端口 8080
- 前端：单个静态 HTML（可让 AI 生成），放 `src/main/resources/static`
