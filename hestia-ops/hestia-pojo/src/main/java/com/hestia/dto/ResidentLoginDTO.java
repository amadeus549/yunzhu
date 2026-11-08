package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 住户登录
 */
@Data
public class ResidentLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 手机号 */
    private String phone;

    /** 验证码 */
    private String code;

}
