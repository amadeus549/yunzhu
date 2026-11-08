package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房屋/点位
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Premise implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属住户 */
    private Long residentId;

    /** 小区名称 */
    private String community;

    /** 楼栋 */
    private String building;

    /** 单元 */
    private String unit;

    /** 房号 */
    private String roomNo;

    /** 类型：1住宅 2商铺 3公共区域 */
    private Integer premiseType;

    /** 建筑面积（㎡） */
    private BigDecimal area;

    /** 是否默认地址 */
    private Integer isDefault;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
