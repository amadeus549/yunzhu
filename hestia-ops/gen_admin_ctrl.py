# -*- coding: utf-8 -*-
"""生成管理端 Controller"""
import os

CP = 'hestia-server/src/main/java/com/hestia/controller/admin'

TPL = '''package com.hestia.controller.admin;

{imports}

/**
 * {doc}
 */
@RestController
@RequestMapping("{path}")
@Api(tags = "{tag}")
@Slf4j
@RequiredArgsConstructor
public class {cls} {{

{deps}
{body}}}
'''

def base_imports(extra=''):
    return extra + '''import io.swagger.annotations.Api;
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
import org.springframework.web.bind.annotation.RestController;'''

CTRLS = []

CTRLS.append(dict(
    cls='AuthController', doc='管理端认证', path='/admin/auth', tag='管理端-认证',
    imports='''import com.hestia.constant.JwtClaimsConstant;
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
''' + base_imports() + '''

import java.util.HashMap;
import java.util.Map;''',
    deps='''    private final StaffService staffService;
    private final JwtProperties jwtProperties;
''',
    body='''    @PostMapping("/login")
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
'''))

CTRLS.append(dict(
    cls='StaffController', doc='员工管理', path='/admin/staff', tag='管理端-员工',
    imports='''import com.hestia.dto.StaffDTO;
import com.hestia.dto.StaffPageQueryDTO;
import com.hestia.entity.Staff;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.StaffService;
import com.hestia.vo.StaffVO;
''' + base_imports() + '''

import java.util.List;''',
    deps='''    private final StaffService staffService;
''',
    body='''    @GetMapping("/page")
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
'''))

CTRLS.append(dict(
    cls='ResidentController', doc='住户管理', path='/admin/resident', tag='管理端-住户',
    imports='''import com.hestia.dto.ResidentDTO;
import com.hestia.dto.ResidentPageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.ResidentService;
import com.hestia.vo.ResidentVO;
''' + base_imports() + '''

import java.util.List;''',
    deps='''    private final ResidentService residentService;
''',
    body='''    @GetMapping("/page")
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
'''))

CTRLS.append(dict(
    cls='PremiseController', doc='房屋点位管理', path='/admin/premise', tag='管理端-房屋点位',
    imports='''import com.hestia.dto.PremiseDTO;
import com.hestia.result.Result;
import com.hestia.service.PremiseService;
import com.hestia.vo.PremiseVO;
''' + base_imports() + '''

import java.util.List;''',
    deps='''    private final PremiseService premiseService;
''',
    body='''    @GetMapping("/list")
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
'''))

CTRLS.append(dict(
    cls='CategoryController', doc='工单类型管理', path='/admin/category', tag='管理端-工单类型',
    imports='''import com.hestia.dto.CategoryDTO;
import com.hestia.dto.CategoryPageQueryDTO;
import com.hestia.entity.WorkOrderCategory;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.CategoryService;
''' + base_imports() + '''

import java.util.List;''',
    deps='''    private final CategoryService categoryService;
''',
    body='''    @GetMapping("/list")
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
'''))

CTRLS.append(dict(
    cls='ServiceItemController', doc='服务项管理', path='/admin/service-item', tag='管理端-服务项',
    imports='''import com.hestia.dto.ServiceItemDTO;
import com.hestia.dto.ServiceItemPageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.ServiceItemService;
import com.hestia.vo.ServiceItemVO;
''' + base_imports() + '''

import java.util.List;''',
    deps='''    private final ServiceItemService itemService;
''',
    body='''    @GetMapping("/page")
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
'''))

CTRLS.append(dict(
    cls='ServicePackageController', doc='服务包管理', path='/admin/service-package', tag='管理端-服务包',
    imports='''import com.hestia.dto.ServicePackageDTO;
import com.hestia.dto.ServicePackagePageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.ServicePackageService;
import com.hestia.vo.ServicePackageVO;
''' + base_imports(),
    deps='''    private final ServicePackageService packageService;
''',
    body='''    @GetMapping("/page")
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
'''))

for c in CTRLS:
    src = TPL.format(**c)
    with open(os.path.join(CP, c['cls'] + '.java'), 'w', encoding='utf-8') as f:
        f.write(src)

print('generated', len(CTRLS))
