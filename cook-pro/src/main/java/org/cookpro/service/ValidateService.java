package org.cookpro.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cookpro.utils.ValidateCodeUtils;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class ValidateService {
    @Resource
    private RedisService redisService;
    private static final String VALIDATE_CODE = "VALIDATE_CODE";
    public String getLoginValidateCode(HttpServletRequest request){
        // 存储验证码到Redis，关联当前会话ID，设置过期时间（5分钟）

        String code = ValidateCodeUtils.generateCode();


        String cacheKey = VALIDATE_CODE + ":" + request.getSession().getId();
        if (redisService.exists(cacheKey)){
            return redisService.getString(cacheKey);
        }
        redisService.cacheObject(cacheKey, code, 5L, TimeUnit.MINUTES);

        return code;
    }


    public void removeLoginValidateCode(HttpServletRequest request){
        String cacheKey = VALIDATE_CODE + ":" + request.getSession().getId();
        redisService.delete(cacheKey);
    }
}
