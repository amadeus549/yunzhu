package com.hestia.controller.admin;

import com.hestia.dto.DeviceDTO;
import com.hestia.dto.DevicePageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.DeviceService;
import com.hestia.vo.DeviceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设备台账管理
 */
@RestController
@RequestMapping("/admin/device")
@Api(tags = "管理端-设备台账")
@Slf4j
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DevicePageQueryDTO query) {
        return Result.success(deviceService.pageQuery(query));
    }

    @GetMapping("/categories")
    @ApiOperation("设备类别列表")
    public Result<List<String>> categories() {
        return Result.success(deviceService.listCategories());
    }

    @GetMapping("/maintain-due")
    @ApiOperation("到期待维保设备")
    public Result<List<DeviceVO>> maintainDue() {
        return Result.success(deviceService.listMaintainDue());
    }

    @GetMapping("/maintain-due/count")
    @ApiOperation("待到期待维保数量")
    public Result<Long> maintainDueCount() {
        return Result.success(deviceService.countMaintainDue());
    }

    @GetMapping("/{id}")
    @ApiOperation("详情")
    public Result<DeviceVO> detail(@PathVariable Long id) {
        return Result.success(deviceService.detail(id));
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody DeviceDTO dto) {
        deviceService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody DeviceDTO dto) {
        deviceService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public Result<?> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }
}
