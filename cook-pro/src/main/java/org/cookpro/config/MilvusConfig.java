package org.cookpro.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.AlterCollectionFieldReq;

public class MilvusConfig {







    public static void main(String[] args) {
        // milvus 2.5.0 之后才支持 修改字段信息
        // 当前是 2.4.6 版本，main 方法不能执行
        ConnectConfig config = ConnectConfig.builder()
                .uri("http://localhost:19530")
                .token("root:Milvus")
                .build();
        MilvusClientV2 client = new MilvusClientV2(config);

        client.alterCollectionField(AlterCollectionFieldReq.builder()
                .collectionName("saa-recipe")
                .fieldName("content")
                .property("max_length", "1024")
                .build());
    }
}
