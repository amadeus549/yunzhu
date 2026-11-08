package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 住户
 */
@Data
public class ResidentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 状态 */
    private Integer status;

}
