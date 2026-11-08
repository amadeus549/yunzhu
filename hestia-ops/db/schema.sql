-- ============================================================
-- 云筑 Hestia · 智慧社区设施运维平台
-- 数据库 schema
-- MySQL 8.0 / utf8mb4
-- 设计说明：
--   1. 不建物理外键，靠应用层保证一致性（便于后续分库分表）
--   2. 所有业务表带 create_time / update_time / create_user / update_user
--   3. 金额统一 DECIMAL(10,2)，避免浮点误差
--   4. 状态字段用 TINYINT + 枚举类映射，不用数据库 ENUM
-- ============================================================

DROP DATABASE IF EXISTS hestia_ops;
CREATE DATABASE hestia_ops DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hestia_ops;

-- ------------------------------------------------------------
-- 1. 员工（管家 / 工程师 / 主管 / 客服）
-- ------------------------------------------------------------
CREATE TABLE sys_staff (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    staff_no       VARCHAR(32)  NOT NULL COMMENT '工号',
    name           VARCHAR(32)  NOT NULL COMMENT '姓名',
    username       VARCHAR(32)  NOT NULL COMMENT '登录账号',
    password       VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    phone          VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    avatar         VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    role           TINYINT      NOT NULL DEFAULT 2 COMMENT '角色：1管家 2工程师 3主管 4客服',
    skill_tags     VARCHAR(255) DEFAULT NULL COMMENT '技能标签，逗号分隔，如 水电,空调,电梯',
    workload_limit INT          NOT NULL DEFAULT 5 COMMENT '同时在手工单上限',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time    DATETIME     DEFAULT NULL,
    update_time    DATETIME     DEFAULT NULL,
    create_user    BIGINT       DEFAULT NULL,
    update_user    BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_staff_no (staff_no),
    UNIQUE KEY uk_username (username),
    KEY idx_role_status (role, status)
) ENGINE = InnoDB COMMENT ='员工';

-- ------------------------------------------------------------
-- 2. 住户
-- ------------------------------------------------------------
CREATE TABLE sys_resident (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid      VARCHAR(64)  DEFAULT NULL COMMENT '微信openid',
    name        VARCHAR(32)  NOT NULL COMMENT '姓名',
    phone       VARCHAR(20)  NOT NULL COMMENT '手机号',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone),
    KEY idx_openid (openid)
) ENGINE = InnoDB COMMENT ='住户';

-- ------------------------------------------------------------
-- 3. 房屋 / 点位（报修地址、设备安装位置）
-- ------------------------------------------------------------
CREATE TABLE sys_premise (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    resident_id BIGINT       DEFAULT NULL COMMENT '所属住户',
    community   VARCHAR(64)  NOT NULL COMMENT '小区名称',
    building    VARCHAR(32)  NOT NULL COMMENT '楼栋',
    unit        VARCHAR(16)  DEFAULT NULL COMMENT '单元',
    room_no     VARCHAR(32)  NOT NULL COMMENT '房号',
    premise_type TINYINT     NOT NULL DEFAULT 1 COMMENT '类型：1住宅 2商铺 3公共区域',
    area        DECIMAL(8,2) DEFAULT NULL COMMENT '建筑面积（㎡）',
    is_default  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认地址',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_resident (resident_id),
    KEY idx_location (community, building, room_no)
) ENGINE = InnoDB COMMENT ='房屋/点位';

-- ------------------------------------------------------------
-- 4. 工单类型（报修 / 投诉 / 咨询 / 巡检异常）
-- ------------------------------------------------------------
CREATE TABLE wo_category (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(32)  NOT NULL COMMENT '类型名称',
    biz_type    TINYINT      NOT NULL DEFAULT 1 COMMENT '业务类型：1报修 2投诉 3咨询 4巡检异常',
    icon        VARCHAR(255) DEFAULT NULL COMMENT '图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    create_user BIGINT       DEFAULT NULL,
    update_user BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_biz_type (biz_type, status)
) ENGINE = InnoDB COMMENT ='工单类型';

-- ------------------------------------------------------------
-- 5. 服务项（可被派单执行的原子服务）
-- ------------------------------------------------------------
CREATE TABLE svc_item (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id    BIGINT        NOT NULL COMMENT '所属工单类型',
    code           VARCHAR(32)   NOT NULL COMMENT '服务编码',
    name           VARCHAR(64)   NOT NULL COMMENT '服务名称',
    description    VARCHAR(500)  DEFAULT NULL COMMENT '服务说明',
    image          VARCHAR(255)  DEFAULT NULL COMMENT '图片',
    price          DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '基础价格',
    unit           VARCHAR(16)   NOT NULL DEFAULT '次' COMMENT '计价单位：次/㎡/小时/件',
    duration_min   INT           NOT NULL DEFAULT 60 COMMENT '预计耗时（分钟）',
    required_skill VARCHAR(64)   DEFAULT NULL COMMENT '所需技能标签',
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
    sort           INT           NOT NULL DEFAULT 0,
    create_time    DATETIME      DEFAULT NULL,
    update_time    DATETIME      DEFAULT NULL,
    create_user    BIGINT        DEFAULT NULL,
    update_user    BIGINT        DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_category (category_id, status)
) ENGINE = InnoDB COMMENT ='服务项';

-- ------------------------------------------------------------
-- 6. 服务规格（同一服务项的不同计价档，如 1.5匹/3匹空调）
-- ------------------------------------------------------------
CREATE TABLE svc_spec (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    item_id     BIGINT        NOT NULL COMMENT '服务项ID',
    name        VARCHAR(32)   NOT NULL COMMENT '规格名称',
    price       DECIMAL(10,2) NOT NULL COMMENT '该规格价格',
    duration_min INT          NOT NULL DEFAULT 60 COMMENT '该规格耗时',
    sort        INT           NOT NULL DEFAULT 0,
    create_time DATETIME      DEFAULT NULL,
    update_time DATETIME      DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_item (item_id)
) ENGINE = InnoDB COMMENT ='服务规格';

-- ------------------------------------------------------------
-- 7. 服务包（维保套餐 / 年度服务包）
-- ------------------------------------------------------------
CREATE TABLE svc_package (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id    BIGINT        NOT NULL COMMENT '所属工单类型',
    code           VARCHAR(32)   NOT NULL COMMENT '套餐编码',
    name           VARCHAR(64)   NOT NULL COMMENT '套餐名称',
    description    VARCHAR(500)  DEFAULT NULL,
    image          VARCHAR(255)  DEFAULT NULL,
    price          DECIMAL(10,2) NOT NULL COMMENT '套餐价',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    valid_days     INT           NOT NULL DEFAULT 365 COMMENT '有效期（天）',
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
    create_time    DATETIME      DEFAULT NULL,
    update_time    DATETIME      DEFAULT NULL,
    create_user    BIGINT        DEFAULT NULL,
    update_user    BIGINT        DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_category (category_id, status)
) ENGINE = InnoDB COMMENT ='服务包';

-- ------------------------------------------------------------
-- 8. 服务包明细
-- ------------------------------------------------------------
CREATE TABLE svc_package_item (
    id         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    package_id BIGINT NOT NULL COMMENT '服务包ID',
    item_id    BIGINT NOT NULL COMMENT '服务项ID',
    quantity   INT    NOT NULL DEFAULT 1 COMMENT '单次数量',
    times      INT    NOT NULL DEFAULT 1 COMMENT '包含次数',
    PRIMARY KEY (id),
    KEY idx_package (package_id),
    KEY idx_item (item_id)
) ENGINE = InnoDB COMMENT ='服务包明细';

-- ------------------------------------------------------------
-- 9. SLA 时效规则
-- ------------------------------------------------------------
CREATE TABLE sla_rule (
    id               BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name             VARCHAR(64) NOT NULL COMMENT '规则名称',
    priority         TINYINT     NOT NULL COMMENT '适用优先级：1低 2普通 3高 4紧急',
    response_minutes INT         NOT NULL COMMENT '响应时限（分钟）',
    resolve_minutes  INT         NOT NULL COMMENT '解决时限（分钟）',
    escalate_role    TINYINT     NOT NULL DEFAULT 3 COMMENT '超时升级到角色',
    status           TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time      DATETIME    DEFAULT NULL,
    update_time      DATETIME    DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_priority (priority)
) ENGINE = InnoDB COMMENT ='SLA时效规则';

-- ------------------------------------------------------------
-- 10. 工单主表
-- ------------------------------------------------------------
CREATE TABLE wo_order (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no          VARCHAR(32)   NOT NULL COMMENT '工单号',
    category_id       BIGINT        NOT NULL COMMENT '工单类型',
    premise_id        BIGINT        DEFAULT NULL COMMENT '房屋/点位',
    resident_id       BIGINT        DEFAULT NULL COMMENT '报修住户',
    title             VARCHAR(100)  NOT NULL COMMENT '标题',
    description       VARCHAR(1000) DEFAULT NULL COMMENT '问题描述',
    priority          TINYINT       NOT NULL DEFAULT 2 COMMENT '优先级：1低 2普通 3高 4紧急',
    status            TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0已取消 1待受理 2待派单 3已派单 4已接单 5处理中 6待验收 7已完成 8已驳回',
    source            TINYINT       NOT NULL DEFAULT 1 COMMENT '来源：1住户自助 2管家代报 3巡检发现 4电话',
    sla_rule_id       BIGINT        DEFAULT NULL COMMENT 'SLA规则',
    response_deadline DATETIME      DEFAULT NULL COMMENT '响应截止时间',
    resolve_deadline  DATETIME      DEFAULT NULL COMMENT '解决截止时间',
    expect_time       DATETIME      DEFAULT NULL COMMENT '期望上门时间',
    assignee_id       BIGINT        DEFAULT NULL COMMENT '当前处理工程师',
    dispatcher_id     BIGINT        DEFAULT NULL COMMENT '派单人',
    accept_time       DATETIME      DEFAULT NULL COMMENT '接单时间',
    start_time        DATETIME      DEFAULT NULL COMMENT '开始处理时间',
    finish_time       DATETIME      DEFAULT NULL COMMENT '完工时间',
    complete_time     DATETIME      DEFAULT NULL COMMENT '验收完成时间',
    cancel_time       DATETIME      DEFAULT NULL COMMENT '取消时间',
    total_amount      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '费用合计',
    response_breached TINYINT       NOT NULL DEFAULT 0 COMMENT '响应是否超时',
    resolve_breached  TINYINT       NOT NULL DEFAULT 0 COMMENT '解决是否超时',
    is_rework         TINYINT       NOT NULL DEFAULT 0 COMMENT '是否返工',
    rework_count      INT           NOT NULL DEFAULT 0 COMMENT '返工次数',
    rating            TINYINT       DEFAULT NULL COMMENT '满意度评分 1-5',
    comment           VARCHAR(500)  DEFAULT NULL COMMENT '评价内容',
    create_time       DATETIME      DEFAULT NULL,
    update_time       DATETIME      DEFAULT NULL,
    create_user       BIGINT        DEFAULT NULL,
    update_user       BIGINT        DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_status_priority (status, priority),
    KEY idx_assignee (assignee_id, status),
    KEY idx_resident (resident_id),
    KEY idx_create_time (create_time),
    KEY idx_resolve_deadline (resolve_deadline, status)
) ENGINE = InnoDB COMMENT ='工单';

-- ------------------------------------------------------------
-- 11. 工单明细（一张工单可包含多个服务项）
-- ------------------------------------------------------------
CREATE TABLE wo_item (
    id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id  BIGINT        NOT NULL COMMENT '工单ID',
    item_id   BIGINT        NOT NULL COMMENT '服务项ID',
    item_name VARCHAR(64)   NOT NULL COMMENT '服务项名称快照',
    spec_id   BIGINT        DEFAULT NULL COMMENT '规格ID',
    spec_name VARCHAR(32)   DEFAULT NULL COMMENT '规格名称快照',
    price     DECIMAL(10,2) NOT NULL COMMENT '单价快照',
    quantity  INT           NOT NULL DEFAULT 1 COMMENT '数量',
    amount    DECIMAL(10,2) NOT NULL COMMENT '小计',
    PRIMARY KEY (id),
    KEY idx_order (order_id)
) ENGINE = InnoDB COMMENT ='工单明细';

-- ------------------------------------------------------------
-- 12. 工单流转日志（状态机审计）
-- ------------------------------------------------------------
CREATE TABLE wo_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT       NOT NULL COMMENT '工单ID',
    operator_id   BIGINT       DEFAULT NULL COMMENT '操作人ID',
    operator_type TINYINT      NOT NULL DEFAULT 1 COMMENT '操作人类型：1员工 2住户 3系统',
    action        VARCHAR(32)  NOT NULL COMMENT '动作：CREATE/ACCEPT/DISPATCH/...',
    from_status   TINYINT      DEFAULT NULL COMMENT '变更前状态',
    to_status     TINYINT      DEFAULT NULL COMMENT '变更后状态',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time   DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_order (order_id, create_time)
) ENGINE = InnoDB COMMENT ='工单流转日志';

-- ------------------------------------------------------------
-- 13. 派单记录（一次工单可能有多次派单/拒单）
-- ------------------------------------------------------------
CREATE TABLE wo_dispatch (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT       NOT NULL COMMENT '工单ID',
    staff_id      BIGINT       NOT NULL COMMENT '被派工程师',
    dispatch_type TINYINT      NOT NULL DEFAULT 1 COMMENT '派单方式：1手动 2智能',
    match_score   INT          DEFAULT NULL COMMENT '智能匹配得分',
    reason        VARCHAR(255) DEFAULT NULL COMMENT '派单/拒单原因',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1待接单 2已接单 3已拒单 4已取消',
    dispatch_time DATETIME     DEFAULT NULL COMMENT '派单时间',
    accept_time   DATETIME     DEFAULT NULL COMMENT '接单时间',
    reject_time   DATETIME     DEFAULT NULL COMMENT '拒单时间',
    create_time   DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_order (order_id),
    KEY idx_staff (staff_id, status)
) ENGINE = InnoDB COMMENT ='派单记录';

-- ------------------------------------------------------------
-- 14. 工单附件
-- ------------------------------------------------------------
CREATE TABLE wo_attachment (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id      BIGINT       NOT NULL COMMENT '工单ID',
    file_type     TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1图片 2视频 3文件',
    url           VARCHAR(500) NOT NULL COMMENT '文件地址',
    uploader_id   BIGINT       DEFAULT NULL COMMENT '上传人',
    uploader_type TINYINT      NOT NULL DEFAULT 2 COMMENT '上传人类型：1员工 2住户',
    create_time   DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_order (order_id)
) ENGINE = InnoDB COMMENT ='工单附件';

-- ------------------------------------------------------------
-- 15. 设备台账
-- ------------------------------------------------------------
CREATE TABLE dev_device (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_no          VARCHAR(32)  NOT NULL COMMENT '设备编号',
    name               VARCHAR(64)  NOT NULL COMMENT '设备名称',
    category           VARCHAR(32)  NOT NULL COMMENT '设备类别：电梯/水泵/配电/消防/门禁/监控',
    brand              VARCHAR(32)  DEFAULT NULL COMMENT '品牌',
    model              VARCHAR(64)  DEFAULT NULL COMMENT '型号',
    premise_id         BIGINT       DEFAULT NULL COMMENT '安装点位',
    location           VARCHAR(100) DEFAULT NULL COMMENT '安装位置描述',
    install_date       DATE         DEFAULT NULL COMMENT '安装日期',
    warranty_end       DATE         DEFAULT NULL COMMENT '质保到期',
    maintain_cycle_days INT         NOT NULL DEFAULT 90 COMMENT '维保周期（天）',
    last_maintain_date DATE         DEFAULT NULL COMMENT '上次维保日期',
    next_maintain_date DATE         DEFAULT NULL COMMENT '下次维保日期',
    status             TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 2维修中 3停用 4报废',
    qr_code            VARCHAR(255) DEFAULT NULL COMMENT '二维码地址',
    remark             VARCHAR(500) DEFAULT NULL,
    create_time        DATETIME     DEFAULT NULL,
    update_time        DATETIME     DEFAULT NULL,
    create_user        BIGINT       DEFAULT NULL,
    update_user        BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_no (device_no),
    KEY idx_category_status (category, status),
    KEY idx_next_maintain (next_maintain_date)
) ENGINE = InnoDB COMMENT ='设备台账';

-- ------------------------------------------------------------
-- 16. 巡检计划
-- ------------------------------------------------------------
CREATE TABLE insp_plan (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name               VARCHAR(64)  NOT NULL COMMENT '计划名称',
    cycle_type         TINYINT      NOT NULL DEFAULT 2 COMMENT '周期类型：1每日 2每周 3每月 4自定义',
    cycle_value        VARCHAR(32)  DEFAULT NULL COMMENT '周期值：如 MON,WED,FRI 或 1,15',
    device_category    VARCHAR(32)  DEFAULT NULL COMMENT '设备类别范围，逗号分隔',
    executor_id        BIGINT       NOT NULL COMMENT '默认执行人',
    start_date         DATE         NOT NULL COMMENT '生效日期',
    generate_ahead_days INT         NOT NULL DEFAULT 1 COMMENT '提前多少天生成任务',
    last_generate_date DATE         DEFAULT NULL COMMENT '上次生成任务的日期',
    next_generate_date DATE         DEFAULT NULL COMMENT '下次生成日期',
    status             TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time        DATETIME     DEFAULT NULL,
    update_time        DATETIME     DEFAULT NULL,
    create_user        BIGINT       DEFAULT NULL,
    update_user        BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_status_next (status, next_generate_date)
) ENGINE = InnoDB COMMENT ='巡检计划';

-- ------------------------------------------------------------
-- 17. 巡检任务
-- ------------------------------------------------------------
CREATE TABLE insp_task (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_no       VARCHAR(32)  NOT NULL COMMENT '任务号',
    plan_id       BIGINT       NOT NULL COMMENT '所属计划',
    device_id     BIGINT       DEFAULT NULL COMMENT '巡检设备',
    executor_id   BIGINT       NOT NULL COMMENT '执行人',
    plan_date     DATE         NOT NULL COMMENT '计划日期',
    actual_time   DATETIME     DEFAULT NULL COMMENT '实际执行时间',
    result        TINYINT      DEFAULT NULL COMMENT '结果：1正常 2异常 3未执行',
    abnormal_desc VARCHAR(500) DEFAULT NULL COMMENT '异常说明',
    order_id      BIGINT       DEFAULT NULL COMMENT '异常转工单ID',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1待执行 2已完成 3已逾期',
    create_time   DATETIME     DEFAULT NULL,
    update_time   DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_no (task_no),
    UNIQUE KEY uk_plan_device_date (plan_id, device_id, plan_date),
    KEY idx_executor (executor_id, status),
    KEY idx_plan_date (plan_date)
) ENGINE = InnoDB COMMENT ='巡检任务';

-- ------------------------------------------------------------
-- 18. 住户服务清单（提交工单前的草稿篮）
-- ------------------------------------------------------------
CREATE TABLE svc_cart (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    resident_id BIGINT   NOT NULL COMMENT '住户ID',
    item_id     BIGINT   NOT NULL COMMENT '服务项ID',
    spec_id     BIGINT   DEFAULT NULL COMMENT '规格ID',
    quantity    INT      NOT NULL DEFAULT 1 COMMENT '数量',
    create_time DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resident_item_spec (resident_id, item_id, spec_id)
) ENGINE = InnoDB COMMENT ='住户服务清单';

-- ------------------------------------------------------------
-- 19. 站内通知（WebSocket 消息落库，保证可靠投递）
-- ------------------------------------------------------------
CREATE TABLE sys_notice (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    receiver_id BIGINT       NOT NULL COMMENT '接收人ID',
    receiver_type TINYINT    NOT NULL DEFAULT 1 COMMENT '接收人类型：1员工 2住户',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    content     VARCHAR(500) DEFAULT NULL COMMENT '内容',
    biz_type    TINYINT      NOT NULL DEFAULT 1 COMMENT '业务类型：1派单 2超时预警 3验收 4系统',
    biz_id      BIGINT       DEFAULT NULL COMMENT '业务ID',
    is_read     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_receiver (receiver_id, receiver_type, is_read)
) ENGINE = InnoDB COMMENT ='站内通知';
