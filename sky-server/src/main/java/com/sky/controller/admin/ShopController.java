package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
public class ShopController {
    @Resource
    private ShopService shopService;


    @PutMapping("/{status}")
    public Result<String> setStatus(@PathVariable Integer status) {
        shopService.setStatus(status);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = shopService.getStatus();
        return Result.success(status);
    }
}
