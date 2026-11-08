package com.hestia.controller.admin;

import com.hestia.dto.StaffDTO;
import com.hestia.dto.StaffPageQueryDTO;
import com.hestia.entity.Staff;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.StaffService;
import com.hestia.vo.StaffVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/staff")
@Api(tags = "管理端-员工")
@Slf4j
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(StaffPageQueryDTO query) {
        return Result.success(staffService.pageQuery(query));
    }

    @GetMapping("/engineers")
    @ApiOperation("工程师列表（可按技能过滤）")
    public Result<List<Staff>> engineers(@RequestParam(required = false) String skill) {
        return Result.success(staffService.listEngineers(skill));
    }

    @GetMapping("/{id}")
    @ApiOperation("详情")
    public Result<StaffVO> detail(@PathVariable Long id) {
        return Result.success(staffService.detail(id));
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody StaffDTO dto) {
        staffService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody StaffDTO dto) {
        staffService.update(dto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation("启用/禁用")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        staffService.updateStatus(id, status);
        return Result.success();
    }
}
