package org.example.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import jakarta.annotation.Resource;
import org.example.UserInfo;
import org.example.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    // 会话登录接口
    @PostMapping("/doLogin")
    public SaResult doLogin(@RequestParam("name") String name,
                            @RequestParam("password") String password) {

        UserInfo userInfo = userService.login(name, password);
        if (userInfo == null) return SaResult.error("用户名或密码错误");
        else {
            StpUtil.login(userInfo);

            return SaResult.ok("登录成功");
        }

    }

    @PostMapping("/dologin/phone")
    public SaResult doLoginByPhone(@RequestParam("phoneNumber") String phoneNumber) {
        // 这里为了演示方便，直接使用手机号当做登录账号，实际项目中可以根据业务需求进行调整
        UserInfo userInfo = userService.login(phoneNumber);
        StpUtil.login(userInfo);
        return SaResult.error("登录失败");
    }

    @GetMapping("/info")
    public SaResult info() {
        if (StpUtil.isLogin()) {
            SaSession session = StpUtil.getSession();
            Object loginId = session.getLoginId();
            if (loginId !=null){
                System.out.println(loginId);
            }
            UserInfo userInfo = userService.getUserInfo(loginId);
            return SaResult.ok("当前登录用户信息：").setData(userInfo);
        } else {
            return SaResult.error("请先登录");
        }
    }

     @PostMapping("/logout")
    public SaResult logout() {
         StpUtil.logout();
         return SaResult.ok("退出登录成功");
     }

}
