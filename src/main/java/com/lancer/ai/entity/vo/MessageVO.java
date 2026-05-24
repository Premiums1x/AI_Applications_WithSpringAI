package com.lancer.ai.entity.vo;

import lombok.Data;
import org.springframework.ai.chat.messages.Message;

//规范展示给前端的Message格式
@Data
public class MessageVO {
//    消息包含的内容
    private String role;
    private  String context;

//    创建构造函数，从Message接口拿到想要的type和content:
    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER -> role = "user";
            case ASSISTANT -> role ="assitants";
            default -> role = "";
        }
        this.context = message.getText();


    }

}
