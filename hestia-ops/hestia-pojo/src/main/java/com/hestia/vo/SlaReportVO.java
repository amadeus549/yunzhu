package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * SLA达成报表
 */
@Data
public class SlaReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单总数 */
    private Long total;

    /** 响应达标 */
    private Long responseOnTime;

    /** 响应达标率 */
    private Double responseRate;

    /** 解决达标 */
    private Long resolveOnTime;

    /** 解决达标率 */
    private Double resolveRate;

    /** 平均响应时长 */
    private Double avgResponseMinutes;

    /** 平均解决时长 */
    private Double avgResolveMinutes;

}
