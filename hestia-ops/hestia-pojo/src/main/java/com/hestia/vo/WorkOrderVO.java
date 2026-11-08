package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单列表项
 */
@Data
public class WorkOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 工单号 */
    private String orderNo;

    /** 类型ID */
    private Long categoryId;

    /** 类型名称 */
    private String categoryName;

    /** 房屋ID */
    private Long premiseId;

    /** 房屋地址 */
    private String premiseAddress;

    /** 住户ID */
    private Long residentId;

    /** 住户姓名 */
    private String residentName;

    /** 住户电话 */
    private String residentPhone;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 优先级 */
    private Integer priority;

    /** 优先级名称 */
    private String priorityName;

    /** 状态 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 来源 */
    private Integer source;

    /** 来源名称 */
    private String sourceName;

    /** 期望时间 */
    private LocalDateTime expectTime;

    /** 处理人ID */
    private Long assigneeId;

    /** 处理人姓名 */
    private String assigneeName;

    /** 响应截止 */
    private LocalDateTime responseDeadline;

    /** 解决截止 */
    private LocalDateTime resolveDeadline;

    /** 费用 */
    private BigDecimal totalAmount;

    /** 响应超时 */
    private Integer responseBreached;

    /** 解决超时 */
    private Integer resolveBreached;

    /** 是否返工 */
    private Integer isRework;

    /** 返工次数 */
    private Integer reworkCount;

    /** 评分 */
    private Integer rating;

    /** 评价 */
    private String comment;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** SLA状态：NORMAL/WARNING/OVERDUE */
    private String slaStatus;

    /** 剩余分钟（负数表示已超时） */
    private Long slaRemainMinutes;

    /** 允许的下一状态 */
    private List<Integer> allowedNext;

}
