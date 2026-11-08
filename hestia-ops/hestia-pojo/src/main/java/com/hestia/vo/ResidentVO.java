package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 住户列表项
 */
@Data
public class ResidentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 状态 */
    private Integer status;

    /** 名下房屋数 */
    private Long premiseCount;

}
