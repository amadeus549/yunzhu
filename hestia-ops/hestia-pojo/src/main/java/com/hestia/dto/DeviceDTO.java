package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 设备
 */
@Data
public class DeviceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 设备编号 */
    private String deviceNo;

    /** 名称 */
    private String name;

    /** 类别 */
    private String category;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 点位 */
    private Long premiseId;

    /** 位置 */
    private String location;

    /** 安装日期 */
    private LocalDate installDate;

    /** 质保到期 */
    private LocalDate warrantyEnd;

    /** 维保周期 */
    private Integer maintainCycleDays;

    /** 状态 */
    private Integer status;

    /** 备注 */
    private String remark;

}
