package com.hestia.mapper;

import com.hestia.entity.Premise;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 房屋/点位 Mapper
 */
@Mapper
public interface PremiseMapper {

    @Select("select * from sys_premise where id = #{id}")
    Premise selectById(Long id);

    @Select("select * from sys_premise where resident_id = #{residentId} order by is_default desc, id")
    List<Premise> selectByResidentId(Long residentId);

    @Select("select * from sys_premise order by community, building, room_no")
    List<Premise> selectAll();

    @Insert("insert into sys_premise (resident_id, community, building, unit, room_no, premise_type, area, is_default, create_time, update_time) " +
            "values (#{residentId}, #{community}, #{building}, #{unit}, #{roomNo}, #{premiseType}, #{area}, #{isDefault}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Premise premise);

    @Update("update sys_premise set resident_id = #{residentId}, community = #{community}, building = #{building}, unit = #{unit}, " +
            "room_no = #{roomNo}, premise_type = #{premiseType}, area = #{area}, is_default = #{isDefault}, update_time = #{updateTime} where id = #{id}")
    void update(Premise premise);

    @Delete("delete from sys_premise where id = #{id}")
    void deleteById(Long id);

    @Select("select count(*) from sys_premise where resident_id = #{residentId}")
    Long countByResidentId(Long residentId);
}
