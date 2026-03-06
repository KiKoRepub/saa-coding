package org.cookpro.service;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.BagUtils;
import org.apache.commons.lang3.StringUtils;
import org.cookpro.dto.UserLoginDTO;
import org.cookpro.entity.User;
import org.cookpro.mapper.UserMapper;
import org.cookpro.vo.UserInfoVo;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {



    public Long getCurrentUserId() {
         return StpUtil.getLoginIdAsLong();
    }


    public Long login(UserLoginDTO dto) {
        // 接入 sa-token 进行管理
        String userName = dto.getUserName();
        String password = dto.getPassword();

        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("username", userName)
                .eq("password", password);

            User user = this.getOne(queryWrapper);

            if (user != null) {
                StpUtil.login(user.getId());

                return user.getId();
            }else return null;

    }


    public String register(UserLoginDTO dto) {
        String userName = dto.getUserName();
        String password = dto.getPassword();

        // 1. 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("username", userName);
        if (this.count(queryWrapper) > 0) {
            return null; // 用户名已存在，注册失败
        }
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(password)) {

            User user = new User();
            user.setUsername(userName);
            user.setPassword(password);

            this.save(user);

            return user.getId().toString(); // 返回新注册用户的ID
        } else {
            return null; // 用户名或密码为空，注册失败
        }

    }

    public UserInfoVo getUserInfo() {
        Long currentUserId = getCurrentUserId();

        User user = getById(currentUserId);

        UserInfoVo userInfoVo = new UserInfoVo();

        BeanUtil.copyProperties(user, userInfoVo);

        // TODO 补充用户偏好信息
        return userInfoVo;
    }
}
