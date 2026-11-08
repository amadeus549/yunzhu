package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 设备列表项
 */
@Data
public class DeviceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 编号 */
    private String deviceNo;

    /** 名称 */
    private String name;

    /** 类别 */
    private String category;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 点位ID */
    private Long premiseId;

    /** 位置 */
    private String location;

    /** 安装日期 */
    private LocalDate installDate;

    /** 质保到期 */
    private LocalDate warrantyEnd;

    /** 维保周期 */
    private Integer maintainCycleDays;

    /** 上次维保 */
    private LocalDate lastMaintainDate;

    /** 下次维保 */
    private LocalDate nextMaintainDate;

    /** 状态 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 是否待维保 */
    private Integer maintainDue;

    /** 质保是否过期 */
    private Integer warrantyExpired;

}
