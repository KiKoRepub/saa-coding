package org.cookpro.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.cookpro.R;
import org.cookpro.dto.UserLoginDTO;
import org.cookpro.service.*;
import org.cookpro.sse.SSEServer;
import org.cookpro.utils.ValidateCodeUtils;
import org.cookpro.vo.UserInfoVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private SSEService sseService;
    @Resource
    private UserService userService;

    @Resource
    private ValidateService validateService;
    @Resource
    UserPreferenceService userPreferenceService;



    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public R<UserInfoVo> getUserInfo(){
        StpUtil.checkLogin();
        return R.ok(userService.getUserInfo());
    }
    @GetMapping("/token")
    @Operation(summary = "获取用户Token")
    public R<String> getToken(){
        StpUtil.checkLogin();
        return R.ok(StpUtil.getTokenValue());
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<String> login(@RequestBody UserLoginDTO dto,HttpServletRequest request){

        if (StpUtil.isLogin()){
            return R.ok("用户已登录");
        }

        // 1. 获取存储的验证码
        String code = validateService.getLoginValidateCode(request);

        // 2. 校验（忽略大小写）
        boolean isValid = code != null &&
                code.equalsIgnoreCase(dto.getValidateCode());

        // 3. 校验后删除验证码（防止重复使用）
        if (isValid) {
            validateService.removeLoginValidateCode(request);
            Long userId = userService.login(dto);
            if (userId  == null) {
                return R.error("登录失败，用户名或密码错误");
            }

            // 消费掉积压的消息
            sseService.onUserConnect(userId, SSEServer.connect(userId));
            // 登录成功后分析用户偏好
            userPreferenceService.analyzePreferencesWithCache(userId);

            return R.ok("登陆成功");
        }
        return R.error("验证码错误");

    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public R<String> logout(){
        StpUtil.checkLogin();
        StpUtil.logout();
        return R.ok("登出成功");
    }
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public R<String> register(@RequestBody UserLoginDTO dto){
        String userId = userService.register(dto);
        if (userId == null) {
            return R.error("注册失败，用户名可能已存在");
        }
        return R.ok("注册成功");
    }

    @GetMapping("/code")
    @Operation(summary = "获取验证码图片")
    public R<byte[]> getValidateCode(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 生成随机验证码
            String code = validateService.getLoginValidateCode(request);

            // 3. 生成验证码图片字节数组
            byte[] imageBytes = ValidateCodeUtils.generateCodeImage(code);
            
            
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            
            return R.ok(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}


