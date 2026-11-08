# 云筑 Hestia · 智慧社区设施运维平台

一套面向社区物业的设施运维管理系统：把报修工单、SLA 时效监控、智能派单、设备台账与巡检闭环装进一套系统管理。项目基于苍穹外卖（sky-take-out）工程结构与部署模型改造而来，覆盖前后端与部署的完整链路。

- **演示账号**：`admin` / `123456`（张明 · 管家角色）
- **访问入口**：http://localhost （管理端 Web）
- **接口文档**：http://localhost/doc.html （knife4j）

---

## 1. 系统架构

```
浏览器 ──HTTP──▶ hestia-nginx (:80)
                   ├── 静态页面  ◀── hestia-admin 构建产物 (dist/)
                   └── /admin/** /resident/** /ws/** ──proxy──▶ hestia-ops (:8080)
                                                                  ├── MySQL  :3306  (hestia_ops)
                                                                  └── Redis  :6379  (缓存/任务)
```

| 目录 | 内容 | 是否需要启动 |
|---|---|---|
| `hestia-ops` | Java 后端（Spring Boot 多模块），所有业务接口 | **需要**（IDEA 启动 `HestiaApplication`） |
| `hestia-admin` | Vue2 管理端**源码**，`npm run build` 出静态文件 | 不需要，构建后由 nginx 托管 |
| `hestia-resident-app` | 住户端**源码**（uni-app），一套代码编译 H5 + 微信小程序 | 不需要，构建后由 nginx 托管 |
| `hestia-nginx` | Nginx：静态托管 + 接口反向代理 + WebSocket | 常驻运行 |

> 与苍穹外卖的区别：前端从"现成静态文件扔进 nginx"升级为**独立源码工程 + 构建发布**；接口路径不再经 `/api` 重写，前端请求路径与后端 `@RequestMapping` 完全一致；nginx 增加了 gzip、静态资源长缓存（文件名带 hash）、`index.html` 禁缓存等工程化配置。

## 2. 技术栈

| 层 | 技术 |
|---|---|
| 后端 | JDK 11 · Spring Boot 2.7.3 · MyBatis + PageHelper · JWT（24h）· Redis（工单/巡检/SLA 缓存）· WebSocket（站内通知）· knife4j 3.0.2 · Lombok 1.18.46 |
| 前端 | Vue 2.7 · Vue Router 3（hash 模式）· Vuex 3 · Element UI 2.15 · axios · ECharts 5 · Sass |
| 数据库 | MySQL 8.0，库名 `hestia_ops`，19 张表（`sys_*` 基础数据、`wo_*` 工单域、`svc_*` 服务域、`dev_*` 设备、`insp_*` 巡检、`sla_rule`） |
| 部署 | Nginx 1.20 · Windows 本机（也可平移到 Linux） |

后端 Maven 模块：`hestia-common`（工具/常量）、`hestia-pojo`（entity/dto/vo）、`hestia-server`（启动与业务）。`db/` 内含建库脚本，`docs/` 内含运维文档。

## 3. 功能模块（管理端 13 个页面）

| 菜单 | 说明 |
|---|---|
| 工作台 | 今日概览、待办工单、7 日趋势、状态分布 |
| 工单管理 | 工单全生命周期：创建 → 受理 → 派单（手动/自动）→ 处理 → 关闭；SLA 响应/解决时限监控 |
| 派单看板 | 待派工单按类型聚合，一键派给合适工程师 |
| 设备台账 | 设备档案、维保到期提醒（`maintain-due`） |
| 巡检计划 | 周期计划配置、手动生成巡检任务 |
| 巡检任务 | 任务执行/异常登记，异常可直接转工单 |
| 服务项 / 服务包 | 维修服务目录与套餐组合（住户端商城数据基础） |
| 工单类型 | 类型字典（业务类型：报修/投诉建议），关联服务项计数 |
| 住户管理 | 住户档案、名下房屋数 |
| 房屋点位 | 小区/楼栋/房号维护，住宅·商铺·公共区域三类，可关联住户 |
| 员工管理 | 工程师/管家账号与启停用 |
| 运营报表 | SLA 达成率、工单趋势、类型分布、工程师绩效，支持导出 Excel |

**住户端**（`hestia-resident-app`，uni-app）：手机号 + 验证码登录（演示验证码 `123456`，演示住户 `13800000001` 陈建国）、我要报修（选类型/房屋/服务项）、我的工单（详情/催单/验收评价/驳回/取消）、服务清单、我的房屋、消息通知。

- **H5 版**：http://localhost/m/ （手机或浏览器打开，已由 nginx 托管）
- **微信小程序版**：构建产物在 `hestia-resident-app/dist/build/mp-weixin`，用微信开发者工具"导入项目"选择该目录即可运行
- 住户端接口前缀 `/resident/**`，token 请求头名为 `authentication`（与管理端 `token` 区分）

## 4. 快速开始

环境要求：JDK 11（`D:\jdk-11`）、MySQL 8.0、Redis（Windows 版可用 Redis-x64-3.2.100）、Node.js 16+（仅构建前端时需要）、IntelliJ IDEA。

**首次准备**

1. 建库：执行 `hestia-ops/db/` 下的脚本，生成 `hestia_ops` 库与演示数据
2. 环境配置：复制 `hestia-server/src/main/resources/application-dev.yml.example` 为 `application-dev.yml`，填入本机数据库/Redis/OSS 信息（该文件含密码，已被 git 忽略，不会上传）
3. 前端构建（若 `hestia-nginx/html/hestia` 已有产物可跳过）：
   ```bash
   cd hestia-admin
   npm install
   npm run build
   cp -r dist/* ../hestia-nginx/html/hestia/
   ```

**每次启动（按顺序）**

| 步骤 | 操作 | 验证 |
|---|---|---|
| 1. Redis | 双击 `redis-server.exe`（或已在服务中） | 端口 6379 |
| 2. 后端 | IDEA 打开 `hestia-ops`，运行 `HestiaApplication` | 日志 `Started HestiaApplication`，端口 8080 |
| 3. Nginx | `hestia-nginx/nginx.exe`（一般常驻） | 端口 80 |
| 4. 访问 | 浏览器打开 http://localhost | 登录页出现，`admin / 123456` 可登录 |

> 停止后端时注意：`spring-boot:run` 会 fork Java 子进程，若提示 `Port 8080 was already in use`，用 `netstat -ano | findstr :8080` 找到残留 PID 并 `taskkill /f /pid <PID>`。

## 5. 开发与发布流程（重要）

**改后端（Java）**

1. 修改 `hestia-ops` 中代码
2. IDEA 重启 `HestiaApplication` 即生效
3. 命令行方式：
   ```bash
   cd /d D:\yunzhu\hestia-ops
   set JAVA_HOME=D:\jdk-11
   mvn -pl hestia-server spring-boot:run -DskipTests
   ```

**改前端（Vue）**

1. 修改 `hestia-admin/src` 中代码
2. 重新构建：`npm run build`（依赖装在 `D:\桌面\cangqiongwaimai\hestia-admin\node_modules`）
3. 将 `dist/` 全量同步到 `hestia-nginx/html/hestia/`
4. 浏览器普通刷新即可（`index.html` 已配置禁缓存；js/css 文件名带 hash 自动换新）

> ⚠️ 只改源码不重新构建，线上不会生效；只构建不同步到 nginx，同样不生效。验证方法：看 `html/hestia/index.html` 引用的 `app.<hash>.js` 是否变化。

**GET 接口的日期参数约定**：`LocalDate` 字段传 `yyyy-MM-dd`，`LocalDateTime` 字段传 `yyyy-MM-dd HH:mm:ss`（DTO 已通过 `@DateTimeFormat` 声明，勿随意改动）。

## 6. 配置速查

| 项 | 值 | 位置 |
|---|---|---|
| 后端端口 | 8080 | `application.yml` |
| 前端开发端口 | 8888（`npm run serve` 时） | `vue.config.js` |
| 数据库 | `localhost:3306/hestia_ops` | `application-dev.yml` |
| Redis | `localhost:6379`，无密码，db 0 | `application-dev.yml` |
| JWT | 管理端/住户端独立密钥，有效期 24h | `application.yml` → `hestia.jwt` |
| nginx 静态目录 | `html/hestia` | `hestia-nginx/conf/nginx.conf` |
| 阿里云 OSS | 报修图片上传（未配置时接口返回明确提示） | `application-dev.yml` |

## 7. 常见问题

启动失败、接口 400/500、页面缓存不生效等**十类已踩过的坑**及排查方法，见 [`hestia-ops/docs/TROUBLESHOOTING.md`](hestia-ops/docs/TROUBLESHOOTING.md)，摘要：

| 症状 | 一句话原因 |
|---|---|
| 找不到符号变量 log | IDEA 用了 JDK 24 / Lombok 注解处理器未生效，用 JDK 11 |
| Port 8080 was already in use | 上次启动的 Java 子进程残留 |
| 服务跑在 51074 | 环境变量 `SERVER__PORT` 覆盖，命令行显式指定 `--server.port=8080` |
| 登录后一直弹"登录状态已过期" | 浏览器缓存旧 JS（强缓存不发请求），Ctrl+F5 或带 `?v=xxx` 打开 |
| 报表接口 400 | 日期参数格式与 `@DateTimeFormat` 不匹配 |
| 分页接口 500 | XML `<if test>` 引用了 DTO 不存在的属性 |
| 改了前端不生效 | 未重新 build 或未同步到 nginx（本文件第 5 节） |

## 8. 目录结构

```
├─ hestia-ops/                 # 后端（IDEA 打开这个）
│  ├─ hestia-server/           #   启动模块：controller/service/mapper/task
│  ├─ hestia-pojo/             #   entity / dto / vo
│  ├─ hestia-common/           #   通用工具、统一返回、异常、JWT
│  ├─ db/                      #   建库脚本
│  └─ docs/                    #   TROUBLESHOOTING.md 排查手册
├─ hestia-admin/               # 前端源码（Vue2）
│  ├─ src/api/                 #   按域拆分的接口封装（70 个 API）
│  ├─ src/views/               #   13 个业务页面
│  ├─ src/router/index.js      #   路由与菜单
│  ├─ src/store/modules/user.js#   登录态（getter 全名 user/token）
│  └─ dist/                    #   构建产物（构建后同步给 nginx）
├─ hestia-nginx/
│  ├─ conf/nginx.conf          #   静态托管 + 反向代理 + 缓存策略
│  └─ html/hestia/             #   前端产物（浏览器实际访问的文件）
└─ Redis-x64-3.2.100/          # 本机 Redis
```

## 9. 约定与注意

- 所有 GET 分页接口统一 `page` / `pageSize` 参数，返回 `PageResult{total, records}`
- 统一响应体 `Result{code, msg, data}`，`code=1` 成功；401 表示未登录/token 过期，前端统一弹窗处理
- 管理端接口一律 `/admin/**`，由 `JwtTokenAdminInterceptor` 校验请求头 `token`；住户端 `/resident/**` 使用独立密钥
- MyBatis 开启驼峰映射；XML 中 `<if test>` 引用的属性必须存在于对应 DTO（有 getter），否则运行时 500
- 双份代码目录：`D:\yunzhu` 为运行/开发目录，桌面项目为其同步副本；**改动后记得双向同步**
