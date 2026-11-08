package com.hestia.controller.resident;

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
