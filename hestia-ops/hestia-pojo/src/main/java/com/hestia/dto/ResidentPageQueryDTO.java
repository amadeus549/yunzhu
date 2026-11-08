package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 住户分页
 */
@Data
public class ResidentPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

}
