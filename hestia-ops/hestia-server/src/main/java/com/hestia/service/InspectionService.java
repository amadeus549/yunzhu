package com.hestia.service;

import com.hestia.dto.InspectionExecuteDTO;
import com.hestia.dto.InspectionPlanDTO;
import com.hestia.dto.InspectionPlanPageQueryDTO;
import com.hestia.dto.InspectionTaskPageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.vo.InspectionPlanVO;
import com.hestia.vo.InspectionTaskVO;

import java.util.List;

/**
 * 巡检服务
 */
public interface InspectionService {

    PageResult planPageQuery(InspectionPlanPageQueryDTO query);

    void savePlan(InspectionPlanDTO dto);

    void updatePlan(InspectionPlanDTO dto);

    void updatePlanStatus(Long id, Integer status);

    PageResult taskPageQuery(InspectionTaskPageQueryDTO query);

    /** 执行巡检任务；异常时可选择转工单 */
    Long execute(InspectionExecuteDTO dto, Long operatorId);

    /** 按计划生成巡检任务（幂等），返回生成数量 */
    int generateTasks(Long planId);

    /** 定时扫描所有到期计划并生成任务，返回生成数量 */
    int scanAndGenerate();

    /** 标记逾期任务，返回数量 */
    int markOverdue();

    List<InspectionTaskVO> listByExecutor(Long executorId, Integer status);
}
