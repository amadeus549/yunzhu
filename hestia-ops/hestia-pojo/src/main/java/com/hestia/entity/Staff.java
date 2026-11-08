package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工号 */
    private String staffNo;

    /** 姓名 */
    private String name;

    /** 登录账号 */
    private String username;

    /** 密码（BCrypt） */
    private String password;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 角色：1管家 2工程师 3主管 4客服 */
    private Integer role;

    /** 技能标签，逗号分隔 */
    private String skillTags;

    /** 同时在手工单上限 */
    private Integer workloadLimit;

    /** 状态：1启用 0禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createUser;

    /** 更新人 */
    private Long updateUser;

}
