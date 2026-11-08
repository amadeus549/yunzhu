package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryDTO implements Serializable {

    //主键
    private Long id;

    //业务类型 1报修 2投诉建议（对应 wo_category.biz_type，数据库 NOT NULL）
    private Integer bizType;

    //图标（可选）
    private String icon;

    //分类名称
    private String name;

    //排序
    private Integer sort;

    //状态 0停用 1启用
    private Integer status;

}
