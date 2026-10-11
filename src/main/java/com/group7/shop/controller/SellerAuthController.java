package com.group7.shop.controller;

import com.group7.shop.common.Result;
import com.group7.shop.dto.ChangePasswordRequest;
import com.group7.shop.dto.LoginRequest;
import com.group7.shop.dto.LoginResponse;
import com.group7.shop.dto.SellerOverview;
import com.group7.shop.entity.Seller;
import com.group7.shop.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号与后台域接口（李宇轩负责）：
 *  POST /api/seller/login    登录，返回 token
 *  POST /api/seller/logout   退出，token 失效
 *  POST /api/seller/password 修改密码（校验原密码，改后旧 token 失效）
 *  GET  /api/seller/overview 后台首页聚合（在售商品 + 意向购买人列表）
 *
 * 已认证 Seller 由 SellerAuthInterceptor 写入 request attribute，此处直接取用，不解析 X-Token。
 */
@RestController
@RequestMapping("/api/seller")
public class SellerAuthController {

    private final SellerService sellerService;

    public SellerAuthController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sellerService.login(request.username(), request.password());
        return Result.success("登录成功", response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute("authenticatedSeller") Seller seller) {
        sellerService.logout(seller);
        return Result.success("已退出登录", null);
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@RequestAttribute("authenticatedSeller") Seller seller,
                                      @Valid @RequestBody ChangePasswordRequest request) {
        sellerService.changePassword(seller, request.oldPassword(), request.newPassword());
        return Result.success("密码修改成功，请重新登录", null);
    }

    @GetMapping("/overview")
    public Result<SellerOverview> overview(@RequestAttribute("authenticatedSeller") Seller seller) {
        return Result.success(sellerService.overview(seller));
    }
}
