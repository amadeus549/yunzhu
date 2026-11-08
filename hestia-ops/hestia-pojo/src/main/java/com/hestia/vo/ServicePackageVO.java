package com.hestia.vo;

import lombok.Data;
import com.hestia.dto.ServicePackageItemDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务包列表项
 */
@Data
public class ServicePackageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 类型ID */
    private Long categoryId;

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

    /** 包含项 */
    private List<ServicePackageItemDTO> items;

}
