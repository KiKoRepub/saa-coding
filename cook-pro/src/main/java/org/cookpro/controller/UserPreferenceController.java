package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.service.UserPreferenceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/preference")
public class UserPreferenceController {


    @Resource
    UserPreferenceService userPreferenceService;



    @PostMapping("/analyze")
    @Operation(summary = "分析用户偏好", description = "基于聊天历史分析用户的烹饪偏好")
    public R<String> getUserPreferences(@RequestParam("userId") Long userId) {
        String preferences = userPreferenceService.analyzeUserPreferences(userId);
        return R.ok(preferences);
    }
}
