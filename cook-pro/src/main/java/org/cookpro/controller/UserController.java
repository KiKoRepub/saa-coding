package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.cookpro.R;
import org.cookpro.dto.UserLoginDTO;
import org.cookpro.service.SSEService;
import org.cookpro.service.UserService;
import org.cookpro.sse.SSEServer;
import org.cookpro.utils.ValidateCodeUtils;
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
    private static final String VALIDATE_CODE = "VALIDATE_CODE";





    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<String> login(@RequestBody UserLoginDTO dto,HttpServletRequest request){
// 1. 获取存储的验证码
        // ========== 方式1：从Session获取 ==========
        String storedCode = (String) request.getSession().getAttribute(VALIDATE_CODE);

        // ========== 方式2：从Redis获取 ==========
        // String sessionId = request.getSession().getId();
        // String storedCode = stringRedisTemplate.opsForValue().get("VALIDATE_CODE:" + sessionId);

        // 2. 校验（忽略大小写）
        boolean isValid = storedCode != null &&
                storedCode.equalsIgnoreCase(dto.getValidateCode());
        // 3. 校验后删除验证码（防止重复使用）
        if (isValid) {
            request.getSession().removeAttribute(VALIDATE_CODE);
            Long userId = userService.login(dto);
            if (userId  == null) {
                return R.error("登录失败，用户名或密码错误");
            }
            // stringRedisTemplate.delete("VALIDATE_CODE:" + sessionId);
            // 消费掉积压的消息
            sseService.onUserConnect(userId, SSEServer.connect(userId));
        }

        return R.ok("登陆成功");
    }


    @GetMapping("/code")
    @Operation(summary = "获取验证码图片")
    public R<byte[]> getValidateCode(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 生成随机验证码
            String code = ValidateCodeUtils.generateCode();
            // 2. 存储验证码（二选一：Session 或 Redis）
            // ========== 方式1：使用Session存储（无需Redis） ==========
            HttpSession session = request.getSession();
            // 存储验证码，设置Session过期时间（可选）
            session.setAttribute(VALIDATE_CODE, code);
            session.setMaxInactiveInterval(300); // 5分钟过期

            // ========== 方式2：使用Redis存储（推荐，分布式场景） ==========
            // String sessionId = request.getSession().getId();
            // stringRedisTemplate.opsForValue().set(
            //     "VALIDATE_CODE:" + sessionId,
            //     code,
            //     5, // 5分钟过期
            //     TimeUnit.MINUTES
            // );

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


