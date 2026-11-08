package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 巡检计划
 */
@Data
public class InspectionPlanDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 名称 */
    private String name;

    /** 周期类型 */
    private Integer cycleType;

    /** 周期值 */
    private String cycleValue;

    /** 设备类别范围 */
    private String deviceCategory;

    /** 执行人 */
    private Long executorId;

    /** 生效日期 */
    private LocalDate startDate;

    /** 提前生成天数 */
    private Integer generateAheadDays;

    /** 状态 */
    private Integer status;

}
