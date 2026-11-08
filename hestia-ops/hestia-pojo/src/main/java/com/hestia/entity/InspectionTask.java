package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 任务号 */
    private String taskNo;

    /** 所属计划 */
    private Long planId;

    /** 巡检设备 */
    private Long deviceId;

    /** 执行人 */
    private Long executorId;

    /** 计划日期 */
    private LocalDate planDate;

    /** 实际执行时间 */
    private LocalDateTime actualTime;

    /** 结果：1正常 2异常 3未执行 */
    private Integer result;

    /** 异常说明 */
    private String abnormalDesc;

    /** 异常转工单ID */
    private Long orderId;

    /** 状态：1待执行 2已完成 3已逾期 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
