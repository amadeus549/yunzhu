package com.hestia.dto;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 巡检任务分页
 */
@Data
public class InspectionTaskPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 计划 */
    private Long planId;

    /** 执行人 */
    private Long executorId;

    /** 状态 */
    private Integer status;

    /** 结果 */
    private Integer result;

    /** 计划日期起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate beginDate;

    /** 计划日期止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

}
