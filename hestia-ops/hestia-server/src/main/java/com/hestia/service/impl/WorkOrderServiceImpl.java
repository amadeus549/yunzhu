package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.context.BaseContext;
import com.hestia.dto.WorkOrderCancelDTO;
import com.hestia.dto.WorkOrderCreateDTO;
import com.hestia.dto.WorkOrderDispatchDTO;
import com.hestia.dto.WorkOrderItemDTO;
import com.hestia.dto.WorkOrderPageQueryDTO;
import com.hestia.dto.WorkOrderRatingDTO;
import com.hestia.dto.WorkOrderRejectDTO;
import com.hestia.dto.WorkOrderSubmitDTO;
import com.hestia.entity.Premise;
import com.hestia.entity.ServiceItem;
import com.hestia.entity.ServiceSpec;
import com.hestia.entity.Staff;
import com.hestia.entity.WorkOrder;
import com.hestia.entity.WorkOrderCategory;
import com.hestia.entity.WorkOrderDispatch;
import com.hestia.entity.WorkOrderItem;
import com.hestia.entity.WorkOrderLog;
import com.hestia.enumeration.DispatchStatus;
import com.hestia.enumeration.DispatchType;
import com.hestia.enumeration.NoticeBizType;
import com.hestia.enumeration.OperatorType;
import com.hestia.enumeration.WorkOrderPriority;
import com.hestia.enumeration.WorkOrderSource;
import com.hestia.enumeration.WorkOrderStatus;
import com.hestia.exception.WorkOrderBusinessException;
import com.hestia.mapper.PremiseMapper;
import com.hestia.mapper.ServiceItemMapper;
import com.hestia.mapper.ServiceSpecMapper;
import com.hestia.mapper.StaffMapper;
import com.hestia.mapper.WorkOrderAttachmentMapper;
import com.hestia.mapper.WorkOrderCategoryMapper;
import com.hestia.mapper.WorkOrderDispatchMapper;
import com.hestia.mapper.WorkOrderItemMapper;
import com.hestia.mapper.WorkOrderLogMapper;
import com.hestia.mapper.WorkOrderMapper;
import com.hestia.result.PageResult;
import com.hestia.service.DispatchService;
import com.hestia.service.NoticeService;
import com.hestia.service.SlaService;
import com.hestia.service.WorkOrderService;
import com.hestia.vo.DispatchCandidateVO;
import com.hestia.vo.WorkOrderDetailVO;
import com.hestia.vo.WorkOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 工单服务实现。
 *
 * <p>所有状态变更统一收敛到 {@link #changeStatus}，由 {@link WorkOrderStatus#canTransitTo}
 * 做合法性校验，杜绝「待受理直接跳已完成」这类脏数据。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final WorkOrderMapper orderMapper;
    private final WorkOrderItemMapper orderItemMapper;
    private final WorkOrderLogMapper logMapper;
    private final WorkOrderDispatchMapper dispatchMapper;
    private final WorkOrderAttachmentMapper attachmentMapper;
    private final WorkOrderCategoryMapper categoryMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final ServiceSpecMapper serviceSpecMapper;
    private final PremiseMapper premiseMapper;
    private final StaffMapper staffMapper;
    private final SlaService slaService;
    private final DispatchService dispatchService;
    private final NoticeService noticeService;
    private final StringRedisTemplate redisTemplate;

    // ==================== 创建 ====================

    @Override
    @Transactional
    public Long create(WorkOrderCreateDTO dto) {
        checkCategory(dto.getCategoryId());
        WorkOrder order = new WorkOrder();
        order.setOrderNo(nextOrderNo());
        order.setCategoryId(dto.getCategoryId());
        order.setPremiseId(dto.getPremiseId());
        order.setResidentId(dto.getResidentId());
        order.setTitle(dto.getTitle());
        order.setDescription(dto.getDescription());
        order.setPriority(dto.getPriority() == null ? WorkOrderPriority.NORMAL.getCode() : dto.getPriority());
        order.setSource(dto.getSource() == null ? WorkOrderSource.HOUSEKEEPER.getCode() : dto.getSource());
        order.setStatus(WorkOrderStatus.PENDING.getCode());
        order.setExpectTime(dto.getExpectTime());
        order.setResponseBreached(0);
        order.setResolveBreached(0);
        order.setIsRework(0);
        order.setReworkCount(0);
        order.setTotalAmount(BigDecimal.ZERO);

        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setCreateUser(BaseContext.getCurrentId());
        slaService.applySla(order, now);

        List<WorkOrderItem> items = buildItems(dto.getItems());
        order.setTotalAmount(sumAmount(items));

        orderMapper.insert(order);
        if (!items.isEmpty()) {
            items.forEach(i -> i.setOrderId(order.getId()));
            orderItemMapper.insertBatch(items);
        }
        writeLog(order.getId(), null, WorkOrderStatus.PENDING, "CREATE", "创建工单", OperatorType.STAFF,
                BaseContext.getCurrentId());
        log.info("工单创建成功 {} 优先级={} SLA解决时限={}",
                order.getOrderNo(), order.getPriority(), order.getResolveDeadline());
        return order.getId();
    }

    @Override
    @Transactional
    public Long submit(WorkOrderSubmitDTO dto, Long residentId) {
        checkCategory(dto.getCategoryId());
        WorkOrder order = new WorkOrder();
        order.setOrderNo(nextOrderNo());
        order.setCategoryId(dto.getCategoryId());
        order.setPremiseId(dto.getPremiseId());
        order.setResidentId(residentId);
        order.setTitle(dto.getTitle());
        order.setDescription(dto.getDescription());
        order.setPriority(dto.getPriority() == null ? WorkOrderPriority.NORMAL.getCode() : dto.getPriority());
        order.setSource(WorkOrderSource.RESIDENT.getCode());
        order.setStatus(WorkOrderStatus.PENDING.getCode());
        order.setExpectTime(dto.getExpectTime());
        order.setResponseBreached(0);
        order.setResolveBreached(0);
        order.setIsRework(0);
        order.setReworkCount(0);
        order.setTotalAmount(BigDecimal.ZERO);

        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setCreateUser(residentId);
        slaService.applySla(order, now);

        List<WorkOrderItem> items = buildItems(dto.getItems());
        order.setTotalAmount(sumAmount(items));

        orderMapper.insert(order);
        if (!items.isEmpty()) {
            items.forEach(i -> i.setOrderId(order.getId()));
            orderItemMapper.insertBatch(items);
        }
        writeLog(order.getId(), null, WorkOrderStatus.PENDING, "CREATE", "住户提交报修", OperatorType.RESIDENT,
                residentId);
        // 通知管家受理
        noticeService.push(1L, 1, NoticeBizType.DISPATCH.getCode(), order.getId(), "新报修工单",
                "住户提交工单 " + order.getOrderNo() + "：" + order.getTitle());
        return order.getId();
    }

    // ==================== 查询 ====================

    @Override
    public PageResult pageQuery(WorkOrderPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<WorkOrderVO> page = (Page<WorkOrderVO>) orderMapper.pageQuery(query);
        page.getResult().forEach(this::enrich);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult pageQueryByResident(Long residentId, Integer page, Integer pageSize, Integer status) {
        WorkOrderPageQueryDTO q = new WorkOrderPageQueryDTO();
        q.setPage(page == null ? 1 : page);
        q.setPageSize(pageSize == null ? 10 : pageSize);
        q.setResidentId(residentId);
        q.setStatus(status);
        return pageQuery(q);
    }

    @Override
    public List<WorkOrderVO> listByResident(Long residentId, Integer status) {
        WorkOrderPageQueryDTO q = new WorkOrderPageQueryDTO();
        q.setPage(1);
        q.setPageSize(50);
        q.setResidentId(residentId);
        q.setStatus(status);
        PageHelper.startPage(1, 50);
        List<WorkOrderVO> list = orderMapper.pageQuery(q);
        list.forEach(this::enrich);
        return list;
    }

    @Override
    public WorkOrderDetailVO detail(Long id) {
        WorkOrderVO vo = orderMapper.selectVOById(id);
        if (vo == null) {
            throw new WorkOrderBusinessException("工单不存在");
        }
        enrich(vo);
        WorkOrderDetailVO detail = new WorkOrderDetailVO();
        detail.setOrder(vo);
        detail.setItems(orderItemMapper.selectVOByOrderId(id));
        detail.setLogs(logMapper.selectVOByOrderId(id));
        detail.getLogs().forEach(l -> l.setActionName(actionName(l.getAction())));
        detail.setAttachments(attachmentMapper.selectUrlsByOrderId(id));
        return detail;
    }

    // ==================== 状态流转 ====================

    @Override
    @Transactional
    public void accept(Long orderId, Long operatorId) {
        WorkOrder order = getOrThrow(orderId);
        order.setDispatcherId(operatorId);
        changeStatus(order, WorkOrderStatus.PENDING_DISPATCH, "ACCEPT", "管家受理，转待派单",
                OperatorType.STAFF, operatorId);
    }

    @Override
    @Transactional
    public void dispatch(WorkOrderDispatchDTO dto, Long operatorId) {
        WorkOrder order = getOrThrow(dto.getOrderId());
        Staff staff = staffMapper.selectById(dto.getStaffId());
        if (staff == null) {
            throw new WorkOrderBusinessException("工程师不存在");
        }
        if (staff.getStatus() != null && staff.getStatus() == 0) {
            throw new WorkOrderBusinessException("该工程师已被禁用");
        }
        // 派单记录
        WorkOrderDispatch record = new WorkOrderDispatch();
        record.setOrderId(order.getId());
        record.setStaffId(staff.getId());
        record.setDispatchType(DispatchType.MANUAL.getCode());
        record.setReason(dto.getRemark() == null ? "管家手动指派" : dto.getRemark());
        record.setStatus(DispatchStatus.PENDING.getCode());
        record.setDispatchTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        dispatchMapper.insert(record);

        order.setAssigneeId(staff.getId());
        order.setDispatcherId(operatorId);
        changeStatus(order, WorkOrderStatus.DISPATCHED, "DISPATCH",
                "派单给 " + staff.getName() + (dto.getRemark() == null ? "" : "：" + dto.getRemark()),
                OperatorType.STAFF, operatorId);

        noticeService.push(staff.getId(), 1, NoticeBizType.DISPATCH.getCode(), order.getId(),
                "新工单派单", "工单 " + order.getOrderNo() + "：" + order.getTitle());
    }

    @Override
    @Transactional
    public void autoDispatch(Long orderId, Long operatorId) {
        WorkOrder order = getOrThrow(orderId);
        DispatchCandidateVO best = dispatchService.pickBest(orderId);
        if (best == null) {
            throw new WorkOrderBusinessException("暂无可用工程师，请手动指派");
        }
        WorkOrderDispatch record = new WorkOrderDispatch();
        record.setOrderId(order.getId());
        record.setStaffId(best.getStaffId());
        record.setDispatchType(DispatchType.AUTO.getCode());
        record.setMatchScore(best.getMatchScore());
        record.setReason(best.getReason());
        record.setStatus(DispatchStatus.PENDING.getCode());
        record.setDispatchTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        dispatchMapper.insert(record);

        order.setAssigneeId(best.getStaffId());
        order.setDispatcherId(operatorId);
        changeStatus(order, WorkOrderStatus.DISPATCHED, "AUTO_DISPATCH",
                String.format("智能派单给 %s（匹配分 %d）", best.getStaffName(), best.getMatchScore()),
                OperatorType.STAFF, operatorId);

        noticeService.push(best.getStaffId(), 1, NoticeBizType.DISPATCH.getCode(), order.getId(),
                "新工单派单（智能）", "工单 " + order.getOrderNo() + "：" + order.getTitle());
        log.info("工单 {} 智能派单 -> {} 分数 {}", order.getOrderNo(), best.getStaffName(), best.getMatchScore());
    }

    @Override
    @Transactional
    public void take(Long orderId, Long staffId) {
        WorkOrder order = getOrThrow(orderId);
        // staffId 为空表示管理端代操作，直接取工单当前处理人
        Long effective = staffId != null ? staffId : order.getAssigneeId();
        if (order.getAssigneeId() == null || !order.getAssigneeId().equals(effective)) {
            throw new WorkOrderBusinessException("该工单未派给您，无法接单");
        }
        LocalDateTime now = LocalDateTime.now();
        order.setAcceptTime(now);
        // 响应是否超时：接单时间晚于响应截止时间即视为响应超时
        if (order.getResponseDeadline() != null && now.isAfter(order.getResponseDeadline())) {
            order.setResponseBreached(1);
        }
        dispatchMapper.accept(orderId, now);
        changeStatus(order, WorkOrderStatus.ACCEPTED, "TAKE", "工程师接单", OperatorType.STAFF, effective);
    }

    @Override
    @Transactional
    public void rejectDispatch(Long orderId, String reason, Long staffId) {
        WorkOrder order = getOrThrow(orderId);
        Long effective = staffId != null ? staffId : order.getAssigneeId();
        if (order.getAssigneeId() == null || !order.getAssigneeId().equals(effective)) {
            throw new WorkOrderBusinessException("该工单未派给您，无法拒单");
        }
        WorkOrderStatus from = WorkOrderStatus.of(order.getStatus());
        if (!from.canTransitTo(WorkOrderStatus.PENDING_DISPATCH)) {
            throw new WorkOrderBusinessException("当前状态不允许拒单");
        }
        dispatchMapper.reject(orderId, LocalDateTime.now(), reason);
        orderMapper.clearAssignee(orderId, WorkOrderStatus.PENDING_DISPATCH.getCode(), LocalDateTime.now());
        writeLog(orderId, from, WorkOrderStatus.PENDING_DISPATCH, "REJECT_DISPATCH",
                reason == null ? "工程师拒单" : "工程师拒单：" + reason, OperatorType.STAFF, effective);

        noticeService.push(order.getDispatcherId() == null ? 1L : order.getDispatcherId(), 1,
                NoticeBizType.DISPATCH.getCode(), orderId, "工单被拒单",
                "工单 " + order.getOrderNo() + " 已被拒单，请重新派单");
    }

    @Override
    @Transactional
    public void start(Long orderId, Long staffId) {
        WorkOrder order = getOrThrow(orderId);
        Long effective = staffId != null ? staffId : order.getAssigneeId();
        order.setStartTime(LocalDateTime.now());
        changeStatus(order, WorkOrderStatus.PROCESSING, "START", "工程师上门处理", OperatorType.STAFF, effective);
    }

    @Override
    @Transactional
    public void finish(Long orderId, String remark, Long staffId) {
        WorkOrder order = getOrThrow(orderId);
        LocalDateTime now = LocalDateTime.now();
        order.setFinishTime(now);
        if (order.getResolveDeadline() != null && now.isAfter(order.getResolveDeadline())) {
            order.setResolveBreached(1);
        }
        Long effective = staffId != null ? staffId : order.getAssigneeId();
        changeStatus(order, WorkOrderStatus.PENDING_ACCEPT, "FINISH",
                remark == null ? "提交完工，等待住户验收" : remark, OperatorType.STAFF, effective);

        if (order.getResidentId() != null) {
            noticeService.push(order.getResidentId(), 2, NoticeBizType.ACCEPTANCE.getCode(), orderId,
                    "完工待验收", "工单 " + order.getOrderNo() + " 已完工，请及时验收");
        }
    }

    @Override
    @Transactional
    public void confirm(Long orderId, WorkOrderRatingDTO dto, Long residentId) {
        WorkOrder order = getOrThrow(orderId);
        Long effective = residentId != null ? residentId : order.getResidentId();
        order.setCompleteTime(LocalDateTime.now());
        order.setRating(dto.getRating());
        order.setComment(dto.getComment());
        changeStatus(order, WorkOrderStatus.COMPLETED, "CONFIRM", "住户验收通过", OperatorType.RESIDENT, effective);
    }

    @Override
    @Transactional
    public void rejectAcceptance(WorkOrderRejectDTO dto, Long residentId) {
        WorkOrder order = getOrThrow(dto.getOrderId());
        Long effective = residentId != null ? residentId : order.getResidentId();
        order.setIsRework(1);
        order.setReworkCount((order.getReworkCount() == null ? 0 : order.getReworkCount()) + 1);
        changeStatus(order, WorkOrderStatus.REJECTED, "REJECT",
                dto.getReason() == null ? "住户验收不通过，返工" : "验收不通过：" + dto.getReason(),
                OperatorType.RESIDENT, effective);

        if (order.getAssigneeId() != null) {
            noticeService.push(order.getAssigneeId(), 1, NoticeBizType.ACCEPTANCE.getCode(), order.getId(),
                    "验收驳回返工", "工单 " + order.getOrderNo() + " 被驳回，请返工");
        }
    }

    @Override
    @Transactional
    public void cancel(WorkOrderCancelDTO dto, Long operatorId, Integer operatorType) {
        WorkOrder order = getOrThrow(dto.getOrderId());
        order.setCancelTime(LocalDateTime.now());
        OperatorType type = operatorType == null ? OperatorType.STAFF : OperatorType.of(operatorType);
        changeStatus(order, WorkOrderStatus.CANCELLED, "CANCEL",
                dto.getReason() == null ? "取消工单" : dto.getReason(), type, operatorId);
    }

    @Override
    public void urge(Long orderId, Long residentId) {
        WorkOrder order = getOrThrow(orderId);
        String content = "住户催单：" + order.getOrderNo() + " " + order.getTitle();
        if (order.getAssigneeId() != null) {
            noticeService.push(order.getAssigneeId(), 1, NoticeBizType.DISPATCH.getCode(), orderId, "住户催单", content);
        }
        noticeService.push(1L, 1, NoticeBizType.DISPATCH.getCode(), orderId, "住户催单", content);
    }

    // ==================== 私有方法 ====================

    private WorkOrder getOrThrow(Long orderId) {
        WorkOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new WorkOrderBusinessException("工单不存在");
        }
        return order;
    }

    private void checkCategory(Long categoryId) {
        WorkOrderCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new WorkOrderBusinessException("工单类型不存在");
        }
        if (category.getStatus() != null && category.getStatus() == 0) {
            throw new WorkOrderBusinessException("该工单类型已停用");
        }
    }

    /**
     * 统一状态变更入口：校验 -> 更新 -> 写日志
     */
    private void changeStatus(WorkOrder order, WorkOrderStatus target, String action, String remark,
                              OperatorType operatorType, Long operatorId) {
        WorkOrderStatus from = WorkOrderStatus.of(order.getStatus());
        if (!from.canTransitTo(target)) {
            throw new WorkOrderBusinessException(
                    String.format("工单当前状态「%s」不允许变更为「%s」", from.getDesc(), target.getDesc()));
        }
        order.setStatus(target.getCode());
        order.setUpdateTime(LocalDateTime.now());
        order.setUpdateUser(operatorId);
        orderMapper.update(order);
        writeLog(order.getId(), from, target, action, remark, operatorType, operatorId);
        log.info("工单 {} {} -> {} by {}", order.getOrderNo(), from.getDesc(), target.getDesc(), action);
    }

    private void writeLog(Long orderId, WorkOrderStatus from, WorkOrderStatus to, String action,
                          String remark, OperatorType operatorType, Long operatorId) {
        WorkOrderLog logRecord = new WorkOrderLog();
        logRecord.setOrderId(orderId);
        logRecord.setOperatorId(operatorId);
        logRecord.setOperatorType(operatorType.getCode());
        logRecord.setAction(action);
        logRecord.setFromStatus(from == null ? null : from.getCode());
        logRecord.setToStatus(to == null ? null : to.getCode());
        logRecord.setRemark(remark);
        logRecord.setCreateTime(LocalDateTime.now());
        logMapper.insert(logRecord);
    }

    private void writeLog(Long orderId, WorkOrderStatus from, WorkOrderStatus to, String action,
                          String remark, Integer operatorType, Long operatorId) {
        WorkOrderLog logRecord = new WorkOrderLog();
        logRecord.setOrderId(orderId);
        logRecord.setOperatorId(operatorId);
        logRecord.setOperatorType(operatorType);
        logRecord.setAction(action);
        logRecord.setFromStatus(from == null ? null : from.getCode());
        logRecord.setToStatus(to == null ? null : to.getCode());
        logRecord.setRemark(remark);
        logRecord.setCreateTime(LocalDateTime.now());
        logMapper.insert(logRecord);
    }

    private List<WorkOrderItem> buildItems(List<WorkOrderItemDTO> dtos) {
        List<WorkOrderItem> items = new ArrayList<>();
        if (dtos == null || dtos.isEmpty()) {
            return items;
        }
        for (WorkOrderItemDTO d : dtos) {
            ServiceItem si = serviceItemMapper.selectById(d.getItemId());
            if (si == null) {
                throw new WorkOrderBusinessException("服务项不存在：" + d.getItemId());
            }
            BigDecimal price = si.getPrice();
            String specName = null;
            if (d.getSpecId() != null) {
                ServiceSpec spec = serviceSpecMapper.selectById(d.getSpecId());
                if (spec != null) {
                    price = spec.getPrice();
                    specName = spec.getName();
                }
            }
            int qty = d.getQuantity() == null || d.getQuantity() <= 0 ? 1 : d.getQuantity();
            WorkOrderItem item = new WorkOrderItem();
            item.setItemId(si.getId());
            item.setItemName(si.getName());
            item.setSpecId(d.getSpecId());
            item.setSpecName(specName);
            item.setPrice(price);
            item.setQuantity(qty);
            item.setAmount(price.multiply(BigDecimal.valueOf(qty)));
            items.add(item);
        }
        return items;
    }

    private BigDecimal sumAmount(List<WorkOrderItem> items) {
        return items.stream()
                .map(WorkOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 工单号：WO + yyyyMMdd + 4 位当日序号，用 Redis INCR 保证多实例下不重复。
     * Redis 不可用时退化为时间戳后缀，保证系统仍可创建工单。
     */
    private String nextOrderNo() {
        String date = LocalDate.now().format(DATE_FMT);
        try {
            String key = "hestia:wo:seq:" + date;
            // 校准：Redis 计数可能因重启/初始化脚本落后于数据库，这里以数据库为准向上修正
            Long maxSeq = orderMapper.maxSeqOfDay(date);
            if (maxSeq != null && maxSeq > 0) {
                String cur = redisTemplate.opsForValue().get(key);
                long curVal = cur == null ? 0L : Long.parseLong(cur);
                if (maxSeq > curVal) {
                    redisTemplate.opsForValue().set(key, String.valueOf(maxSeq), 2, TimeUnit.DAYS);
                }
            }
            Long seq = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, 2, TimeUnit.DAYS);
            return "WO" + date + String.format("%04d", seq == null ? 1L : seq);
        } catch (Exception e) {
            log.warn("Redis 不可用，工单号降级为时间戳", e);
            return "WO" + date + (System.currentTimeMillis() % 10000);
        }
    }

    /** 补齐展示字段 */
    private void enrich(WorkOrderVO vo) {
        vo.setStatusName(WorkOrderStatus.descOf(vo.getStatus()));
        vo.setPriorityName(WorkOrderPriority.descOf(vo.getPriority()));
        vo.setSourceName(WorkOrderSource.descOf(vo.getSource()));
        slaService.fillSlaView(vo);
    }

    private String actionName(String action) {
        if (action == null) {
            return "";
        }
        switch (action) {
            case "CREATE": return "创建工单";
            case "ACCEPT": return "受理";
            case "DISPATCH": return "派单";
            case "AUTO_DISPATCH": return "智能派单";
            case "TAKE": return "接单";
            case "REJECT_DISPATCH": return "拒单";
            case "START": return "开始处理";
            case "FINISH": return "提交完工";
            case "CONFIRM": return "验收通过";
            case "REJECT": return "验收驳回";
            case "CANCEL": return "取消工单";
            case "URGE": return "催单";
            default: return action;
        }
    }
}
