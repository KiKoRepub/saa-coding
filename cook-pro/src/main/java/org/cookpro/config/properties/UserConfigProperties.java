package org.cookpro.config.properties;

import cn.dev33.satoken.stp.StpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.cookpro.entity.User;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
@Data
@Slf4j
@ConfigurationProperties(prefix = "user")
public class UserConfigProperties {



    private String baseDir;



    public String getHistoryDir() {

        String historyDir = getUserDir() + "/history";

        mkDir(historyDir);

        return historyDir;
    }
    public String getInfoDir(){
        String infoDir = getUserDir() + "/info";

        mkDir(infoDir);

        return infoDir;
    }
    public String getUserDir(){

        long userId = 1L;

        if (StpUtil.isLogin()){
            userId = StpUtil.getLoginIdAsLong();
        }else log.warn("用户未登录，使用默认用户ID: 1L");


        String userDir = getBaseDir() + '/' + userId;

        mkDir(userDir);

        return userDir;
    }
    private static void mkDir(String userDir) {
        File file = new File(userDir);
        // 如果目录不存在，则创建
        if (! file.exists()) file.mkdirs();
    }
}
