package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单流转日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工单ID */
    private Long orderId;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人类型：1员工 2住户 3系统 */
    private Integer operatorType;

    /** 动作 */
    private String action;

    /** 变更前状态 */
    private Integer fromStatus;

    /** 变更后状态 */
    private Integer toStatus;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

}
