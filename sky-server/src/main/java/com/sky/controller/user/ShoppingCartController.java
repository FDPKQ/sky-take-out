package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("add shopping cart :{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);


        return Result.success();
    }
}
