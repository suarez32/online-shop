package com.group7.shop.config;

import com.group7.shop.entity.Seller;
import com.group7.shop.repository.SellerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 卖家端 X-Token 登录拦截器（李宇轩负责）。
 * 拦截 /api/seller/**（放行 login）：校验请求头 X-Token，缺失或无效返回 401（统一 Result 格式）。
 * 校验通过后将 Seller 写入 request attribute "authenticatedSeller"，供 Controller 直接取用，
 * Controller 不再自行解析 token。
 */
@Component
public class SellerAuthInterceptor implements HandlerInterceptor {

    private final SellerRepository sellerRepository;

    public SellerAuthInterceptor(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String token = request.getHeader("X-Token");
        if (token == null || token.isBlank()) {
            return writeUnauthorized(response);
        }
        Seller seller = sellerRepository.findByToken(token).orElse(null);
        if (seller == null) {
            return writeUnauthorized(response);
        }
        request.setAttribute("authenticatedSeller", seller);
        return true;
    }

    private boolean writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已失效\",\"data\":null}");
        return false;
    }
}
