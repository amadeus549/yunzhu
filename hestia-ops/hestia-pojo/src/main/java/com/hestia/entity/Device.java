package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备台账
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 设备编号 */
    private String deviceNo;

    /** 设备名称 */
    private String name;

    /** 设备类别 */
    private String category;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 安装点位 */
    private Long premiseId;

    /** 安装位置描述 */
    private String location;

    /** 安装日期 */
    private LocalDate installDate;

    /** 质保到期 */
    private LocalDate warrantyEnd;

    /** 维保周期（天） */
    private Integer maintainCycleDays;

    /** 上次维保日期 */
    private LocalDate lastMaintainDate;

    /** 下次维保日期 */
    private LocalDate nextMaintainDate;

    /** 状态：1正常 2维修中 3停用 4报废 */
    private Integer status;

    /** 二维码地址 */
    private String qrCode;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createUser;

    /** 更新人 */
    private Long updateUser;

}
