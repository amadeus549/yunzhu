package com.hestia.controller.admin;

import com.hestia.constant.JwtClaimsConstant;
import com.hestia.context.BaseContext;
import com.hestia.dto.PasswordEditDTO;
import com.hestia.dto.StaffLoginDTO;
import com.hestia.entity.Staff;
import com.hestia.enumeration.StaffRole;
import com.hestia.properties.JwtProperties;
import com.hestia.result.Result;
import com.hestia.service.StaffService;
import com.hestia.utils.JwtUtil;
import com.hestia.vo.StaffLoginVO;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端认证
 */
@RestController
@RequestMapping("/admin/auth")
@Api(tags = "管理端-认证")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final StaffService staffService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation("登录")
    public Result<StaffLoginVO> login(@RequestBody StaffLoginDTO dto) {
        Staff staff = staffService.login(dto);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.STAFF_ID, staff.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(), claims);

        StaffLoginVO vo = new StaffLoginVO();
        vo.setId(staff.getId());
        vo.setUsername(staff.getUsername());
        vo.setName(staff.getName());
        vo.setRole(staff.getRole());
        vo.setRoleName(StaffRole.descOf(staff.getRole()));
        vo.setToken(token);
        return Result.success(vo);
    }

    @GetMapping("/info")
    @ApiOperation("当前登录人信息")
    public Result<StaffVO> info() {
        return Result.success(staffService.detail(BaseContext.getCurrentId()));
    }

    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<?> editPassword(@RequestBody PasswordEditDTO dto) {
        staffService.editPassword(BaseContext.getCurrentId(), dto);
        return Result.success();
    }

    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result<?> logout() {
        return Result.success();
    }
}
