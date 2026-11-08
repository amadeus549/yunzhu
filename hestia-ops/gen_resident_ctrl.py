# -*- coding: utf-8 -*-
"""生成通用 Controller 与住户端 Controller"""
import os

ADMIN = 'hestia-server/src/main/java/com/hestia/controller/admin'
RES = 'hestia-server/src/main/java/com/hestia/controller/resident'

def write(path, src):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(src)

# ---------------- 通用上传 ----------------
write(os.path.join(ADMIN, 'CommonController.java'), '''package com.hestia.controller.admin;

import com.hestia.result.Result;
import com.hestia.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 通用能力
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "管理端-通用")
@Slf4j
@RequiredArgsConstructor
public class CommonController {

    private final AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传（报修照片等）")
    public Result<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf(".")) : ".jpg";
        String objectName = "hestia/" + LocalDate.now() + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            String url = aliOssUtil.upload(file.getBytes(), objectName);
            log.info("文件上传成功：{}", url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return Result.error("文件上传失败");
        } catch (Exception e) {
            log.error("OSS 上传失败", e);
            return Result.error("文件上传失败，请检查 OSS 配置");
        }
    }
}
''')

# ---------------- 住户端认证 ----------------
write(os.path.join(RES, 'ResidentAuthController.java'), '''package com.hestia.controller.resident;

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
''')

# ---------------- 住户端工单 ----------------
write(os.path.join(RES, 'ResidentWorkOrderController.java'), '''package com.hestia.controller.resident;

import com.hestia.context.BaseContext;
import com.hestia.dto.WorkOrderCancelDTO;
import com.hestia.dto.WorkOrderRatingDTO;
import com.hestia.dto.WorkOrderRejectDTO;
import com.hestia.dto.WorkOrderSubmitDTO;
import com.hestia.enumeration.OperatorType;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.WorkOrderService;
import com.hestia.vo.WorkOrderDetailVO;
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

/**
 * 住户端报修工单
 */
@RestController
@RequestMapping("/resident/work-order")
@Api(tags = "住户端-报修工单")
@Slf4j
@RequiredArgsConstructor
public class ResidentWorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    @ApiOperation("提交报修")
    public Result<Long> submit(@RequestBody WorkOrderSubmitDTO dto) {
        return Result.success(workOrderService.submit(dto, BaseContext.getCurrentId()));
    }

    @GetMapping("/page")
    @ApiOperation("我的工单分页")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer status) {
        return Result.success(workOrderService.pageQueryByResident(
                BaseContext.getCurrentId(), page, pageSize, status));
    }

    @GetMapping("/{id}")
    @ApiOperation("工单详情")
    public Result<WorkOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(workOrderService.detail(id));
    }

    @PutMapping("/{id}/confirm")
    @ApiOperation("验收通过并评价")
    public Result<?> confirm(@PathVariable Long id, @RequestBody WorkOrderRatingDTO dto) {
        dto.setOrderId(id);
        workOrderService.confirm(id, dto, BaseContext.getCurrentId());
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @ApiOperation("验收驳回")
    public Result<?> reject(@PathVariable Long id, @RequestBody WorkOrderRejectDTO dto) {
        dto.setOrderId(id);
        workOrderService.rejectAcceptance(dto, BaseContext.getCurrentId());
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消工单")
    public Result<?> cancel(@PathVariable Long id, @RequestBody WorkOrderCancelDTO dto) {
        dto.setOrderId(id);
        workOrderService.cancel(dto, BaseContext.getCurrentId(), OperatorType.RESIDENT.getCode());
        return Result.success();
    }

    @PostMapping("/{id}/urge")
    @ApiOperation("催单")
    public Result<?> urge(@PathVariable Long id) {
        workOrderService.urge(id, BaseContext.getCurrentId());
        return Result.success();
    }
}
''')

# ---------------- 住户端服务清单 ----------------
write(os.path.join(RES, 'ResidentCartController.java'), '''package com.hestia.controller.resident;

import com.hestia.context.BaseContext;
import com.hestia.dto.CartDTO;
import com.hestia.result.Result;
import com.hestia.service.CartService;
import com.hestia.vo.CartVO;
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
 * 住户端服务清单
 */
@RestController
@RequestMapping("/resident/cart")
@Api(tags = "住户端-服务清单")
@Slf4j
@RequiredArgsConstructor
public class ResidentCartController {

    private final CartService cartService;

    @GetMapping("/list")
    @ApiOperation("我的服务清单")
    public Result<List<CartVO>> list() {
        return Result.success(cartService.list(BaseContext.getCurrentId()));
    }

    @PostMapping
    @ApiOperation("加入清单")
    public Result<?> add(@RequestBody CartDTO dto) {
        cartService.add(BaseContext.getCurrentId(), dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @ApiOperation("修改数量")
    public Result<?> update(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(BaseContext.getCurrentId(), id, quantity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("移除")
    public Result<?> remove(@PathVariable Long id) {
        cartService.remove(BaseContext.getCurrentId(), id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    @ApiOperation("清空清单")
    public Result<?> clear() {
        cartService.clear(BaseContext.getCurrentId());
        return Result.success();
    }
}
''')

# ---------------- 住户端房屋 ----------------
write(os.path.join(RES, 'ResidentPremiseController.java'), '''package com.hestia.controller.resident;

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
''')

# ---------------- 住户端通知 ----------------
write(os.path.join(RES, 'ResidentNoticeController.java'), '''package com.hestia.controller.resident;

import com.hestia.context.BaseContext;
import com.hestia.entity.Notice;
import com.hestia.result.Result;
import com.hestia.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 住户端通知
 */
@RestController
@RequestMapping("/resident/notice")
@Api(tags = "住户端-通知")
@Slf4j
@RequiredArgsConstructor
public class ResidentNoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    @ApiOperation("我的通知")
    public Result<List<Notice>> list() {
        return Result.success(noticeService.list(BaseContext.getCurrentId(), 2));
    }

    @GetMapping("/unread-count")
    @ApiOperation("未读数量")
    public Result<Long> unreadCount() {
        return Result.success(noticeService.countUnread(BaseContext.getCurrentId(), 2));
    }

    @PutMapping("/read-all")
    @ApiOperation("全部已读")
    public Result<?> readAll() {
        noticeService.markAllRead(BaseContext.getCurrentId(), 2);
        return Result.success();
    }
}
''')

print('done')
