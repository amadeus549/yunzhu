package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.context.BaseContext;
import com.hestia.dto.InspectionExecuteDTO;
import com.hestia.dto.InspectionPlanDTO;
import com.hestia.dto.InspectionPlanPageQueryDTO;
import com.hestia.dto.InspectionTaskPageQueryDTO;
import com.hestia.dto.WorkOrderCreateDTO;
import com.hestia.dto.WorkOrderItemDTO;
import com.hestia.entity.Device;
import com.hestia.entity.InspectionPlan;
import com.hestia.entity.InspectionTask;
import com.hestia.enumeration.CycleType;
import com.hestia.enumeration.DeviceStatus;
import com.hestia.enumeration.InspectionResult;
import com.hestia.enumeration.InspectionTaskStatus;
import com.hestia.enumeration.WorkOrderPriority;
import com.hestia.enumeration.WorkOrderSource;
import com.hestia.exception.WorkOrderBusinessException;
import com.hestia.mapper.DeviceMapper;
import com.hestia.mapper.InspectionPlanMapper;
import com.hestia.mapper.InspectionTaskMapper;
import com.hestia.mapper.StaffMapper;
import com.hestia.result.PageResult;
import com.hestia.service.InspectionService;
import com.hestia.service.WorkOrderService;
import com.hestia.vo.InspectionPlanVO;
import com.hestia.vo.InspectionTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 巡检服务实现。
 *
 * <p>任务生成的两个关键点：
 * <ol>
 *   <li>幂等：唯一索引 (plan_id, device_id, plan_date) + 生成前 count 校验，定时任务重跑不会产生重复任务</li>
 *   <li>周期推演：按 cycle_type / cycle_value 推算下一次生成日期，支持每日、每周几、每月几号</li>
 * </ol>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InspectionPlanMapper planMapper;
    private final InspectionTaskMapper taskMapper;
    private final DeviceMapper deviceMapper;
    private final StaffMapper staffMapper;
    private final WorkOrderService workOrderService;
    private final StringRedisTemplate redisTemplate;

    // ==================== 计划 ====================

    @Override
    public PageResult planPageQuery(InspectionPlanPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<InspectionPlanVO> page = (Page<InspectionPlanVO>) planMapper.pageQuery(query);
        page.getResult().forEach(vo -> vo.setCycleDesc(cycleDesc(vo.getCycleType(), vo.getCycleValue())));
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void savePlan(InspectionPlanDTO dto) {
        InspectionPlan plan = new InspectionPlan();
        BeanUtils.copyProperties(dto, plan);
        plan.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        plan.setGenerateAheadDays(dto.getGenerateAheadDays() == null ? 1 : dto.getGenerateAheadDays());
        plan.setNextGenerateDate(dto.getStartDate());
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.insert(plan);
    }

    @Override
    public void updatePlan(InspectionPlanDTO dto) {
        InspectionPlan plan = new InspectionPlan();
        BeanUtils.copyProperties(dto, plan);
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.update(plan);
    }

    @Override
    public void updatePlanStatus(Long id, Integer status) {
        InspectionPlan plan = new InspectionPlan();
        plan.setId(id);
        plan.setStatus(status);
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.update(plan);
    }

    // ==================== 任务 ====================

    @Override
    public PageResult taskPageQuery(InspectionTaskPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<InspectionTaskVO> page = (Page<InspectionTaskVO>) taskMapper.pageQuery(query);
        page.getResult().forEach(this::enrichTask);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<InspectionTaskVO> listByExecutor(Long executorId, Integer status) {
        InspectionTaskPageQueryDTO q = new InspectionTaskPageQueryDTO();
        q.setPage(1);
        q.setPageSize(50);
        q.setExecutorId(executorId);
        q.setStatus(status);
        PageHelper.startPage(1, 50);
        List<InspectionTaskVO> list = taskMapper.pageQuery(q);
        list.forEach(this::enrichTask);
        return list;
    }

    @Override
    @Transactional
    public Long execute(InspectionExecuteDTO dto, Long operatorId) {
        InspectionTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new WorkOrderBusinessException("巡检任务不存在");
        }
        if (InspectionTaskStatus.COMPLETED.getCode() == task.getStatus()) {
            throw new WorkOrderBusinessException("该巡检任务已完成，请勿重复提交");
        }
        LocalDateTime now = LocalDateTime.now();
        task.setActualTime(now);
        task.setResult(dto.getResult());
        task.setAbnormalDesc(dto.getAbnormalDesc());
        task.setStatus(InspectionTaskStatus.COMPLETED.getCode());
        task.setUpdateTime(now);

        Long createdOrderId = null;
        boolean abnormal = InspectionResult.ABNORMAL.getCode() == dto.getResult();
        if (abnormal && dto.getCreateOrder() != null && dto.getCreateOrder() == 1) {
            Device device = task.getDeviceId() == null ? null : deviceMapper.selectById(task.getDeviceId());
            WorkOrderCreateDTO wo = new WorkOrderCreateDTO();
            wo.setCategoryId(device != null && "电梯".equals(device.getCategory()) ? 2L : 3L);
            wo.setPremiseId(device == null ? null : device.getPremiseId());
            wo.setTitle("巡检异常：" + (device == null ? "设备" : device.getName()));
            wo.setDescription(dto.getAbnormalDesc() == null ? "巡检发现设备异常" : dto.getAbnormalDesc());
            wo.setPriority(WorkOrderPriority.HIGH.getCode());
            wo.setSource(WorkOrderSource.INSPECTION.getCode());
            wo.setItems(new ArrayList<>());
            createdOrderId = workOrderService.create(wo);
            task.setOrderId(createdOrderId);
            // 设备转为维修中
            if (device != null) {
                Device update = new Device();
                update.setId(device.getId());
                update.setStatus(DeviceStatus.REPAIRING.getCode());
                update.setUpdateTime(now);
                deviceMapper.update(update);
            }
        }
        taskMapper.update(task);
        log.info("巡检任务 {} 执行完成，结果={} 转工单={}", task.getTaskNo(), dto.getResult(), createdOrderId);
        return createdOrderId;
    }

    @Override
    @Transactional
    public int generateTasks(Long planId) {
        InspectionPlan plan = planMapper.selectById(planId);
        if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
            return 0;
        }
        LocalDate target = plan.getNextGenerateDate() == null ? LocalDate.now() : plan.getNextGenerateDate();
        int ahead = plan.getGenerateAheadDays() == null ? 0 : plan.getGenerateAheadDays();
        // 未到生成窗口直接返回，避免连续点击「立即生成」把未来几期任务一次性抽干
        if (target.isAfter(LocalDate.now().plusDays(ahead))) {
            log.debug("巡检计划 {} 未到生成时间（{}），跳过", plan.getName(), target);
            return 0;
        }
        List<Device> devices = resolveDevices(plan.getDeviceCategory());
        int created = 0;
        for (Device device : devices) {
            // 幂等：同一计划 + 同一设备 + 同一天只生成一条
            if (taskMapper.countByUnique(planId, device.getId(), target) > 0) {
                continue;
            }
            InspectionTask task = new InspectionTask();
            task.setTaskNo(nextTaskNo(target));
            task.setPlanId(planId);
            task.setDeviceId(device.getId());
            task.setExecutorId(plan.getExecutorId());
            task.setPlanDate(target);
            task.setStatus(InspectionTaskStatus.PENDING.getCode());
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.insert(task);
            created++;
        }
        plan.setLastGenerateDate(target);
        plan.setNextGenerateDate(calcNext(plan, target));
        plan.setUpdateTime(LocalDateTime.now());
        planMapper.update(plan);
        log.info("巡检计划 {} 生成任务 {} 条，下次生成 {}", plan.getName(), created, plan.getNextGenerateDate());
        return created;
    }

    @Override
    @Transactional
    public int scanAndGenerate() {
        List<InspectionPlan> due = planMapper.selectDuePlans(LocalDate.now());
        int total = 0;
        for (InspectionPlan p : due) {
            total += generateTasks(p.getId());
        }
        return total;
    }

    @Override
    @Transactional
    public int markOverdue() {
        InspectionTaskPageQueryDTO q = new InspectionTaskPageQueryDTO();
        q.setPage(1);
        q.setPageSize(500);
        q.setStatus(InspectionTaskStatus.PENDING.getCode());
        PageHelper.startPage(1, 500);
        List<InspectionTaskVO> list = taskMapper.pageQuery(q);
        int count = 0;
        LocalDate today = LocalDate.now();
        for (InspectionTaskVO vo : list) {
            if (vo.getPlanDate() != null && vo.getPlanDate().isBefore(today)) {
                InspectionTask update = new InspectionTask();
                update.setId(vo.getId());
                update.setStatus(InspectionTaskStatus.OVERDUE.getCode());
                update.setUpdateTime(LocalDateTime.now());
                taskMapper.update(update);
                count++;
            }
        }
        return count;
    }

    // ==================== 私有 ====================

    private List<Device> resolveDevices(String categories) {
        if (categories == null || categories.isEmpty()) {
            return deviceMapper.selectEnabled();
        }
        List<Device> devices = new ArrayList<>();
        for (String c : categories.split(",")) {
            String cat = c.trim();
            if (!cat.isEmpty()) {
                devices.addAll(deviceMapper.selectByCategory(cat));
            }
        }
        return devices;
    }

    /** 按周期类型推算下一次生成日期 */
    private LocalDate calcNext(InspectionPlan plan, LocalDate from) {
        int type = plan.getCycleType() == null ? CycleType.WEEKLY.getCode() : plan.getCycleType();
        if (type == CycleType.DAILY.getCode()) {
            return from.plusDays(1);
        }
        if (type == CycleType.WEEKLY.getCode()) {
            Set<DayOfWeek> days = parseWeekdays(plan.getCycleValue());
            LocalDate d = from.plusDays(1);
            for (int i = 0; i < 8; i++) {
                if (days.contains(d.getDayOfWeek())) {
                    return d;
                }
                d = d.plusDays(1);
            }
            return from.plusWeeks(1);
        }
        if (type == CycleType.MONTHLY.getCode()) {
            Set<Integer> days = parseMonthDays(plan.getCycleValue());
            LocalDate d = from.plusDays(1);
            for (int i = 0; i < 62; i++) {
                if (days.contains(d.getDayOfMonth())) {
                    return d;
                }
                d = d.plusDays(1);
            }
            return from.plusMonths(1);
        }
        return from.plusDays(7);
    }

    private Set<DayOfWeek> parseWeekdays(String value) {
        Set<DayOfWeek> set = new HashSet<>();
        if (value == null || value.isEmpty()) {
            set.add(DayOfWeek.MONDAY);
            return set;
        }
        for (String v : value.split(",")) {
            String s = v.trim().toUpperCase();
            switch (s) {
                case "MON": set.add(DayOfWeek.MONDAY); break;
                case "TUE": set.add(DayOfWeek.TUESDAY); break;
                case "WED": set.add(DayOfWeek.WEDNESDAY); break;
                case "THU": set.add(DayOfWeek.THURSDAY); break;
                case "FRI": set.add(DayOfWeek.FRIDAY); break;
                case "SAT": set.add(DayOfWeek.SATURDAY); break;
                case "SUN": set.add(DayOfWeek.SUNDAY); break;
                default: break;
            }
        }
        if (set.isEmpty()) {
            set.add(DayOfWeek.MONDAY);
        }
        return set;
    }

    private Set<Integer> parseMonthDays(String value) {
        Set<Integer> set = new HashSet<>();
        if (value == null || value.isEmpty()) {
            set.add(1);
            return set;
        }
        set = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .map(Integer::parseInt)
                .filter(d -> d >= 1 && d <= 28)
                .collect(Collectors.toSet());
        if (set.isEmpty()) {
            set.add(1);
        }
        return set;
    }

    private String cycleDesc(Integer type, String value) {
        if (type == null) {
            return "";
        }
        int t = type;
        if (t == CycleType.DAILY.getCode()) {
            return "每日";
        }
        if (t == CycleType.WEEKLY.getCode()) {
            return "每周 " + (value == null ? "MON" : value);
        }
        if (t == CycleType.MONTHLY.getCode()) {
            return "每月 " + (value == null ? "1" : value) + " 号";
        }
        return "自定义";
    }

    private String nextTaskNo(LocalDate date) {
        String d = date.format(DATE_FMT);
        try {
            String key = "hestia:it:seq:" + d;
            Long maxSeq = taskMapper.maxSeqOfDay(d);
            if (maxSeq != null && maxSeq > 0) {
                String cur = redisTemplate.opsForValue().get(key);
                long curVal = cur == null ? 0L : Long.parseLong(cur);
                if (maxSeq > curVal) {
                    redisTemplate.opsForValue().set(key, String.valueOf(maxSeq), 2, TimeUnit.DAYS);
                }
            }
            Long seq = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, 2, TimeUnit.DAYS);
            return "IT" + d + String.format("%03d", seq == null ? 1L : seq);
        } catch (Exception e) {
            return "IT" + d + (System.currentTimeMillis() % 1000);
        }
    }

    private void enrichTask(InspectionTaskVO vo) {
        vo.setResultName(InspectionResult.descOf(vo.getResult()));
        vo.setStatusName(InspectionTaskStatus.descOf(vo.getStatus()));
    }
}
