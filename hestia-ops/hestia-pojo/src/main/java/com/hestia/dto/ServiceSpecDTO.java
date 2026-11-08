package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务规格
 */
@Data
public class ServiceSpecDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 规格名称 */
    private String name;

    /** 价格 */
    private BigDecimal price;

    /** 耗时 */
    private Integer durationMin;

    /** 排序 */
    private Integer sort;

}
