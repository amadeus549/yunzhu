package com.hestia.controller.resident;

import com.hestia.entity.WorkOrderCategory;
import com.hestia.result.Result;
import com.hestia.service.CategoryService;
import com.hestia.service.ServiceItemService;
import com.hestia.vo.ServiceItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 住户端通用数据：报修页所需的字典
 */
@RestController
@RequestMapping("/resident/common")
@Api(tags = "住户端-通用")
@Slf4j
@RequiredArgsConstructor
public class ResidentCommonController {

    private final CategoryService categoryService;
    private final ServiceItemService itemService;

    @GetMapping("/category-list")
    @ApiOperation("报修可选的工单类型（启用中）")
    public Result<List<WorkOrderCategory>> categoryList() {
        return Result.success(categoryService.listEnabled());
    }

    @GetMapping("/service-list")
    @ApiOperation("按工单类型列出上架服务项")
    public Result<List<ServiceItemVO>> serviceList(@RequestParam Long categoryId) {
        return Result.success(itemService.listByCategory(categoryId));
    }
}
