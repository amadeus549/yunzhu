package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 巡检计划列表项
 */
@Data
public class InspectionPlanVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 周期类型 */
    private Integer cycleType;

    /** 周期值 */
    private String cycleValue;

    /** 周期描述 */
    private String cycleDesc;

    /** 设备范围 */
    private String deviceCategory;

    /** 执行人ID */
    private Long executorId;

    /** 执行人 */
    private String executorName;

    /** 生效日期 */
    private LocalDate startDate;

    /** 下次生成 */
    private LocalDate nextGenerateDate;

    /** 状态 */
    private Integer status;

}
