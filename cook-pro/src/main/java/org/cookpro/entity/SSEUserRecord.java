package org.cookpro.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/*
    保存用户的SSE 消息传递记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SSEUserRecord extends BaseEntity{


    private Long id;

    private Long userId;

    private Long toId;

    private String eventName;

    private String data;

    private Long timestamp;

}
