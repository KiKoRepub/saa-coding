package org.cookpro.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.cookpro.dto.UserLoginDTO;
import org.cookpro.entity.User;
import org.cookpro.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {



    public Long getCurrentUserId() {
        // 这里应该从安全上下文或会话中获取当前用户的ID
        // 这是一个示例，实际实现可能需要根据你的认证机制进行调整
        return 1L; // 假设当前用户ID为1
    }


    public Long login(UserLoginDTO dto) {

        String userName = dto.getUserName();
        String password = dto.getPassword();

        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("username", userName)
                .eq("password", password);

            User user = this.getOne(queryWrapper);

            if (user != null) {
                return user.getId();
            }else return null;

    }


}
