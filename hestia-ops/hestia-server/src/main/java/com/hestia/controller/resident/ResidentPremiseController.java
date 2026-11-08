package com.hestia.controller.resident;

import com.hestia.context.BaseContext;
import com.hestia.result.Result;
import com.hestia.service.PremiseService;
import com.hestia.vo.PremiseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 住户端房屋
 */
@RestController
@RequestMapping("/resident/premise")
@Api(tags = "住户端-房屋")
@Slf4j
@RequiredArgsConstructor
public class ResidentPremiseController {

    private final PremiseService premiseService;

    @GetMapping("/list")
    @ApiOperation("我的房屋列表")
    public Result<List<PremiseVO>> list() {
        return Result.success(premiseService.listByResident(BaseContext.getCurrentId()));
    }
}
