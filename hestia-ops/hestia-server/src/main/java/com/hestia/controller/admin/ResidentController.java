package com.hestia.controller.admin;

import com.hestia.dto.ResidentDTO;
import com.hestia.dto.ResidentPageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.ResidentService;
import com.hestia.vo.ResidentVO;
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
 * 住户管理
 */
@RestController
@RequestMapping("/admin/resident")
@Api(tags = "管理端-住户")
@Slf4j
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(ResidentPageQueryDTO query) {
        return Result.success(residentService.pageQuery(query));
    }

    @GetMapping("/list")
    @ApiOperation("全部住户（下拉用）")
    public Result<List<ResidentVO>> list() {
        return Result.success(residentService.listAll());
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody ResidentDTO dto) {
        residentService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody ResidentDTO dto) {
        residentService.update(dto);
        return Result.success();
    }
}
