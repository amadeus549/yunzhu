package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 派单记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDispatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工单ID */
    private Long orderId;

    /** 被派工程师 */
    private Long staffId;

    /** 派单方式：1手动 2智能 */
    private Integer dispatchType;

    /** 智能匹配得分 */
    private Integer matchScore;

    /** 派单/拒单原因 */
    private String reason;

    /** 状态：1待接单 2已接单 3已拒单 4已取消 */
    private Integer status;

    /** 派单时间 */
    private LocalDateTime dispatchTime;

    /** 接单时间 */
    private LocalDateTime acceptTime;

    /** 拒单时间 */
    private LocalDateTime rejectTime;

    /** 创建时间 */
    private LocalDateTime createTime;

}
