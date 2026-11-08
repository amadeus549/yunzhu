package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台概览
 */
@Data
public class WorkspaceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 今日新增 */
    private Long todayNew;

    /** 待受理 */
    private Long pendingAccept;

    /** 待派单 */
    private Long pendingDispatch;

    /** 处理中 */
    private Long processing;

    /** 待验收 */
    private Long pendingAcceptance;

    /** 今日完成 */
    private Long todayCompleted;

    /** SLA超时 */
    private Long slaOverdue;

    /** 紧急未完成 */
    private Long urgentCount;

    /** 忙碌工程师 */
    private Long engineerBusy;

    /** 空闲工程师 */
    private Long engineerIdle;

    /** 平均响应分钟 */
    private Double avgResponseMinutes;

    /** 按时完成率 */
    private Double onTimeRate;

}
