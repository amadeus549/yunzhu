package com.hestia.controller.admin;

import com.hestia.dto.ServiceItemDTO;
import com.hestia.dto.ServiceItemPageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.ServiceItemService;
import com.hestia.vo.ServiceItemVO;
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
 * 服务项管理
 */
@RestController
@RequestMapping("/admin/service-item")
@Api(tags = "管理端-服务项")
@Slf4j
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService itemService;

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(ServiceItemPageQueryDTO query) {
        return Result.success(itemService.pageQuery(query));
    }

    @GetMapping("/list")
    @ApiOperation("按工单类型列出上架服务项")
    public Result<List<ServiceItemVO>> list(@RequestParam Long categoryId) {
        return Result.success(itemService.listByCategory(categoryId));
    }

    @GetMapping("/{id}")
    @ApiOperation("详情")
    public Result<ServiceItemVO> detail(@PathVariable Long id) {
        return Result.success(itemService.detail(id));
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody ServiceItemDTO dto) {
        itemService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody ServiceItemDTO dto) {
        itemService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public Result<?> delete(@PathVariable Long id) {
        itemService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation("上架/下架")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        itemService.updateStatus(id, status);
        return Result.success();
    }
}
