package com.hestia.dto;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 报表查询
 */
@Data
public class ReportQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 开始日期 */
    // GET 参数绑定必须显式声明格式：LocalDate 无注解时 Spring 按本地化格式
    // （zh-CN 为 2026/10/10 斜杠风格）解析，前端传 2026-10-10 会直接 400
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate begin;

    /** 结束日期 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate end;

}
