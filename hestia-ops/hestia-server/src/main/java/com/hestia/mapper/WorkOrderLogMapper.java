package com.hestia.mapper;

import com.hestia.entity.WorkOrderLog;
import com.hestia.vo.WorkOrderLogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工单流转日志 Mapper
 */
@Mapper
public interface WorkOrderLogMapper {

    @Insert("insert into wo_log (order_id, operator_id, operator_type, action, from_status, to_status, remark, create_time) " +
            "values (#{orderId}, #{operatorId}, #{operatorType}, #{action}, #{fromStatus}, #{toStatus}, #{remark}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkOrderLog log);

    /** 带操作人姓名，见 WorkOrderLogMapper.xml */
    List<WorkOrderLogVO> selectVOByOrderId(Long orderId);

    @Select("select count(*) from wo_log where order_id = #{orderId}")
    Long countByOrderId(Long orderId);
}
