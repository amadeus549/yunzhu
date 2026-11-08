package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务包
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属工单类型 */
    private Long categoryId;

    /** 套餐编码 */
    private String code;

    /** 套餐名称 */
    private String name;

    /** 套餐说明 */
    private String description;

    /** 图片 */
    private String image;

    /** 套餐价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 有效期（天） */
    private Integer validDays;

    /** 状态：1上架 0下架 */
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
