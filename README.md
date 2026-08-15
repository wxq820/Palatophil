# 无定珍 (Palatophil)

生态食材采购协同系统。全栈实现按《无定珍 V1.2 全栈开发规格说明书》。

**当前进度：M2 食材与食谱 CRUD**（前后端 CRUD 联调 + 审核流转 + 食谱编辑器）

## 目录结构

```
Palatophil/
├── backend/          Spring Boot 3.3 + Java 17 + MyBatis-Plus + JWT + WebSocket
├── miniprogram/      微信小程序原生（Vant Weapp 后续接入）
├── admin/            Vue 3 + Vite + Element Plus PC 后台
├── docs/             规格书（V1.0 / V1.1 / V1.2）
├── docker-compose.yml
├── pom.xml           Maven 父 POM
└── .env.example
```

## 快速启动（推荐 Docker）

### 1. 启动基础设施（MySQL + Redis）

```bash
docker compose up -d mysql redis
```

> 首次启动会自动执行 `backend/src/main/resources/db/{schema,seed}.sql`，
> 创建 `palatophil` 库、12 张表、48 个系统食材、3 个示例食谱、2 个测试账号。
>
> **端口约定**：Docker MySQL 映射到本机 `3308`（避开 3306/3307），
> Redis 用 `6380`（避开 6379）。后端配置：`application-dev.yml` 已默认指向。

### 2. 启动后端

```bash
# 需要 JDK 17+ 与 Maven
cd backend
mvn spring-boot:run
```

启动后可以访问：

- 健康检查：<http://localhost:8080/api/health>
- 接口文档：<http://localhost:8080/doc.html>
- Swagger：<http://localhost:8080/swagger-ui.html>

### 3. 启动 PC 后台

```bash
cd admin
npm install
npm run dev
```

访问 <http://localhost:5173>，默认账号：

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | ADMIN |
| testuser | user123 | USER |

### 4. 启动微信小程序

用微信开发者工具导入 `miniprogram/` 目录，
关闭 `不校验合法域名`（开发期可勾上）即可联调。

## 默认测试账号

| 账号 | 密码 | 登录方式 |
|------|------|---------|
| admin | admin123 | PC 后台 |
| testuser | user123 | PC 后台 |

小程序登录：调用 `wx.login` → 调 `POST /api/auth/wx-login`（开发期走 mock 模式）→ 拿 JWT。

## API 端点

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/auth/wx-login` | 否 | 微信小程序登录（Mock 模式可用） |
| POST | `/api/auth/admin-login` | 否 | PC 后台账号密码登录 |
| GET | `/api/auth/me` | 是 | 获取当前登录用户 |
| POST | `/api/auth/logout` | 是 | 退出登录 |
| GET | `/api/health` | 否 | 健康检查 |
| POST | `/api/algorithm/compute-k` | 是 | K 系数计算（演示接口） |

## 核心算法（V1.2）

- K 系数：`K = Σ实际食材总量 / Σ标准食材总量`；每人实际 = round(标准量 × K, 1)
- 数值格式：统一 1 位小数，<0.1g 前端不展示，>0 按 0.1g 最小展示
- 食材合并：按 `ingredient_id` 全局合并；全员总量 = Σ(标准量 × K)
- 强校验：每次提交必须 ≥1 个食材

## 后续里程碑

- M2：食材库 + 标准食谱 CRUD
- M3：协同会话 + WebSocket 实时同步 + 草稿恢复
- M4：采购清单生成 + K 系数 + 价格记录 + 批零叠加
- M5：标签系统 + 关注/收藏/点赞
- M6：审核流程 + 历史版本 + 数据导出

## 文档

- [V1.0 - 产品说明](docs/无定珍V1.0.docx)
- [V1.1 - 功能详述](docs/无定珍V1.1.docx)
- [V1.2 - 全栈开发规格](docs/无定珍V1.2全栈开发规格说明书.docx)
