package com.hestia.mapper;

import com.hestia.entity.SlaRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * SLA 规则 Mapper
 */
@Mapper
public interface SlaRuleMapper {

    @Select("select * from sla_rule where priority = #{priority} and status = 1 limit 1")
    SlaRule selectByPriority(Integer priority);

    @Select("select * from sla_rule order by priority")
    List<SlaRule> selectAll();
}
