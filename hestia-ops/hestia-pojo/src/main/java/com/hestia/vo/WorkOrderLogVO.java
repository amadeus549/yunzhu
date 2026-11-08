package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单日志项
 */
@Data
public class WorkOrderLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人 */
    private String operatorName;

    /** 操作人类型 */
    private Integer operatorType;

    /** 动作码 */
    private String action;

    /** 动作名称 */
    private String actionName;

    /** 原状态 */
    private Integer fromStatus;

    /** 新状态 */
    private Integer toStatus;

    /** 备注 */
    private String remark;

    /** 时间 */
    private LocalDateTime createTime;

}
