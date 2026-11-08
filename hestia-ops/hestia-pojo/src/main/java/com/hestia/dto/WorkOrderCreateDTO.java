package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端创建工单
 */
@Data
public class WorkOrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单类型 */
    private Long categoryId;

    /** 房屋/点位 */
    private Long premiseId;

    /** 住户 */
    private Long residentId;

    /** 标题 */
    private String title;

    /** 问题描述 */
    private String description;

    /** 优先级 */
    private Integer priority;

    /** 来源 */
    private Integer source;

    /** 期望上门时间 */
    private LocalDateTime expectTime;

    /** 服务项明细 */
    private List<WorkOrderItemDTO> items;

}
