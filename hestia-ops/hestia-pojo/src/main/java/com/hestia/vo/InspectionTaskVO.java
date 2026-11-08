package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检任务列表项
 */
@Data
public class InspectionTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 任务号 */
    private String taskNo;

    /** 计划ID */
    private Long planId;

    /** 计划名称 */
    private String planName;

    /** 设备ID */
    private Long deviceId;

    /** 设备名称 */
    private String deviceName;

    /** 执行人ID */
    private Long executorId;

    /** 执行人 */
    private String executorName;

    /** 计划日期 */
    private LocalDate planDate;

    /** 执行时间 */
    private LocalDateTime actualTime;

    /** 结果 */
    private Integer result;

    /** 结果名称 */
    private String resultName;

    /** 异常说明 */
    private String abnormalDesc;

    /** 转工单ID */
    private Long orderId;

    /** 状态 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

}
