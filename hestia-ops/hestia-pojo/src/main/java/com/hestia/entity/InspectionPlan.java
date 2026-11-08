package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检计划
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 计划名称 */
    private String name;

    /** 周期类型：1每日 2每周 3每月 4自定义 */
    private Integer cycleType;

    /** 周期值 */
    private String cycleValue;

    /** 设备类别范围 */
    private String deviceCategory;

    /** 默认执行人 */
    private Long executorId;

    /** 生效日期 */
    private LocalDate startDate;

    /** 提前多少天生成任务 */
    private Integer generateAheadDays;

    /** 上次生成任务日期 */
    private LocalDate lastGenerateDate;

    /** 下次生成日期 */
    private LocalDate nextGenerateDate;

    /** 状态：1启用 0停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createUser;

    /** 更新人 */
    private Long updateUser;

}
