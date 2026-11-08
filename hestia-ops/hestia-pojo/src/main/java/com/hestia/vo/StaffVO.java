package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工列表项
 */
@Data
public class StaffVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 工号 */
    private String staffNo;

    /** 姓名 */
    private String name;

    /** 账号 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 角色 */
    private Integer role;

    /** 角色名称 */
    private String roleName;

    /** 技能标签 */
    private String skillTags;

    /** 在手上限 */
    private Integer workloadLimit;

    /** 状态 */
    private Integer status;

    /** 在手工单数 */
    private Long activeCount;

}
