package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务包
 */
@Data
public class ServicePackageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 类型 */
    private Long categoryId;

    /** 编码 */
    private String code;

    /** 名称 */
    private String name;

    /** 说明 */
    private String description;

    /** 套餐价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 有效期 */
    private Integer validDays;

    /** 状态 */
    private Integer status;

    /** 包含服务项 */
    private List<ServicePackageItemDTO> items;

}
