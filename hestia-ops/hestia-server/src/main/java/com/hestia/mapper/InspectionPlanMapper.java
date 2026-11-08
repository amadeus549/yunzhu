package com.hestia.mapper;

import com.hestia.dto.InspectionPlanPageQueryDTO;
import com.hestia.entity.InspectionPlan;
import com.hestia.vo.InspectionPlanVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 巡检计划 Mapper
 */
@Mapper
public interface InspectionPlanMapper {

    @Select("select * from insp_plan where id = #{id}")
    InspectionPlan selectById(Long id);

    @Insert("insert into insp_plan (name, cycle_type, cycle_value, device_category, executor_id, start_date, generate_ahead_days, " +
            "last_generate_date, next_generate_date, status, create_time, update_time) " +
            "values (#{name}, #{cycleType}, #{cycleValue}, #{deviceCategory}, #{executorId}, #{startDate}, #{generateAheadDays}, " +
            "#{lastGenerateDate}, #{nextGenerateDate}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InspectionPlan plan);

    /** 动态更新，见 InspectionPlanMapper.xml */
    void update(InspectionPlan plan);

    List<InspectionPlanVO> pageQuery(InspectionPlanPageQueryDTO query);

    @Select("select * from insp_plan where status = 1 order by id")
    List<InspectionPlan> selectEnabled();

    /** 到达生成日期的计划 */
    @Select("select * from insp_plan where status = 1 and next_generate_date is not null and next_generate_date <= #{date} order by next_generate_date")
    List<InspectionPlan> selectDuePlans(LocalDate date);
}
