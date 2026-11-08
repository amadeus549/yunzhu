package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工新增/编辑
 */
@Data
public class StaffDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工号 */
    private String staffNo;

    /** 姓名 */
    private String name;

    /** 登录账号 */
    private String username;

    /** 密码（新增必填） */
    private String password;

    /** 手机号 */
    private String phone;

    /** 角色 */
    private Integer role;

    /** 技能标签 */
    private String skillTags;

    /** 在手上限 */
    private Integer workloadLimit;

    /** 状态 */
    private Integer status;

}
