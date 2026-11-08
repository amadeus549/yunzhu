package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单附件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工单ID */
    private Long orderId;

    /** 类型：1图片 2视频 3文件 */
    private Integer fileType;

    /** 文件地址 */
    private String url;

    /** 上传人 */
    private Long uploaderId;

    /** 上传人类型：1员工 2住户 */
    private Integer uploaderType;

    /** 创建时间 */
    private LocalDateTime createTime;

}
