package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属工单类型 */
    private Long categoryId;

    /** 服务编码 */
    private String code;

    /** 服务名称 */
    private String name;

    /** 服务说明 */
    private String description;

    /** 图片 */
    private String image;

    /** 基础价格 */
    private BigDecimal price;

    /** 计价单位 */
    private String unit;

    /** 预计耗时（分钟） */
    private Integer durationMin;

    /** 所需技能标签 */
    private String requiredSkill;

    /** 状态：1上架 0下架 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createUser;

    /** 更新人 */
    private Long updateUser;

}
