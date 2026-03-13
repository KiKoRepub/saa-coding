package org.cookpro.config;

import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.stores.BaseStore;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MemoryConfig {


    @Autowired
    DataSource dataSource;

//    @Bean
//    public BaseStore memoryStore() {
//        // 使用数据库存储作为记忆存储
//        // 存储 聊天记录、会话的索引、用户画像等信息
//        return new DatabaseStore(dataSource);
//    }


    // TODO 引入 MongoDB 作为具体消息内容存储的 实现


}
