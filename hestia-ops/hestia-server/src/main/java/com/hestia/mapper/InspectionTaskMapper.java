package com.hestia.mapper;

import com.hestia.dto.InspectionTaskPageQueryDTO;
import com.hestia.entity.InspectionTask;
import com.hestia.vo.InspectionTaskVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 巡检任务 Mapper
 */
@Mapper
public interface InspectionTaskMapper {

    @Select("select * from insp_task where id = #{id}")
    InspectionTask selectById(Long id);

    @Insert("insert into insp_task (task_no, plan_id, device_id, executor_id, plan_date, actual_time, result, abnormal_desc, order_id, status, create_time, update_time) " +
            "values (#{taskNo}, #{planId}, #{deviceId}, #{executorId}, #{planDate}, #{actualTime}, #{result}, #{abnormalDesc}, #{orderId}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InspectionTask task);

    /** 动态更新，见 InspectionTaskMapper.xml */
    void update(InspectionTask task);

    List<InspectionTaskVO> pageQuery(InspectionTaskPageQueryDTO query);

    /** 幂等校验：同一计划、同一设备、同一天只生成一条任务 */
    @Select("select count(*) from insp_task where plan_id = #{planId} and device_id = #{deviceId} and plan_date = #{planDate}")
    int countByUnique(@Param("planId") Long planId, @Param("deviceId") Long deviceId, @Param("planDate") LocalDate planDate);

    /** 当天已有任务的最大序号 */
    @Select("select ifnull(max(cast(substr(task_no, 11) as unsigned)), 0) from insp_task where task_no like concat('IT', #{date}, '%')")
    Long maxSeqOfDay(@Param("date") String date);

    @Select("select count(*) from insp_task where status = 1")
    Long countPending();

    @Select("select count(*) from insp_task where status = 3")
    Long countOverdue();
}
