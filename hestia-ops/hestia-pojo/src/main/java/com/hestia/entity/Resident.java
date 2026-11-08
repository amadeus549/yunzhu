package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住户
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resident implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 微信openid */
    private String openid;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 状态：1正常 0禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
