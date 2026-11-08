package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 派单候选人
 */
@Data
public class DispatchCandidateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工程师ID */
    private Long staffId;

    /** 姓名 */
    private String staffName;

    /** 电话 */
    private String phone;

    /** 技能 */
    private String skillTags;

    /** 在手工单 */
    private Long activeCount;

    /** 上限 */
    private Integer workloadLimit;

    /** 匹配分 */
    private Integer matchScore;

    /** 技能是否匹配 */
    private Integer matched;

    /** 评分说明 */
    private String reason;

}
