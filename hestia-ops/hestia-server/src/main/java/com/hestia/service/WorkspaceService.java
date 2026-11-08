package com.hestia.service;

import com.hestia.vo.StatusDistributionVO;
import com.hestia.vo.TrendVO;
import com.hestia.vo.WorkspaceVO;

import java.util.List;

/**
 * 工作台服务
 */
public interface WorkspaceService {

    WorkspaceVO overview();

    List<StatusDistributionVO> statusDistribution();

    List<TrendVO> recentTrend(int days);
}
