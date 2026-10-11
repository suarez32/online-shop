package com.group7.shop.service;

import com.group7.shop.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductImageStorage {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Pattern DATA_URI =
            Pattern.compile("^data:image/(jpeg|png);base64,([A-Za-z0-9+/=\\r\\n]+)$");

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public String storeOrPlaceholder(String dataUri) {
        if (dataUri == null || dataUri.isBlank()) {
            return "/placeholder.png";
        }

        Matcher matcher = DATA_URI.matcher(dataUri.trim());
        if (!matcher.matches()) {
            throw BusinessException.badRequest("图片仅支持 JPG/PNG 格式的 Base64 数据");
        }

        String subtype = matcher.group(1);
        byte[] bytes;
        try {
            bytes = Base64.getMimeDecoder().decode(matcher.group(2));
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest("图片 Base64 数据无效");
        }

        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw BusinessException.badRequest("图片大小必须大于0且不超过5MB");
        }

        String extension = "png".equals(subtype) ? ".png" : ".jpg";
        String filename = UUID.randomUUID() + extension;
        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve(filename), bytes);
        } catch (IOException ex) {
            // 上传失败按接口约定回退占位图，而不是阻止商品发布。
            return "/placeholder.png";
        }
        return "/uploads/" + filename;
    }
}
