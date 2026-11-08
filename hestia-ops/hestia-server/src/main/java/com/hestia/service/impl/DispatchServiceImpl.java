package com.hestia.service.impl;

import com.hestia.entity.ServiceItem;
import com.hestia.entity.Staff;
import com.hestia.entity.WorkOrder;
import com.hestia.entity.WorkOrderItem;
import com.hestia.exception.WorkOrderBusinessException;
import com.hestia.mapper.ServiceItemMapper;
import com.hestia.mapper.StaffMapper;
import com.hestia.mapper.WorkOrderDispatchMapper;
import com.hestia.mapper.WorkOrderItemMapper;
import com.hestia.mapper.WorkOrderMapper;
import com.hestia.service.DispatchService;
import com.hestia.vo.DispatchCandidateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 智能派单实现。
 *
 * <p>评分模型（满分 100）：
 * <pre>
 *   技能匹配  60 分：完全覆盖所需技能 60；部分覆盖 30；完全不匹配 0
 *   负载余量  30 分：(1 - 在手工单/上限) * 30
 *   派单间隔  10 分：距上次派单 >=2h 得 10；>=30min 得 5；否则 0
 * </pre>
 * 技能不匹配不会被直接排除，但分数会明显落后，保证极端情况下仍有人可派。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private static final int SKILL_FULL = 60;
    private static final int SKILL_PARTIAL = 30;
    private static final int LOAD_FULL = 30;
    private static final int TIME_FULL = 10;

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderItemMapper orderItemMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final StaffMapper staffMapper;
    private final WorkOrderDispatchMapper dispatchMapper;

    @Override
    public List<DispatchCandidateVO> candidates(Long orderId) {
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) {
            throw new WorkOrderBusinessException("工单不存在");
        }
        Set<String> required = requiredSkills(orderId);
        List<Staff> engineers = staffMapper.selectEngineers(null);

        List<DispatchCandidateVO> result = new ArrayList<>();
        for (Staff staff : engineers) {
            Set<String> own = parseTags(staff.getSkillTags());
            int hit = 0;
            for (String s : required) {
                if (own.contains(s)) {
                    hit++;
                }
            }
            int skillScore;
            if (required.isEmpty() || hit == required.size()) {
                skillScore = SKILL_FULL;
            } else if (hit > 0) {
                skillScore = SKILL_PARTIAL;
            } else {
                skillScore = 0;
            }

            long active = staffMapper.countActiveOrders(staff.getId());
            int limit = staff.getWorkloadLimit() == null ? 5 : staff.getWorkloadLimit();
            double ratio = limit <= 0 ? 1.0 : Math.min(1.0, (double) active / limit);
            int loadScore = (int) Math.round((1 - ratio) * LOAD_FULL);

            LocalDateTime last = dispatchMapper.lastDispatchTime(staff.getId());
            int timeScore = 0;
            if (last == null) {
                timeScore = TIME_FULL;
            } else {
                long minutes = Duration.between(last, LocalDateTime.now()).toMinutes();
                if (minutes >= 120) {
                    timeScore = TIME_FULL;
                } else if (minutes >= 30) {
                    timeScore = TIME_FULL / 2;
                }
            }

            int total = skillScore + loadScore + timeScore;
            DispatchCandidateVO vo = new DispatchCandidateVO();
            vo.setStaffId(staff.getId());
            vo.setStaffName(staff.getName());
            vo.setPhone(staff.getPhone());
            vo.setSkillTags(staff.getSkillTags());
            vo.setActiveCount(active);
            vo.setWorkloadLimit(limit);
            vo.setMatchScore(total);
            vo.setMatched(skillScore > 0 ? 1 : 0);
            vo.setReason(String.format("技能 %d/%d 命中(+%d)，负载 %d/%d(+%d)，间隔(+%d)",
                    hit, required.size(), skillScore, active, limit, loadScore, timeScore));
            result.add(vo);
        }
        result.sort(Comparator.comparingInt(DispatchCandidateVO::getMatchScore).reversed());
        return result;
    }

    @Override
    public DispatchCandidateVO pickBest(Long orderId) {
        List<DispatchCandidateVO> list = candidates(orderId);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /** 汇总工单涉及的服务项所需技能 */
    private Set<String> requiredSkills(Long orderId) {
        Set<String> skills = new HashSet<>();
        List<WorkOrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (WorkOrderItem item : items) {
            ServiceItem si = serviceItemMapper.selectById(item.getItemId());
            if (si != null && si.getRequiredSkill() != null && !si.getRequiredSkill().isEmpty()) {
                skills.add(si.getRequiredSkill());
            }
        }
        return skills;
    }

    private Set<String> parseTags(String tags) {
        if (tags == null || tags.isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
