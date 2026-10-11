package com.group7.shop.service;

import com.group7.shop.common.BusinessException;
import com.group7.shop.common.PasswordHasher;
import com.group7.shop.dto.IntentView;
import com.group7.shop.dto.LoginResponse;
import com.group7.shop.dto.ProductView;
import com.group7.shop.dto.SellerOverview;
import com.group7.shop.entity.Intent;
import com.group7.shop.entity.IntentStatus;
import com.group7.shop.entity.Product;
import com.group7.shop.entity.ProductStatus;
import com.group7.shop.entity.Seller;
import com.group7.shop.repository.IntentRepository;
import com.group7.shop.repository.ProductRepository;
import com.group7.shop.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号与后台域核心服务（李宇轩负责）。
 * 登录/登出/改密/后台总览均在此实现；token 仅存于 Seller.token 字段，不做服务端会话。
 * 密码哈希使用 PasswordHasher（JDK 内置，无第三方依赖）。
 */
@Service
public class SellerService {

    private static final List<ProductStatus> ACTIVE_STATUSES =
            List.of(ProductStatus.ON_SALE, ProductStatus.FROZEN);

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final IntentRepository intentRepository;

    public SellerService(SellerRepository sellerRepository,
                         ProductRepository productRepository,
                         IntentRepository intentRepository) {
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.intentRepository = intentRepository;
    }

    @Transactional
    public LoginResponse login(String username, String password) {
        Seller seller = sellerRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.unauthorized("用户名或密码错误"));
        if (!PasswordHasher.matches(password, seller.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        // 重新登录使旧 token 失效：覆盖为新 token
        seller.setToken(generateToken());
        sellerRepository.save(seller);
        return new LoginResponse(seller.getToken(), seller.getUsername());
    }

    @Transactional
    public void logout(Seller seller) {
        seller.setToken(null);
        sellerRepository.save(seller);
    }

    @Transactional
    public void changePassword(Seller seller, String oldPassword, String newPassword) {
        if (!PasswordHasher.matches(oldPassword, seller.getPasswordHash())) {
            throw BusinessException.badRequest("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw BusinessException.badRequest("新密码不少于8位");
        }
        // 换盐重算哈希
        String salt = PasswordHasher.generateSalt();
        seller.setSalt(salt);
        seller.setPasswordHash(salt + ":" + PasswordHasher.hash(salt, newPassword));
        // 改后旧 token 失效
        seller.setToken(null);
        sellerRepository.save(seller);
    }

    @Transactional(readOnly = true)
    public SellerOverview overview(Seller seller) {
        Product product = productRepository.findByStatusInOrderByCreatedAtDesc(ACTIVE_STATUSES)
                .stream().findFirst().orElse(null);
        ProductView productView = product == null ? null : ProductView.from(product);

        List<IntentView> queue = new ArrayList<>();
        if (product != null) {
            List<Intent> intents = intentRepository.findByProductIdOrderBySubmittedAtAsc(product.getId());
            int position = 0;
            for (Intent intent : intents) {
                if (intent.getStatus() == IntentStatus.WAITING) {
                    position++;
                }
                if (intent.getStatus() == IntentStatus.WAITING || intent.getStatus() == IntentStatus.IN_TRADE) {
                    // 已进入交易的意向 position 置空
                    Integer pos = intent.getStatus() == IntentStatus.IN_TRADE ? null : position;
                    queue.add(new IntentView(
                            intent.getId(),
                            intent.getName(),
                            intent.getPhone(),
                            intent.getStatus(),
                            pos,
                            intent.getSubmittedAt()));
                }
            }
        }

        boolean canPublish = !productRepository.existsByStatusIn(ACTIVE_STATUSES);
        return new SellerOverview(productView, queue, canPublish);
    }

    private String generateToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "")
                + java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
