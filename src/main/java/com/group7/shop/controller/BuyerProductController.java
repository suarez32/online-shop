package com.group7.shop.controller;

import com.group7.shop.common.Result;
import com.group7.shop.dto.ProductView;
import com.group7.shop.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api")
public class BuyerProductController {
    private final ProductService productService;

    public BuyerProductController(ProductService productService) {
        this.productService = productService;
    }

    /** Wiki: GET /api/product -> data: { product: ProductView | null } */
    @GetMapping("/product")
    public Result<Map<String, ProductView>> currentProduct() {
        ProductView product = productService.currentBuyerProduct();
        return Result.success(Collections.singletonMap("product", product));
    }
}
