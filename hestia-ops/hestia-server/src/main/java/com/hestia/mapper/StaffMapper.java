package com.hestia.mapper;

import com.hestia.dto.StaffPageQueryDTO;
import com.hestia.entity.Staff;
import com.hestia.vo.StaffVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工 Mapper
 */
@Mapper
public interface StaffMapper {

    @Select("select * from sys_staff where username = #{username}")
    Staff selectByUsername(String username);

    @Select("select * from sys_staff where id = #{id}")
    Staff selectById(Long id);

    @Select("select * from sys_staff where status = 1 order by role, staff_no")
    List<Staff> selectAll();

    @Insert("insert into sys_staff (staff_no, name, username, password, phone, avatar, role, skill_tags, workload_limit, status, create_time, update_time) " +
            "values (#{staffNo}, #{name}, #{username}, #{password}, #{phone}, #{avatar}, #{role}, #{skillTags}, #{workloadLimit}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Staff staff);

    /** 动态更新，见 StaffMapper.xml */
    void update(Staff staff);

    /** 分页查询（含在手工单数），见 StaffMapper.xml */
    List<StaffVO> pageQuery(StaffPageQueryDTO query);

    /** 在手工单数：已派单/已接单/处理中/待验收/已驳回 */
    @Select("select count(*) from wo_order where assignee_id = #{staffId} and status in (3,4,5,6,8)")
    Long countActiveOrders(Long staffId);

    /** 查询在岗工程师，按技能标签过滤（skill 为空则不过滤），见 StaffMapper.xml */
    List<Staff> selectEngineers(@Param("skill") String skill);

    @Select("select count(*) from sys_staff where role = 2 and status = 1")
    Long countEngineers();

    @Select("select count(*) from sys_staff")
    Long countAll();
}
