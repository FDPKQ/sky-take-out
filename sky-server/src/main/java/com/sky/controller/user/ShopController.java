package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController {
    @Resource
    private ShopService shopService;

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = shopService.getStatus();
        return Result.success(status);
    }
}
