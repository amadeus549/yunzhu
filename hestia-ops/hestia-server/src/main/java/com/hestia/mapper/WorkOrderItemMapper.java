package com.hestia.mapper;

import com.hestia.entity.WorkOrderItem;
import com.hestia.vo.WorkOrderItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工单明细 Mapper
 */
@Mapper
public interface WorkOrderItemMapper {

    /** 批量插入，见 WorkOrderItemMapper.xml */
    void insertBatch(@Param("items") List<WorkOrderItem> items);

    @Select("select * from wo_item where order_id = #{orderId} order by id")
    List<WorkOrderItem> selectByOrderId(Long orderId);

    @Select("select id, item_id, item_name, spec_id, spec_name, price, quantity, amount from wo_item where order_id = #{orderId} order by id")
    List<WorkOrderItemVO> selectVOByOrderId(Long orderId);

    @Delete("delete from wo_item where order_id = #{orderId}")
    void deleteByOrderId(Long orderId);
}
