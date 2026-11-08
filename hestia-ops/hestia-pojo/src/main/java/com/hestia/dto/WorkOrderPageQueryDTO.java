package com.hestia.dto;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单分页查询
 */
@Data
public class WorkOrderPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 工单号 */
    private String orderNo;

    /** 标题模糊 */
    private String title;

    /** 工单类型 */
    private Long categoryId;

    /** 状态 */
    private Integer status;

    /** 优先级 */
    private Integer priority;

    /** 处理人 */
    private Long assigneeId;

    /** 住户 */
    private Long residentId;

    /** 房屋 */
    private Long premiseId;

    /** 创建时间起（格式 yyyy-MM-dd HH:mm:ss） */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /** 创建时间止（格式 yyyy-MM-dd HH:mm:ss） */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 是否超时 0否 1是 */
    private Integer slaBreached;

}
