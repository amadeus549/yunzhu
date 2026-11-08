package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房屋列表项
 */
@Data
public class PremiseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 住户ID */
    private Long residentId;

    /** 住户姓名 */
    private String residentName;

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

    /** 完整地址 */
    private String fullAddress;

}
