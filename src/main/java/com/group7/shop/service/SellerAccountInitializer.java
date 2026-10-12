package com.group7.shop.service;

import com.group7.shop.common.PasswordHasher;
import com.group7.shop.entity.Seller;
import com.group7.shop.repository.SellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 账号自动初始化（李宇轩负责：Seller 实体与账号自动初始化）。
 * 数据库无卖家账号时，用 application.yml 中的默认凭据创建一个，并在日志打印。
 * 仅首次启动执行（count == 0 判定），幂等，重启不会重复创建。
 */
@Component
public class SellerAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SellerAccountInitializer.class);

    private final SellerRepository sellerRepository;

    @Value("${app.default-seller.username:seller}")
    private String defaultUsername;

    @Value("${app.default-seller.password:12345678}")
    private String defaultPassword;

    public SellerAccountInitializer(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Override
    public void run(String... args) {
        if (sellerRepository.count() == 0) {
            String salt = PasswordHasher.generateSalt();
            Seller seller = new Seller(
                    defaultUsername,
                    salt,
                    salt + ":" + PasswordHasher.hash(salt, defaultPassword),
                    null);
            sellerRepository.save(seller);
            log.info("=========================================================");
            log.info("  默认卖家账号已初始化（仅首次启动）");
            log.info("  用户名: {}    密码: {}", defaultUsername, defaultPassword);
            log.info("  可在 application.yml 通过 app.default-seller.* 覆盖");
            log.info("=========================================================");
        }
    }
}
