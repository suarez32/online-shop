package com.group7.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册卖家端鉴权拦截器：覆盖 /api/seller/**，放行 /api/seller/login。
 * 买家侧接口（/api/product、/api/intents）不在此拦截范围内，由其自身口令码机制保护。
 */
@Configuration
public class SellerAuthWebConfig implements WebMvcConfigurer {

    private final SellerAuthInterceptor sellerAuthInterceptor;

    public SellerAuthWebConfig(SellerAuthInterceptor sellerAuthInterceptor) {
        this.sellerAuthInterceptor = sellerAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sellerAuthInterceptor)
                .addPathPatterns("/api/seller/**")
                .excludePathPatterns("/api/seller/login");
    }
}
