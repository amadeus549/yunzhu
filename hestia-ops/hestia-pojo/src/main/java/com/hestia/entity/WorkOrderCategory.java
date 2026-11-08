package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 类型名称 */
    private String name;

    /** 业务类型：1报修 2投诉 3咨询 4巡检异常 */
    private Integer bizType;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

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
