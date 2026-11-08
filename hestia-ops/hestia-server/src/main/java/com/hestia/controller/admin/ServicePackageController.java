package com.hestia.controller.admin;

import com.hestia.dto.ServicePackageDTO;
import com.hestia.dto.ServicePackagePageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.ServicePackageService;
import com.hestia.vo.ServicePackageVO;
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

/**
 * 服务包管理
 */
@RestController
@RequestMapping("/admin/service-package")
@Api(tags = "管理端-服务包")
@Slf4j
@RequiredArgsConstructor
public class ServicePackageController {

    private final ServicePackageService packageService;

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(ServicePackagePageQueryDTO query) {
        return Result.success(packageService.pageQuery(query));
    }

    @GetMapping("/{id}")
    @ApiOperation("详情")
    public Result<ServicePackageVO> detail(@PathVariable Long id) {
        return Result.success(packageService.detail(id));
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody ServicePackageDTO dto) {
        packageService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody ServicePackageDTO dto) {
        packageService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public Result<?> delete(@PathVariable Long id) {
        packageService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation("上架/下架")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        packageService.updateStatus(id, status);
        return Result.success();
    }
}
