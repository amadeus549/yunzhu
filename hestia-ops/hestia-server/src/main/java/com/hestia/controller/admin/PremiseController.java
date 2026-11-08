package com.hestia.controller.admin;

import com.hestia.dto.PremiseDTO;
import com.hestia.result.Result;
import com.hestia.service.PremiseService;
import com.hestia.vo.PremiseVO;
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
 * 房屋点位管理
 */
@RestController
@RequestMapping("/admin/premise")
@Api(tags = "管理端-房屋点位")
@Slf4j
@RequiredArgsConstructor
public class PremiseController {

    private final PremiseService premiseService;

    @GetMapping("/list")
    @ApiOperation("全部点位（下拉用）")
    public Result<List<PremiseVO>> list() {
        return Result.success(premiseService.listAll());
    }

    @GetMapping("/{id}")
    @ApiOperation("详情")
    public Result<PremiseVO> detail(@PathVariable Long id) {
        return Result.success(premiseService.detail(id));
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody PremiseDTO dto) {
        premiseService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody PremiseDTO dto) {
        premiseService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public Result<?> delete(@PathVariable Long id) {
        premiseService.delete(id);
        return Result.success();
    }
}
