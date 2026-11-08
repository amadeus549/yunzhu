package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工程师绩效
 */
@Data
public class StaffPerformanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long staffId;

    /** 姓名 */
    private String staffName;

    /** 接单总数 */
    private Long total;

    /** 完成数 */
    private Long completed;

    /** 平均解决时长 */
    private Double avgResolveMinutes;

    /** 按时率 */
    private Double onTimeRate;

    /** 平均评分 */
    private Double avgRating;

}
