package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class UserService {


    private static final List<UserInfo> userList = List.of(
            new UserInfo(1, "sa", "123456"),
            new UserInfo(2, "admin", "123456"),
            new UserInfo(3, "test", "123456")
    );


    public UserInfo login(String name,String password){


        return userList.stream()
                .filter(userInfo -> userInfo.getName().equals(name) && userInfo.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }
    public UserInfo login(String phoneNumber){
        log.warn("使用手机号登录(未验证短信)，手机号：{}", phoneNumber);
        return userList.stream()
                .filter(userInfo -> userInfo.getPhoneNumber().equals(phoneNumber))
                .findFirst()
                .orElse(null);
    }

    public UserInfo getUserInfo(Object loginId) {
        return userList.stream()
                .filter(userInfo -> userInfo.getId() == Integer.parseInt(loginId.toString()))
                .findFirst()
                .orElse(null);
    }
}
