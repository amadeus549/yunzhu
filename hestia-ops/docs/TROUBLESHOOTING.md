# 启动故障排查手册

> 适用：云筑 Hestia · 智慧社区设施运维平台
> 项目路径：`D:\yunzhu\hestia-ops`

---

## 快速自检（30 秒定位）

后端启动失败，先按顺序确认这三件事：

| 检查项 | 命令 | 期望结果 |
|---|---|---|
| Java 版本 | `java -version` | **11.x**（不是 17/21/24） |
| Redis 是否在跑 | `netstat -ano \| findstr :6379` | 有 LISTENING |
| 8080 是否被占 | `netstat -ano \| findstr :8080` | 没有输出 |

三条都正常还起不来，再看下一节的具体错误。

---

## 错误一：`找不到符号 变量 log` / `NoSuchFieldError: JCImport`

### 现象

```
[ERROR] AliOssUtil.java:[64,9] 找不到符号
[ERROR]   符号:   变量 log
```

或

```
java.lang.NoSuchFieldError: Class com.sun.tools.javac.tree.JCTree$JCImport
does not have member field 'com.sun.tools.javac.tree.JCTree qualid'
```

### 根因

IDEA 把项目 SDK 设成了 **JDK 24**，而项目是 **Spring Boot 2.7.3 + Lombok**：

1. **Lombok 注解处理器在 JDK 23+ 默认不再自动执行**（`-proc:none` 成为默认），`@Slf4j`、`@Data` 全部失效
2. 旧的 Lombok 1.18.20 本身也不认识 JDK 17+ 的 `javac` 内部结构

### 已做的修复

**① 项目 SDK 改为 JDK 11**

`.idea/misc.xml`：
```xml
<!-- 修复前 -->
<component name="ProjectRootManager" ... languageLevel="JDK_24"
           project-jdk-name="24" project-jdk-type="JavaSDK" />

<!-- 修复后 -->
<component name="ProjectRootManager" ... languageLevel="JDK_11"
           project-jdk-name="11" project-jdk-type="JavaSDK" />
```

**② Lombok 升级到 1.18.46**（支持 JDK 8 ~ 24）

根 `pom.xml`：
```xml
<lombok>1.18.46</lombok>
```

**③ 显式声明注解处理器路径**（关键，没有这条 JDK 23+ 仍然编译失败）

根 `pom.xml` 的 `maven-compiler-plugin`：
```xml
<configuration>
    <source>11</source>
    <target>11</target>
    <encoding>UTF-8</encoding>
    <annotationProcessorPaths>
        <path>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok}</version>
        </path>
    </annotationProcessorPaths>
</configuration>
```

**④ 显式锁死编译级别**

```xml
<properties>
    <java.version>11</java.version>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
</properties>
```

### 如果 IDEA 里还是报错

1. 关掉 IDEA
2. 确认 `.idea/misc.xml` 里是 `project-jdk-name="11"`
3. 重新打开，`File → Project Structure → Project` 确认 SDK = 11、Language level = 11
4. `Build → Rebuild Project`
5. 还不行就 `File → Invalidate Caches → Invalidate and Restart`

### 命令行验证是否修好

```bash
cd /d D:\yunzhu\hestia-ops
set JAVA_HOME=D:\jdk-11
mvn clean install -DskipTests
```

看到 `BUILD SUCCESS` 就说明编译链没问题。

---

## 错误二：`Port 8080 was already in use`

### 现象

```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 8080 was already in use.
```

### 根因

上一次启动的进程没退干净。IDEA 里点「停止」有时只停了 Maven，`spring-boot:run` fork 出来的 java 子进程还活着。

### 解决

**查占用者：**
```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object OwningProcess
```

**杀掉：**
```powershell
$pid = (Get-NetTCPConnection -LocalPort 8080 -State Listen).OwningProcess
Stop-Process -Id $pid -Force
```

或者一条命令看谁占的：
```bash
netstat -ano | findstr :8080
# 最后一列是 PID，然后 taskkill /f /pid <PID>
```

### 换成别的端口（不想杀进程时）

```bash
mvn -pl hestia-server spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

前端 `vue.config.js` 里的代理目标也要跟着改。

---

## 错误三：跑在 51074 而不是 8080

### 现象

后端日志显示 `Tomcat started on port(s): 51074`，但前端访问 `localhost:8080` 连不上。

### 根因

宿主环境里有环境变量 **`SERVER__PORT=51074`**，Spring Boot 的宽松绑定会把它识别为 `server.port`，优先级高于 `application.yml`。

### 解决

**命令行启动时清掉：**
```bash
unset SERVER__PORT
mvn -pl hestia-server spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
```

**IDEA 里启动时：**
`Run → Edit Configurations → Environment variables`，删掉 `SERVER__PORT`，
或在 VM options 加 `-Dserver.port=8080`。

> 经验：命令行参数 `--server.port=8080` 优先级最高，最保险。

---

## 错误四：`ClassNotFoundException: com.hestia.entity.AddressBook`

### 现象

启动时 MyBatis 解析 XML 报错，说找不到 `AddressBook`、`Dish`、`Setmeal`、`Orders` 这些类。

### 根因

`target/classes/mapper/` 里残留了外卖时代的旧 XML（`AddressBookMapper.xml`、`DishMapper.xml` 等）。
源码已经删了，但 `target` 是增量目录，没被清理。

也可能是 IDEA 从 Local History 把旧文件恢复回来了。

### 解决

```bash
cd /d D:\yunzhu\hestia-ops
mvn clean install -DskipTests
```

**必须带 `clean`**。只看 `src` 是干净的没用，`target` 里还可能有一份。

### 检查是否还有残留

```bash
# 源码里旧的 Mapper XML 应该只有 11 个
ls hestia-server/src/main/resources/mapper/

# 编译产物里也应只有 11 个
ls hestia-server/target/classes/mapper/

# 全局搜旧实体引用
grep -rn "AddressBook\|Setmeal\|ShoppingCart" --include=*.xml --include=*.java .
```

正常情况：`src` 和 `target` 里都是 **11 个 XML**。

---

## 错误五：`Unable to connect to Redis`

### 现象

```
io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379
```

### 根因

Redis 没启动。

> 注意：Redis 挂了**不会**阻止 Spring Boot 启动（Lettuce 是懒连接），
> 但会登录失败、派单报错、SLA 预警扫描报异常。

### 解决

```bash
cd /d D:\yunzhu\Redis-x64-3.2.100
redis-server.exe redis.windows.conf
```

另开一个窗口验证：
```bash
redis-cli.exe ping
# 返回 PONG 就对了
```

---

## 错误六：`Access denied for user 'root'@'localhost'`

### 现象

启动时 Druid 初始化数据源失败。

### 根因

MySQL 密码和 `application-dev.yml` 里不一致。

### 解决

检查配置：
```bash
grep -A5 "url:\|username:\|password:" hestia-server/src/main/resources/application-dev.yml
```

当前配置是：
- 库名 `hestia_ops`
- 账号 `root`
- 密码见本地 `application-dev.yml`（不入 git 仓库，勿写进文档）

改了密码就同步改配置。另外确认库存在：
```bash
mysql -uroot -p -e "show databases like 'hestia_ops'"
```

不存在就重新导入：
```bash
mysql -uroot -p < db/schema.sql
mysql -uroot -p hestia_ops < db/data.sql
```

---

## 标准启动顺序

三者的依赖关系：**Redis / MySQL → 后端 → Nginx**

### 1. Redis
```bash
cd /d D:\yunzhu\Redis-x64-3.2.100
redis-server.exe redis.windows.conf
```

### 2. 后端
```bash
cd /d D:\yunzhu\hestia-ops
set JAVA_HOME=D:\jdk-11
mvn -pl hestia-server spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
```

看到这两行就是成功：
```
Tomcat started on port(s): 8080 (http)
Started HestiaApplication in 3.8 seconds
```

### 3. Nginx
```bash
cd /d D:\yunzhu\hestia-nginx
nginx.exe -p "D:/yunzhu/hestia-nginx/"
```

### 4. 访问
http://localhost ｜ 账号 `admin / 123456`

---

## 停止服务

```powershell
# Nginx
Stop-Process -Name nginx -Force -ErrorAction SilentlyContinue

# 后端
$pid = (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($pid) { Stop-Process -Id $pid -Force }

# Redis
$pid = (Get-NetTCPConnection -LocalPort 6379 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($pid) { Stop-Process -Id $pid -Force }
```

---

## 本次修复记录

| 问题 | 修复 | 文件 |
|---|---|---|
| IDEA 用 JDK 24 编译 | SDK 改为 JDK 11 | `.idea/misc.xml` |
| Lombok 1.18.20 不兼容高版本 JDK | 升到 1.18.46 | `pom.xml` |
| JDK 23+ 注解处理器不自动执行 | 显式 `annotationProcessorPaths` | `pom.xml` |
| 编译级别漂移 | 锁死 source/target = 11 | `pom.xml` |
| 8080 端口残留 | 清理残留 java 进程 | — |

备份文件：`.idea/misc.xml.bak`、`pom.xml.bak`（确认无问题后可删）


---

## 错误七：网页登录后一直弹「登录状态已过期，请重新登录」

### 现象
- 账号密码正确，登录接口返回 200 并拿到 token
- 跳进首页立刻弹「登录状态已过期」，点重新登录又回登录页，形成死循环
- 直接 curl 带 token 请求后端却是 200

### 根因
`src/store/index.js` 只挂了 `modules: { user }`，而 `src/store/modules/user.js`
声明了 `namespaced: true`，所以 getter 的**全名**是 `user/token`、`user/name`、`user/roleName`。

`src/utils/request.js` 的请求拦截器却写成：

```js
const token = store.getters.token   // 错误：取到 undefined
```

取到 `undefined` → 所有请求不带 `token` 请求头 → `JwtTokenAdminInterceptor` 抛 401 →
响应拦截器弹「登录状态已过期」。

同理 `src/layout/index.vue` 的 `mapGetters(['name', 'roleName'])` 也是全局取法，
会显示「未登录」。路由守卫直接读 `localStorage`，不受影响。

### 30 秒定位法
1. F12 → Network，随便点一个 `/admin/**` 请求，看 Request Headers 有没有 `token`
2. **有 401 但没有 `token` 头 → 一定是前端注入环节，不是后端 token 过期**
3. curl 验证后端：能登录、带 token 能取数据 → 后端无责

### 已做的修复
| 文件 | 改动 |
|---|---|
| `src/utils/request.js` | `store.getters.token` → `store.getters['user/token']`，并加 `localStorage.getItem('hestia_token')` 兜底 |
| `src/utils/request.js` | 401 弹窗加 `reloginPromptShown` 防重入，避免并发请求弹一摞确认框 |
| `src/layout/index.vue` | `mapGetters(['name','roleName'])` → `mapGetters({ name:'user/name', roleName:'user/roleName' })` |

### 必须重新构建前端（最容易漏的一步）
改完源码**必须 build 并同步到 nginx 静态目录**，否则线上跑的还是旧 JS：

```bash
# 依赖在桌面项目里最全（D:/yunzhu/hestia-admin/node_modules 是残缺的）
cd /d/桌面/cangqiongwaimai/hestia-admin
node node_modules/@vue/cli-service/bin/vue-cli-service.js build
cp -r dist/* /d/yunzhu/hestia-nginx/html/hestia/
```

验证线上文件确实更新（旧写法应搜不到）：

```bash
curl -s http://127.0.0.1/ | grep -o 'app\.[a-f0-9]*\.js'
curl -s http://127.0.0.1/js/app.<hash>.js | grep -o 'getters\["user/token"\]'
```

### 缓存坑（重要）
nginx 对 `*.js` 设了 30 天强缓存。虽然新构建会生成新文件名 hash，
但 `index.html` 若被浏览器缓存，浏览器仍会加载旧的 `app.xxx.js`，看起来像「没修好」。

已在 `hestia-nginx/conf/nginx.conf` 加精确匹配，让 `index.html` 永不缓存：

```nginx
location = /index.html {
    root   html/hestia;
    add_header Cache-Control "no-cache, no-store, must-revalidate";
    add_header Pragma "no-cache";
    add_header Expires "0";
}
```

改完执行 `nginx -t && nginx -s reload`；用户侧第一次打开按 `Ctrl + F5` 强制刷新。


---

## 错误八：代码已修复并部署，浏览器却仍跑旧 JS（缓存顽疾）

### 现象
- 服务端文件已确认是修复版（curl 拉下来的 app.js 含新代码）
- 但登录后所有 `/admin/**` 请求依旧 401，弹「登录状态已过期」
- 弹窗出现时人还在登录页；点「重新登录」再登录也不行

### 证据（nginx access.log）
1. 浏览器 UA 的请求里**从始至终没有一条 `GET /js/app.*.js`** —— 强缓存期内浏览器根本不发网络请求，永远用本地缓存
2. 登录接口 200，登录后的请求全部 401 —— 旧 JS 的 `store.getters.token` 取到 undefined，不带 token 头
3. 同一时刻用 curl 带 token 请求是 200 —— 后端无责

根因组合：**页面从昨晚打开后一直没真正重新加载 + JS 有 30 天强缓存**。
点弹窗的「重新登录」只是 SPA 内部路由跳转，不会重新加载页面。

### 解法
| 手段 | 说明 |
|---|---|
| `Ctrl + F5` | 对所有打开过系统的浏览器窗口各来一次（强制绕过缓存） |
| 无痕窗口 | 完全绕过缓存，适合快速验证 |
| 带查询串打开 | `http://localhost/?v=20261108` —— URL 不同必回源，最省事 |

服务端已加固：`location = /index.html` 返回 `no-cache`，**今后正常刷新即可拿到新版**，本问题不会复发。

### 如何确认浏览器拿到的是新版
1. nginx 日志里应出现浏览器的 `GET /js/app.8ee37315.js 200`
2. F12 → Network → 勾 Disable cache 后刷新，任意 `/admin/**` 请求的 Request Headers 应有 `token`
3. 无头浏览器冒烟：
   ```bash
   "/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe" \
     --headless=new --disable-gpu --dump-dom --virtual-time-budget=8000 \
     "http://127.0.0.1/?v=smoke" | grep -c "登录运维管理端"
   ```
   输出 1 即新版渲染正常。


---

## 错误九：报表等 GET 接口整体返回 400（LocalDate 缺 @DateTimeFormat）

### 现象
- 前端运营报表 5 个接口（sla/trend/category/staff/export）全部 400，前端新旧版本都一样
- 其他接口（工单、员工、通知）全部 200
- 请求形如 `GET /admin/report/sla?begin=2026-10-10&end=2026-11-08`

### 根因（实验证实）
`ReportQueryDTO.begin/end` 是 `LocalDate` 且**没有 `@DateTimeFormat` 注解**。
Spring MVC 对无注解的 `LocalDate` 按**系统本地化格式**解析（zh-CN 为斜杠风格 `2026/10/10`），
前端传 ISO 横杠 `2026-10-10` 直接解析失败 → 400。

| 实验 | 结果 |
|---|---|
| `begin=2026-10-10`（ISO 横杠，前端格式） | 400 |
| `begin=2026/10/10`（本地化斜杠） | 200 |
| 不传日期 | 200 |

### 修复
给**所有走 GET query 绑定**的 DTO 日期字段加注解：
- `ReportQueryDTO.begin/end`：`@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`
- `InspectionTaskPageQueryDTO.beginDate/endDate`：同上
- `WorkOrderPageQueryDTO.beginTime/endTime`：`@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")`（LocalDateTime）

**不需要加**的：`@RequestBody` 的 DTO（DeviceDTO、InspectionPlanDTO、WorkOrderCreateDTO、
WorkOrderSubmitDTO）——它们走 Jackson，不是 Spring MVC 的 converter。

### 排查方法论
1. 先看 access.log 找到 400 的具体 URL 和参数
2. 同参数换日期格式重试（横杠 → 斜杠），**200 就锁定参数绑定问题，与业务无关**
3. 检查 DTO 字段类型与注解：GET 绑定的 LocalDate/LocalDateTime/Date 必须有 @DateTimeFormat
4. Java 改动必须重启后端才生效


---

## 错误十：工单类型分页 500（DTO 缺字段 → MyBatis OGNL 异常）

### 现象
- `/admin/category/page` 500，而同控制器 `/admin/category/list` 200
- 响应体只有通用错误（无具体信息），需查后端控制台堆栈

### 根因（两个叠加 bug，均为苍穹外卖残留）
1. **`CategoryPageQueryDTO` 没有 `bizType` 字段**，而 `WorkOrderCategoryMapper.xml`
   的 pageQuery 写了 `<if test="bizType != null">`。MyBatis 对不存在的属性求值抛
   `There is no getter for property named 'bizType'` → 500。
   直接连库跑 pageQuery 的 SQL 是通的，排除表结构问题。
2. **`CategoryDTO` 也缺 `bizType`/`status`/`icon`**（只有 id/type/name/sort）：
   新增时 biz_type 插 NULL 撞 NOT NULL 约束（新增也会 500）；
   编辑时改业务类型/状态不生效。

### 修复
- `CategoryPageQueryDTO`：删残留 `type`，加 `bizType`
- `CategoryDTO`：删残留 `type`，加 `bizType`、`icon`、`status`
- `CategoryServiceImpl.save`：bizType/status 兜底默认值（防 NOT NULL 约束）

### 排查方法论
1. access.log 锁定「同控制器某方法 500、其他 200」→ 问题在该方法的 SQL/绑定
2. 连库直跑 XML 里的 SQL：**SQL 通 → 参数绑定/OGNL 层问题**
3. XML `<if test>` 里引用的每个属性，DTO 必须有对应 getter
4. 检查 entity 表字段与 DTO 字段的一致性（copyProperties 拷不到就是静默丢失）
5. 苍穹外卖改造项目：DTO/VO/注释里的 `type`（菜品/套餐）、`Dish`、`Setmeal` 字样都是残留信号


---

## 全面体检记录（2026-11-08 14:50）

### 检查维度与结果
| 维度 | 方法 | 结果 |
|---|---|---|
| 全量 GET 接口 | 登录后逐个真实调用 36 个 | 34/36 通过（仅 category/page 待重启生效，已修） |
| Mapper XML 一致性 | 脚本比对 `<if test>`/`#{}` 属性 vs DTO/entity 字段、接口方法 vs XML id | 全部一致 |
| 前端 API vs 后端端点 | 70 个前端 API 与 98 个后端端点正则匹配 | 全覆盖 |
| entity vs 数据库 | 19 张表逐列双向对比（字段→列、NOT NULL 无默认→entity） | 全部一致 |
| 苍穹外卖残留 | grep Dish/Setmeal/AddressBook/Employee/ShoppingCart/OrderDetail | 代码与 mapper 零残留 |
| 前端可疑模式 | getters 全局取法、localStorage key | 无残留，key 一致 |

### 发现并修复
1. **功能空洞：房屋点位（premise）没有管理页面**
   - 后端 list/detail/save/update/delete 五个接口齐全，api/basic.js 封装齐全，
     住户/工单/派单都依赖 sys_premise 数据，但界面无任何维护入口（只能靠 seed）
   - 已补：`views/premise/index.vue`（列表/筛选/新增/编辑/删除，关联住户下拉可空=公共区域）
     + 路由 `premise` + 菜单「房屋点位」
   - 写链路实测：新增 200 → 列表出现且 fullAddress 正确 → 编辑生效 → 删除后数据还原
2. 次要：8 个 API 定义未被页面引用（editPassword/listNotice/unreadCount/getDevice/myTasks/
   premise 系列已被新页面用上）——死代码无害，保留

### 构建注意（再次踩坑记录）
build 跑在**桌面项目**（依赖完整），必须先把 yunzhu 的源码改动同步到桌面项目再 build，
否则构建的是旧代码（本次第一遍 build 就因此产物 hash 未变）。
验证产物是否真的更新：对比 Build hash、检查新页面文案是否存在于 chunk。


---

## 错误十一：接口文档 localhost/doc.html 返回 400

### 现象
- 直连后端 `8080/doc.html` 是 200，经 nginx `80/doc.html` 是 400（Tomcat 错误页）
- 修复 400 后页面能打开，但样式/脚本全 404

### 根因（两个 nginx 转发陷阱叠加）
1. **400 的根因**：`/doc.html` 等 location 没配 `proxy_set_header Host $host`，
   转发时 Host 头变成 upstream 名 `hestia_server`——**下划线违反 RFC 主机名规范**，
   Tomcat 直接拒收返回 400。
   实锤方法：`curl -H "Host: hestia_server" http://127.0.0.1:8080/doc.html` → 400；
   `curl -H "Host: localhost" ...` → 200。
2. **404 的根因**：nginx 正则 location `\.(js|css...)$`（静态缓存规则）**优先级高于**
   普通前缀 location，`/webjars/**/*.js|css` 被抢走去本地磁盘找文件 → 404。
   修复：文档 location 一律加 `^~` 前缀修饰符（命中后不再检查正则）。

### 修复（仅改 nginx.conf，reload 即生效）
四个文档 location（/doc.html、/webjars/、/swagger-resources/、/v2/）统一：
```nginx
location ^~ /doc.html {
    proxy_pass http://hestia_server/doc.html;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

### 附带说明
- `/v2/api-docs` 不带 group 参数是 404，属正常：项目两个 Docket 都命名了分组
  （管理端/住户端），knife4j 页面会自动带 group 请求，无需处理
- `favicon.ico` 404 是 knife4j UI jar 本身未提供，直连后端同样 404，无碍
- 经验：**nginx 转发的每个 location 都应显式配 `proxy_set_header Host $host`**；
  upstream 名尽量不含下划线（或全局统一加 Host 头）


---

## 错误十二：工单类型分页 500 复发（修复只补了一半）

### 现象
- 上一轮修复（DTO 补 bizType）后，`/admin/category/page` 依然 500
- 8080（IDEA 运行）与 8081（Maven 用当前源码起的临时实例）**同时 500**——
  排除「IDEA 旧编译产物」假设，说明源码仍有问题

### 真实根因（从 8081 日志抓到堆栈）
```
ReflectionException: There is no getter for property named 'status'
in 'class com.hestia.dto.CategoryPageQueryDTO'
```
XML pageQuery 有**三个**动态条件：`name`、`bizType`、`status`。
上一轮只补了 `bizType`，漏了 `status`——修复不完整。

### 教训：上一轮静态检查为什么漏报
旧检查脚本用 `<select...>(.*?)</\1>` 反向引用闭合匹配语句体，
pageQuery 内嵌**子查询 `(select count(*)...)`** 时闭合匹配错位，语句体不完整，
`<if test="status">` 被漏掉。修正版改为「语句起点 + 查找对应闭合标签切片」，
31 条语句全部覆盖。

### 修复
- `CategoryPageQueryDTO` 补 `status`（Integer）
- 验证：8081 实例三种参数组合（空/bizType/status）全部 200
- 全量静态检查重跑：31 条语句一致 ✓
