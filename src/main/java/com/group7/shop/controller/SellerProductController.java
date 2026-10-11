package com.group7.shop.controller;

import com.group7.shop.common.Result;
import com.group7.shop.dto.ProductView;
import com.group7.shop.dto.PublishProductRequest;
import com.group7.shop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
public class SellerProductController {
    private final ProductService productService;

    public SellerProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 该路由必须由李宇轩的 X-Token 拦截器保护。 */
    @PostMapping("/products")
    public Result<ProductView> publish(@Valid @RequestBody PublishProductRequest request) {
        return Result.success("商品发布成功", productService.publish(request));
    }

    /** 包含当前商品与历史商品，按创建时间倒序。 */
    @GetMapping("/products")
    public Result<List<ProductView>> products() {
        return Result.success(productService.sellerProducts());
    }

    @PostMapping("/products/{id}/freeze")
    public Result<ProductView> freeze(@PathVariable Long id) {
        return Result.success("商品已手动冻结", productService.freezeManually(id));
    }

    @PostMapping("/products/{id}/unfreeze")
    public Result<ProductView> unfreeze(@PathVariable Long id) {
        return Result.success("商品已解冻", productService.unfreezeManually(id));
    }

    @PostMapping("/products/{id}/offshelf")
    public Result<ProductView> offshelf(@PathVariable Long id) {
        return Result.success("商品已下架", productService.offshelf(id));
    }

    @GetMapping("/history")
    public Result<Map<String, List<ProductView>>> history() {
        return Result.success(Map.of("items", productService.history()));
    }
}
