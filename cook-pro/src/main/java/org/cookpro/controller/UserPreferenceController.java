package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.service.UserPreferenceService;
import org.cookpro.vo.UserPreferenceVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/preference")
public class UserPreferenceController {


    @Resource
    UserPreferenceService userPreferenceService;




    @GetMapping("/one")
    @Operation(summary = "获取用户喜好信息", description = "获取用户喜好信息")
    public R<UserPreferenceVo> getPreference(@RequestParam("userId") Long userId){
        return R.ok(userPreferenceService.getPreference(userId));
    }

}
