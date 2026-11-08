package com.hestia.enumeration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工单状态机。
 *
 * <p>状态流转路径：
 * <pre>
 *  待受理(1) --受理--> 待派单(2) --派单--> 已派单(3) --接单--> 已接单(4)
 *      |                   |                  |
 *      |                   |                  +--拒单--> 待派单(2)
 *      +-------取消--------+------------------+-------------> 已取消(0)
 *
 *  已接单(4) --开始处理--> 处理中(5) --提交完工--> 待验收(6)
 *  待验收(6) --验收通过--> 已完成(7)
 *  待验收(6) --验收驳回--> 已驳回(8) --重新处理--> 处理中(5)
 * </pre>
 *
 * <p>设计要点：
 * <ul>
 *   <li>状态码与数据库 tinyint 一一对应，避免数据库 ENUM 的迁移成本</li>
 *   <li>所有状态变更必须经过 {@link #canTransitTo(WorkOrderStatus)} 校验，杜绝非法跳转</li>
 *   <li>{@link #allowedNext()} 直接给前端渲染操作按钮，前后端共用一套规则</li>
 * </ul>
 */
public enum WorkOrderStatus {

    /** 已取消：住户或管家主动撤销，终态 */
    CANCELLED(0, "已取消"),
    /** 待受理：工单已提交，等待管家受理 */
    PENDING(1, "待受理"),
    /** 待派单：管家已受理，等待指派工程师 */
    PENDING_DISPATCH(2, "待派单"),
    /** 已派单：已指派工程师，等待接单 */
    DISPATCHED(3, "已派单"),
    /** 已接单：工程师接单，尚未上门 */
    ACCEPTED(4, "已接单"),
    /** 处理中：工程师已上门开始处理 */
    PROCESSING(5, "处理中"),
    /** 待验收：工程师提交完工，等待住户验收 */
    PENDING_ACCEPT(6, "待验收"),
    /** 已完成：住户验收通过，终态 */
    COMPLETED(7, "已完成"),
    /** 已驳回：验收不通过，等待返工 */
    REJECTED(8, "已驳回");

    private final int code;
    private final String desc;

    WorkOrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 合法流转表。key 为当前状态，value 为允许到达的下一个状态集合。
     * 使用 EnumMap + EnumSet，查询 O(1)，且类型安全。
     */
    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> ALLOWED;

    static {
        Map<WorkOrderStatus, Set<WorkOrderStatus>> m = new EnumMap<>(WorkOrderStatus.class);
        m.put(CANCELLED, EnumSet.noneOf(WorkOrderStatus.class));
        m.put(PENDING, EnumSet.of(PENDING_DISPATCH, CANCELLED));
        m.put(PENDING_DISPATCH, EnumSet.of(DISPATCHED, CANCELLED));
        // 拒单后退回待派单，形成闭环而不是直接取消
        m.put(DISPATCHED, EnumSet.of(ACCEPTED, PENDING_DISPATCH));
        m.put(ACCEPTED, EnumSet.of(PROCESSING));
        m.put(PROCESSING, EnumSet.of(PENDING_ACCEPT));
        m.put(PENDING_ACCEPT, EnumSet.of(COMPLETED, REJECTED));
        // 返工：驳回后重新进入处理中
        m.put(REJECTED, EnumSet.of(PROCESSING));
        m.put(COMPLETED, EnumSet.noneOf(WorkOrderStatus.class));
        ALLOWED = Collections.unmodifiableMap(m);
    }

    /**
     * 判断当前状态是否可以流转到目标状态
     */
    public boolean canTransitTo(WorkOrderStatus target) {
        if (target == null) {
            return false;
        }
        return ALLOWED.getOrDefault(this, Collections.emptySet()).contains(target);
    }

    /**
     * 当前状态允许到达的下一批状态码，供前端渲染操作按钮
     */
    public List<Integer> allowedNext() {
        List<Integer> list = new ArrayList<>();
        for (WorkOrderStatus s : ALLOWED.getOrDefault(this, Collections.emptySet())) {
            list.add(s.code);
        }
        return list;
    }

    /**
     * 是否为终态（不可再流转）
     */
    public boolean isFinal() {
        return ALLOWED.getOrDefault(this, Collections.emptySet()).isEmpty();
    }

    /**
     * 是否为进行中（未完成、未取消），用于负载统计
     */
    public boolean isActive() {
        return this != CANCELLED && this != COMPLETED;
    }

    public static WorkOrderStatus of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("工单状态不能为空");
        }
        for (WorkOrderStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("非法的工单状态: " + code);
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (WorkOrderStatus s : values()) {
            if (s.code == code) {
                return s.desc;
            }
        }
        return "";
    }
}
