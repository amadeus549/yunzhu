package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 趋势点
 */
@Data
public class TrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期 */
    private LocalDate date;

    /** 新增 */
    private Long created;

    /** 完成 */
    private Long completed;

}
