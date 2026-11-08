package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 接收人ID */
    private Long receiverId;

    /** 接收人类型：1员工 2住户 */
    private Integer receiverType;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 业务类型：1派单 2超时预警 3验收 4系统 */
    private Integer bizType;

    /** 业务ID */
    private Long bizId;

    /** 是否已读 */
    private Integer isRead;

    /** 创建时间 */
    private LocalDateTime createTime;

}
