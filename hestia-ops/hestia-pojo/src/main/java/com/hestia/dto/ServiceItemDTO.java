package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务项
 */
@Data
public class ServiceItemDTO implements Serializable {

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

    /** 价格 */
    private BigDecimal price;

    /** 单位 */
    private String unit;

    /** 耗时 */
    private Integer durationMin;

    /** 所需技能 */
    private String requiredSkill;

    /** 状态 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 规格列表 */
    private List<ServiceSpecDTO> specs;

}
