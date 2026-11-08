package com.hestia.vo;

import lombok.Data;
import com.hestia.dto.ServiceSpecDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务项列表项
 */
@Data
public class ServiceItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 类型ID */
    private Long categoryId;

    /** 类型名称 */
    private String categoryName;

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

    /** 规格 */
    private List<ServiceSpecDTO> specs;

}
