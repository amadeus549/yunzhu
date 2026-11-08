package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工登录返回
 */
@Data
public class StaffLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工ID */
    private Long id;

    /** 账号 */
    private String username;

    /** 姓名 */
    private String name;

    /** 角色 */
    private Integer role;

    /** 角色名称 */
    private String roleName;

    /** JWT */
    private String token;

}
