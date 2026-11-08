package com.hestia.controller.admin;

import com.hestia.dto.CategoryDTO;
import com.hestia.dto.CategoryPageQueryDTO;
import com.hestia.entity.WorkOrderCategory;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 工单类型管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "管理端-工单类型")
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    @ApiOperation("启用的类型（下拉用）")
    public Result<List<WorkOrderCategory>> list() {
        return Result.success(categoryService.listEnabled());
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO query) {
        return Result.success(categoryService.pageQuery(query));
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody CategoryDTO dto) {
        categoryService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody CategoryDTO dto) {
        categoryService.update(dto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation("启用/停用")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }
}
