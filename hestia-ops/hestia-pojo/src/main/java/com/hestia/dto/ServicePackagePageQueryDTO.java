package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务包分页
 */
@Data
public class ServicePackagePageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 名称 */
    private String name;

    /** 类型 */
    private Long categoryId;

    /** 状态 */
    private Integer status;

}
