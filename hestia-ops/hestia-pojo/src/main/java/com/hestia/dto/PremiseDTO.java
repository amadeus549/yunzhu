package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房屋/点位
 */
@Data
public class PremiseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 住户 */
    private Long residentId;

    /** 小区 */
    private String community;

    /** 楼栋 */
    private String building;

    /** 单元 */
    private String unit;

    /** 房号 */
    private String roomNo;

    /** 类型 */
    private Integer premiseType;

    /** 面积 */
    private BigDecimal area;

    /** 是否默认 */
    private Integer isDefault;

}
