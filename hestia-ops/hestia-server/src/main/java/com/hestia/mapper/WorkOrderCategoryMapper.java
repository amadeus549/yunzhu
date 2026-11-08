package com.hestia.mapper;

import com.hestia.dto.CategoryPageQueryDTO;
import com.hestia.entity.WorkOrderCategory;
import com.hestia.vo.CategoryVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工单类型 Mapper
 */
@Mapper
public interface WorkOrderCategoryMapper {

    @Select("select * from wo_category where id = #{id}")
    WorkOrderCategory selectById(Long id);

    @Select("select * from wo_category where status = 1 order by sort")
    List<WorkOrderCategory> selectEnabled();

    @Select("select * from wo_category order by sort")
    List<WorkOrderCategory> selectAll();

    @Insert("insert into wo_category (name, biz_type, icon, sort, status, create_time, update_time) " +
            "values (#{name}, #{bizType}, #{icon}, #{sort}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkOrderCategory category);

    /** 动态更新，见 WorkOrderCategoryMapper.xml */
    void update(WorkOrderCategory category);

    List<CategoryVO> pageQuery(CategoryPageQueryDTO query);
}
