package com.hestia.service;

import com.hestia.dto.WorkOrderCancelDTO;
import com.hestia.dto.WorkOrderCreateDTO;
import com.hestia.dto.WorkOrderDispatchDTO;
import com.hestia.dto.WorkOrderPageQueryDTO;
import com.hestia.dto.WorkOrderRatingDTO;
import com.hestia.dto.WorkOrderRejectDTO;
import com.hestia.dto.WorkOrderSubmitDTO;
import com.hestia.result.PageResult;
import com.hestia.vo.WorkOrderDetailVO;
import com.hestia.vo.WorkOrderVO;

import java.util.List;

/**
 * 工单服务 —— 负责工单全生命周期与状态机流转
 */
public interface WorkOrderService {

    /** 管理端创建工单（管家代报/电话报修） */
    Long create(WorkOrderCreateDTO dto);

    /** 住户端提交报修 */
    Long submit(WorkOrderSubmitDTO dto, Long residentId);

    PageResult pageQuery(WorkOrderPageQueryDTO query);

    WorkOrderDetailVO detail(Long id);

    /** 住户端「我的工单」 */
    List<WorkOrderVO> listByResident(Long residentId, Integer status);

    /** 住户端「我的工单」分页 */
    PageResult pageQueryByResident(Long residentId, Integer page, Integer pageSize, Integer status);

    /** 受理：待受理 -> 待派单 */
    void accept(Long orderId, Long operatorId);

    /** 派单：待派单 -> 已派单 */
    void dispatch(WorkOrderDispatchDTO dto, Long operatorId);

    /** 智能派单：自动选人并派单 */
    void autoDispatch(Long orderId, Long operatorId);

    /** 接单：已派单 -> 已接单 */
    void take(Long orderId, Long staffId);

    /** 拒单：已派单 -> 待派单 */
    void rejectDispatch(Long orderId, String reason, Long staffId);

    /** 开始处理：已接单 -> 处理中 */
    void start(Long orderId, Long staffId);

    /** 提交完工：处理中 -> 待验收 */
    void finish(Long orderId, String remark, Long staffId);

    /** 验收通过：待验收 -> 已完成 */
    void confirm(Long orderId, WorkOrderRatingDTO dto, Long residentId);

    /** 验收驳回：待验收 -> 已驳回 */
    void rejectAcceptance(WorkOrderRejectDTO dto, Long residentId);

    /** 取消工单 */
    void cancel(WorkOrderCancelDTO dto, Long operatorId, Integer operatorType);

    /** 催单：发通知给当前处理人/派单池 */
    void urge(Long orderId, Long residentId);
}
