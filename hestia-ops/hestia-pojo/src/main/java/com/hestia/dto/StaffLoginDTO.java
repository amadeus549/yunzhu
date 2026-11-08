package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工登录
 */
@Data
public class StaffLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 账号 */
    private String username;

    /** 密码 */
    private String password;

}
