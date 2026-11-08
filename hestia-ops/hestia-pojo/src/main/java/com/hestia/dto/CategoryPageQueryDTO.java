package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryPageQueryDTO implements Serializable {

    //页码
    private int page;

    //每页记录数
    private int pageSize;

    //分类名称
    private String name;

    //业务类型 1报修 2投诉建议（对应 wo_category.biz_type）
    //注意：XML 的 <if test="bizType != null"> 要求本类必须有该属性的 getter，
    //否则 MyBatis OGNL 求值抛「There is no getter for property」→ 接口 500
    private Integer bizType;

    //状态 0停用 1启用（XML 动态条件同样引用此属性）
    private Integer status;

}
