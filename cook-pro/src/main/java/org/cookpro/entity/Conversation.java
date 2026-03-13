package org.cookpro.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("conversation")
@EqualsAndHashCode(callSuper = true)
public class Conversation extends BaseEntity {

        private Long userId;

        private String conversationId;

        private String title;

        private String lastMessage;


}
