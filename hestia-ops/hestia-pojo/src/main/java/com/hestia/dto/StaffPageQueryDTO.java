package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工分页查询
 */
@Data
public class StaffPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 姓名模糊 */
    private String name;

    /** 角色 */
    private Integer role;

    /** 状态 */
    private Integer status;

}
