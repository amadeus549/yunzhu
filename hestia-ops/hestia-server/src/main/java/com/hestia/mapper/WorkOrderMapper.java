package com.hestia.mapper;

import com.hestia.dto.WorkOrderPageQueryDTO;
import com.hestia.entity.WorkOrder;
import com.hestia.vo.CategoryReportVO;
import com.hestia.vo.SlaReportVO;
import com.hestia.vo.StaffPerformanceVO;
import com.hestia.vo.StatusDistributionVO;
import com.hestia.vo.TrendVO;
import com.hestia.vo.WorkOrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单 Mapper —— 系统的聚合根
 */
@Mapper
public interface WorkOrderMapper {

    @Insert("insert into wo_order (order_no, category_id, premise_id, resident_id, title, description, priority, status, source, " +
            "sla_rule_id, response_deadline, resolve_deadline, expect_time, assignee_id, dispatcher_id, total_amount, " +
            "response_breached, resolve_breached, is_rework, rework_count, create_time, update_time, create_user, update_user) " +
            "values (#{orderNo}, #{categoryId}, #{premiseId}, #{residentId}, #{title}, #{description}, #{priority}, #{status}, #{source}, " +
            "#{slaRuleId}, #{responseDeadline}, #{resolveDeadline}, #{expectTime}, #{assigneeId}, #{dispatcherId}, #{totalAmount}, " +
            "#{responseBreached}, #{resolveBreached}, #{isRework}, #{reworkCount}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkOrder order);

    @Select("select * from wo_order where id = #{id}")
    WorkOrder selectById(Long id);

    /** 动态更新，见 WorkOrderMapper.xml */
    void update(WorkOrder order);

    /** 拒单/退回派单池时需要把处理人置空，动态 SQL 无法更新 null 字段，单独提供 */
    @org.apache.ibatis.annotations.Update("update wo_order set assignee_id = null, status = #{status}, update_time = #{updateTime} where id = #{id}")
    void clearAssignee(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /** 多条件分页查询，见 WorkOrderMapper.xml */
    List<WorkOrderVO> pageQuery(WorkOrderPageQueryDTO query);

    /** 单条详情视图，见 WorkOrderMapper.xml */
    WorkOrderVO selectVOById(Long id);

    @Select("select count(*) from wo_order where date(create_time) = curdate()")
    Long countTodayNew();

    @Select("select count(*) from wo_order where status = #{status}")
    Long countByStatus(Integer status);

    @Select("select count(*) from wo_order where status = 7 and date(complete_time) = curdate()")
    Long countTodayCompleted();

    /** SLA 超时数量：已标记超时，或截止时间已过但状态未终结 */
    @Select("select count(*) from wo_order where status in (1,2,3,4,5,6,8) and (response_breached = 1 or resolve_breached = 1 or resolve_deadline < now())")
    Long countSlaOverdue();

    @Select("select count(*) from wo_order where status in (1,2,3,4,5,6,8) and priority = 4")
    Long countUrgentActive();

    /** 扫描应判定超时的工单，见 WorkOrderMapper.xml */
    List<WorkOrder> selectTimeoutOrders(@Param("now") LocalDateTime now);

    /** 扫描临近截止的工单用于预警，见 WorkOrderMapper.xml */
    List<WorkOrder> selectWarningOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 平均响应时长（分钟），见 WorkOrderMapper.xml */
    Double avgResponseMinutes(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /** 按时完成率，见 WorkOrderMapper.xml */
    Double onTimeRate(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    // ---------------- 报表 ----------------

    List<StatusDistributionVO> countGroupByStatus(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    List<TrendVO> countTrend(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    List<CategoryReportVO> countGroupByCategory(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    SlaReportVO slaReport(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    List<StaffPerformanceVO> staffPerformance(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /** 当天已有工单的最大序号，用于初始化 Redis 计数器 */
    @Select("select ifnull(max(cast(substr(order_no, 11) as unsigned)), 0) from wo_order where order_no like concat('WO', #{date}, '%')")
    Long maxSeqOfDay(@Param("date") String date);

    /** 工程师在手工单分布，用于工作台 */
    @Select("select assignee_id, count(*) from wo_order where status in (3,4,5,6,8) and assignee_id is not null group by assignee_id")
    List<java.util.Map<String, Object>> countAssigneeLoad();
}
