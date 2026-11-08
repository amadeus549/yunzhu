package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备分页
 */
@Data
public class DevicePageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 编号 */
    private String deviceNo;

    /** 名称 */
    private String name;

    /** 类别 */
    private String category;

    /** 状态 */
    private Integer status;

    /** 是否待维保 0否 1是 */
    private Integer maintainDue;

}
