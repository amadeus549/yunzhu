package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工单号 */
    private String orderNo;

    /** 工单类型 */
    private Long categoryId;

    /** 房屋/点位 */
    private Long premiseId;

    /** 报修住户 */
    private Long residentId;

    /** 标题 */
    private String title;

    /** 问题描述 */
    private String description;

    /** 优先级：1低 2普通 3高 4紧急 */
    private Integer priority;

    /** 状态：0已取消 1待受理 2待派单 3已派单 4已接单 5处理中 6待验收 7已完成 8已驳回 */
    private Integer status;

    /** 来源：1住户自助 2管家代报 3巡检发现 4电话 */
    private Integer source;

    /** SLA规则 */
    private Long slaRuleId;

    /** 响应截止时间 */
    private LocalDateTime responseDeadline;

    /** 解决截止时间 */
    private LocalDateTime resolveDeadline;

    /** 期望上门时间 */
    private LocalDateTime expectTime;

    /** 当前处理工程师 */
    private Long assigneeId;

    /** 派单人 */
    private Long dispatcherId;

    /** 接单时间 */
    private LocalDateTime acceptTime;

    /** 开始处理时间 */
    private LocalDateTime startTime;

    /** 完工时间 */
    private LocalDateTime finishTime;

    /** 验收完成时间 */
    private LocalDateTime completeTime;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 费用合计 */
    private BigDecimal totalAmount;

    /** 响应是否超时 */
    private Integer responseBreached;

    /** 解决是否超时 */
    private Integer resolveBreached;

    /** 是否返工 */
    private Integer isRework;

    /** 返工次数 */
    private Integer reworkCount;

    /** 满意度评分 */
    private Integer rating;

    /** 评价内容 */
    private String comment;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createUser;

    /** 更新人 */
    private Long updateUser;

}
