package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 巡检计划分页
 */
@Data
public class InspectionPlanPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 名称 */
    private String name;

    /** 状态 */
    private Integer status;

}
