package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SLA时效规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 规则名称 */
    private String name;

    /** 适用优先级：1低 2普通 3高 4紧急 */
    private Integer priority;

    /** 响应时限（分钟） */
    private Integer responseMinutes;

    /** 解决时限（分钟） */
    private Integer resolveMinutes;

    /** 超时升级到角色 */
    private Integer escalateRole;

    /** 状态：1启用 0禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
