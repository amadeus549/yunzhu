# 云筑 Hestia · 智慧社区设施运维平台

> 面向物业与园区的一体化设施运维系统：把「住户报修 → 受理 → 派单 → 上门 → 验收」整条链路做成可追踪、可度量、可升级的闭环。

传统物业的报修靠微信群和电话，工单散落在聊天记录里，超时无人跟进、责任无法追溯。
Hestia 把这条链路沉淀成结构化数据，并用 **状态机 + SLA 时效 + 加权派单 + 巡检闭环** 四个机制把「救火式维修」变成「可运营的服务」。

---

## 一、核心能力

| 模块 | 说明 |
|---|---|
| 工单中心 | 7 态状态机、多条件检索、全链路流转日志、返工计数、满意度评价 |
| SLA 时效 | 按优先级匹配响应/解决双时限，定时扫描超时并自动升级通知主管 |
| 智能派单 | 技能匹配 × 负载余量 × 派单间隔 三因子加权评分，输出候选人排行 |
| 派单看板 | 待派单工单 + 候选人评分并列展示，支持一键派单与手动指派 |
| 设备台账 | 设备档案、质保到期、维保周期、下次维保日期自动推算与到期提醒 |
| 巡检管理 | 周期计划（日/周/月）、幂等生成任务、异常巡检一键转工单 |
| 服务目录 | 工单类型 → 服务项（含规格档位）→ 服务包（套餐）三层结构 |
| 运营报表 | SLA 达成率、工单趋势、类型分布、工程师绩效，支持 Excel 导出 |
| 实时通知 | 派单、超时预警、验收提醒落库 + WebSocket 推送 |

---

## 二、技术栈

| 层次 | 选型 |
|---|---|
| 后端框架 | Spring Boot 2.7.3、Spring MVC、MyBatis 2.2 |
| 数据层 | MySQL 8.0、Druid 连接池、PageHelper 分页 |
| 缓存 | Redis（工单号原子自增、SLA 预警去重） |
| 安全 | JWT 双端鉴权（管理端 / 住户端独立密钥）、BCrypt 密码加密 |
| 实时通信 | WebSocket（`/ws/{receiverType}-{receiverId}`） |
| 接口文档 | Knife4j（Swagger 2） |
| 报表导出 | Apache POI（XSSF） |
| 对象存储 | 阿里云 OSS |
| 前端 | Vue 2.7、Element UI、ECharts 5、axios |
| 构建 | Maven 多模块、Vue CLI 5 |
| 部署 | Nginx 静态托管 + 反向代理 |

---

## 三、工程结构

```
hestia-ops/                     后端（Maven 多模块）
├── hestia-common/              通用能力：常量、枚举、异常、JWT、OSS
│   └── enumeration/
│       └── WorkOrderStatus.java    ★ 工单状态机（含流转表）
├── hestia-pojo/                领域模型：entity / dto / vo
└── hestia-server/              Web 层
    ├── controller/admin/       管理端接口
    ├── controller/resident/    住户端接口
    ├── service/impl/           业务实现
    │   ├── WorkOrderServiceImpl.java    ★ 状态机流转
    │   ├── DispatchServiceImpl.java     ★ 加权派单
    │   ├── SlaServiceImpl.java          ★ 时效扫描
    │   └── InspectionServiceImpl.java   ★ 巡检生成
    ├── mapper/                 MyBatis 接口
    ├── task/OpsScheduledTask   定时任务
    └── websocket/              WebSocket 推送

hestia-admin/                   前端（Vue 2.7 + Element UI）
├── src/api/                    接口封装
├── src/layout/                 侧边栏 + 顶栏布局
├── src/views/                  13 个业务页面
└── src/router/                 路由与守卫

db/
├── schema.sql                  19 张表 DDL
└── data.sql                    初始化数据（含 17 条工单样例）
```

---

## 四、关键设计

### 4.1 工单状态机

状态不是随手 `setStatus`，而是收敛到一个枚举 + 一张流转表：

```
待受理(1) ──受理──> 待派单(2) ──派单──> 已派单(3) ──接单──> 已接单(4)
   │                   │                  │                    │
   │                   │                  └──拒单──> 待派单(2)   │
   └──────取消─────────┴───────────────────────────────────────> 已取消(0)

已接单(4) ──开始处理──> 处理中(5) ──提交完工──> 待验收(6)
待验收(6) ──验收通过──> 已完成(7)
待验收(6) ──验收驳回──> 已驳回(8) ──返工──> 处理中(5)
```

要点：

- `WorkOrderStatus.canTransitTo()` 用 `EnumMap<Status, EnumSet<Status>>` 存流转表，查询 O(1)
- 所有状态变更走 `WorkOrderServiceImpl.changeStatus()`，非法跳转直接抛业务异常
- `allowedNext()` 把可执行动作下发给前端，按钮和校验共用一套规则，避免前后端各写一遍
- 每次流转写一条 `wo_log`，形成完整审计链路

### 4.2 SLA 时效

- 建单时按优先级匹配 `sla_rule`，落库 **响应截止** 与 **解决截止** 两个时间点
- 未接单阶段看响应时限，接单之后看解决时限（`fillSlaView` 按状态切换基准）
- 定时任务每 60 秒扫描一次：已过期且未打标的工单，写入 `response_breached` / `resolve_breached`，并通知主管与处理人
- 预警扫描（剩余 < 30 分钟）用 **Redis SETNX 去重**，同一工单 6 小时内只提醒一次，避免疲劳轰炸

### 4.3 智能派单

评分模型（满分 100）：

| 维度 | 权重 | 规则 |
|---|---|---|
| 技能匹配 | 60 | 完全覆盖所需技能 60；部分覆盖 30；不匹配 0 |
| 负载余量 | 30 | `(1 - 在手工单 / 上限) × 30` |
| 派单间隔 | 10 | 距上次派单 ≥2h 得 10；≥30min 得 5 |

技能不匹配不会被直接排除，只是分数落后——保证极端情况下仍有人可派。候选人列表带 `reason` 字段说明每一分的来源，派单过程可解释。

### 4.4 巡检闭环

- 周期推演支持「每日 / 每周几 / 每月几号」，`cycle_value` 存 `MON,WED` 或 `1,15`
- 任务生成做双重幂等：唯一索引 `(plan_id, device_id, plan_date)` + 生成前 `count` 校验
- 巡检发现异常可直接转工单：自动建一张高优先级报修单，设备状态同步改为「维修中」
- 每小时标记逾期任务，形成「计划 → 任务 → 异常 → 工单」的完整闭环

### 4.5 其他工程细节

- **工单号生成**：`WO + yyyyMMdd + 4 位序号`，Redis `INCR` 保证多实例不重复；Redis 不可用时降级为时间戳后缀，不阻塞建单
- **鉴权隔离**：管理端与住户端使用不同 JWT 密钥和请求头，令牌无法跨端冒用
- **密码存储**：BCrypt（自带盐值），优于常见的 MD5
- **通知可靠性**：先落库再推送，WebSocket 断开也不丢消息，用户下次登录可在通知中心看到
- **无物理外键**：一致性由应用层保证，便于后续分库分表

---

## 五、快速开始

### 5.1 环境要求

- JDK 11
- Maven 3.6+
- MySQL 8.0
- Redis 5+
- Node.js 16+

### 5.2 初始化数据库

```bash
mysql -uroot -p < db/schema.sql
mysql -uroot -p hestia_ops < db/data.sql
```

### 5.3 修改数据源配置

编辑 `hestia-server/src/main/resources/application-dev.yml`：

```yaml
hestia:
  datasource:
    host: localhost
    port: 3306
    database: hestia_ops
    username: root
    password: 你的密码
  redis:
    host: localhost
    port: 6379
```

### 5.4 启动后端

```bash
mvn clean install -DskipTests
mvn -pl hestia-server spring-boot:run
```

接口文档：http://localhost:8080/doc.html

### 5.5 启动前端

```bash
cd hestia-admin
npm install
npm run serve      # http://localhost:8888
```

生产构建：

```bash
npm run build      # 产物在 hestia-admin/dist
```

### 5.6 演示账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | 管家 |
| lisi | 123456 | 工程师（水电/空调） |
| wangwu | 123456 | 工程师（电梯/门禁） |
| sunqi | 123456 | 主管 |

---

## 六、数据库设计

19 张表，按领域分组：

| 前缀 | 含义 | 表 |
|---|---|---|
| `sys_` | 组织与用户 | staff、resident、premise、notice |
| `wo_` | 工单域 | order、item、log、dispatch、attachment、category |
| `svc_` | 服务目录 | item、spec、package、package_item、cart |
| `dev_` | 设备 | device |
| `insp_` | 巡检 | plan、task |
| `sla_` | 时效 | rule |

设计约定：

- 金额统一 `DECIMAL(10,2)`，不用浮点
- 状态用 `TINYINT` + 应用层枚举，不用数据库 `ENUM`（迁移成本低）
- 所有业务表带 `create_time / update_time / create_user / update_user`
- 服务项名称、价格在工单明细里做 **快照**，避免服务目录调价影响历史工单

---

## 七、后续可扩展方向

- 工单超时改用 RocketMQ 延迟消息 / Redis ZSet 延迟队列，替代轮询扫描
- 派单引入地理位置，按距离做权重
- 多租户：按 `community_id` 做数据隔离，演进为 SaaS
- 工单附件接入对象存储直传 + 图片压缩
- 引入 SkyWalking 做链路追踪
