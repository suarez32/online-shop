package com.group7.shop.controller;

import com.group7.shop.common.Result;
import com.group7.shop.dto.IntentQueryResult;
import com.group7.shop.dto.SubmitIntentRequest;
import com.group7.shop.entity.IntentStatus;
import com.group7.shop.service.IntentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 买家购买意向接口（负责人：华天遥），路径与 Wiki《接口设计》一致：
 * POST   /api/intents              提交意向，返回口令码（仅此一次）
 * GET    /api/intents/{code}       查排队位次与状态
 * PUT    /api/intents/{code}       修改姓名/电话，位次不变
 * POST   /api/intents/{code}/cancel 撤销意向，后续位次前移
 * 买家侧无账号体系，口令码即凭证，无需 X-Token。
 */
@RestController
@RequestMapping("/api/intents")
public class IntentController {

    private final IntentService intentService;

    public IntentController(IntentService intentService) {
        this.intentService = intentService;
    }

    @PostMapping
    public Result<Map<String, String>> submit(@RequestBody SubmitIntentRequest request) {
        String code = intentService.submit(request);
        return Result.success(Map.of("code", code));
    }

    @GetMapping("/{code}")
    public Result<IntentQueryResult> query(@PathVariable("code") String code) {
        return Result.success(intentService.query(code));
    }

    @PutMapping("/{code}")
    public Result<IntentQueryResult> update(@PathVariable("code") String code,
                                            @RequestBody SubmitIntentRequest request) {
        return Result.success(intentService.update(code, request));
    }

    @PostMapping("/{code}/cancel")
    public Result<Map<String, IntentStatus>> cancel(@PathVariable("code") String code) {
        IntentStatus status = intentService.cancel(code);
        return Result.success(Map.of("status", status));
    }
}
