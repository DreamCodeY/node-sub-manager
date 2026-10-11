# 节点管理小站

vless 节点链接 → Mihomo / Clash 配置，批量转换 + 订阅链接。

## 🔒 隐私架构（核心设计）

- **默认零上传**：vless 解析、YAML 生成全部在浏览器本地（JS）完成，节点信息不出用户设备。
- **订阅是用户主动选择**：只有点击「生成订阅链接」时，YAML 才会被 POST 到服务端保存，且用户可随时删除。
- **可自部署**：不信任演示站？`docker build` 一下，5 分钟跑自己的。

## 快速开始

### 本地运行

```bash
mvn package
java -jar target/node-sub-manager-0.1.0.jar
# 打开 http://localhost:8080
```

### Docker 一键部署

```bash
docker build -t node-sub-manager .
docker run -d -p 8080:8080 -v node-sub-data:/app/data --name node-sub node-sub-manager
```

### 部署到 VPS（Oracle 免费小鸡）

```bash
# 1. 安装 JDK 17
sudo apt update && sudo apt install -y openjdk-17-jre-headless

# 2. 传 jar 包并运行（screen / systemd 二选一）
java -jar node-sub-manager-0.1.0.jar

# 3. 放行端口（Oracle 控制台安全列表 + UFW 二选一）
sudo ufw allow 8080/tcp
```

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/sub | 上传 YAML 生成订阅（`Content-Type: text/plain`，上限 200KB）→ `{"id": "..."}` |
| GET | /sub/{id} | 拉取订阅（`text/yaml`），供 OpenClash / Clash 订阅 |
| DELETE | /sub/{id} | 删除订阅 |

## Roadmap

- [ ] v1：VLESS（含 REALITY）批量转换 + 订阅（当前版本）
- [ ] v2：trojan / ss / vmess 解析
- [ ] v2：订阅过期自动清理
- [ ] v3：Nginx 反代 + HTTPS（生产部署）
