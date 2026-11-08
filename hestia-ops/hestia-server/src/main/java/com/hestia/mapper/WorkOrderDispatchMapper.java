package com.hestia.mapper;

import com.hestia.entity.WorkOrderDispatch;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 派单记录 Mapper
 */
@Mapper
public interface WorkOrderDispatchMapper {

    @Insert("insert into wo_dispatch (order_id, staff_id, dispatch_type, match_score, reason, status, dispatch_time, accept_time, reject_time, create_time) " +
            "values (#{orderId}, #{staffId}, #{dispatchType}, #{matchScore}, #{reason}, #{status}, #{dispatchTime}, #{acceptTime}, #{rejectTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkOrderDispatch dispatch);

    @Update("update wo_dispatch set status = 2, accept_time = #{acceptTime} where order_id = #{orderId} and status = 1")
    void accept(@Param("orderId") Long orderId, @Param("acceptTime") LocalDateTime acceptTime);

    @Update("update wo_dispatch set status = 3, reject_time = #{rejectTime}, reason = #{reason} where order_id = #{orderId} and status = 1")
    void reject(@Param("orderId") Long orderId, @Param("rejectTime") LocalDateTime rejectTime, @Param("reason") String reason);

    @Select("select * from wo_dispatch where order_id = #{orderId} order by id desc")
    List<WorkOrderDispatch> selectByOrderId(Long orderId);

    /** 某工程师最近一次接单时间，用于派单排序 */
    @Select("select max(dispatch_time) from wo_dispatch where staff_id = #{staffId}")
    LocalDateTime lastDispatchTime(Long staffId);
}
