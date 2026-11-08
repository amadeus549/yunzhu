package com.hestia.controller.resident;

import com.hestia.constant.JwtClaimsConstant;
import com.hestia.context.BaseContext;
import com.hestia.dto.ResidentLoginDTO;
import com.hestia.entity.Resident;
import com.hestia.properties.JwtProperties;
import com.hestia.result.Result;
import com.hestia.service.ResidentService;
import com.hestia.utils.JwtUtil;
import com.hestia.vo.PremiseVO;
import com.hestia.service.PremiseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 住户端认证
 */
@RestController
@RequestMapping("/resident/auth")
@Api(tags = "住户端-认证")
@Slf4j
@RequiredArgsConstructor
public class ResidentAuthController {

    private final ResidentService residentService;
    private final PremiseService premiseService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation("手机号 + 验证码登录（演示验证码 123456）")
    public Result<Map<String, Object>> login(@RequestBody ResidentLoginDTO dto) {
        Resident resident = residentService.login(dto.getPhone(), dto.getCode());
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.RESIDENT_ID, resident.getId());
        String token = JwtUtil.createJWT(jwtProperties.getResidentSecretKey(),
                jwtProperties.getResidentTtl(), claims);

        Map<String, Object> data = new HashMap<>();
        data.put("id", resident.getId());
        data.put("name", resident.getName());
        data.put("phone", resident.getPhone());
        data.put("token", token);
        return Result.success(data);
    }

    @GetMapping("/info")
    @ApiOperation("当前住户信息")
    public Result<Map<String, Object>> info() {
        Long id = BaseContext.getCurrentId();
        List<PremiseVO> premises = premiseService.listByResident(id);
        Map<String, Object> data = new HashMap<>();
        data.put("residentId", id);
        data.put("premises", premises);
        return Result.success(data);
    }
}
