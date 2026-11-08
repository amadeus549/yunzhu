package com.hestia.service;

import com.hestia.vo.DispatchCandidateVO;

import java.util.List;

/**
 * 智能派单服务
 */
public interface DispatchService {

    /**
     * 计算派单候选人并按匹配分倒序返回。
     * 评分模型：技能匹配(60) + 负载余量(30) + 近期派单间隔(10)
     */
    List<DispatchCandidateVO> candidates(Long orderId);

    /** 取匹配分最高的候选人，无可用候选人返回 null */
    DispatchCandidateVO pickBest(Long orderId);
}
