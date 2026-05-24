package com.lancer.ai.controller;

import com.lancer.ai.entity.vo.MessageVO;
import com.lancer.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("ai/history")
public class ChatHistoryController {
//    会话列表数据源
    private final ChatHistoryRepository chatHistoryRepository;
//每个会话的详细记录数据源：
    private final ChatMemory chatMemory;

    @GetMapping("{type}")
    public List<String> getChatIDs(@PathVariable("type") String type){
//        根据业务类型来查询所有会话ID
        return chatHistoryRepository.getChatIDs(type);
    }

    @GetMapping("{type}/{chatID}")
    public List<MessageVO> getChatHistorys(@PathVariable("type")String type,@PathVariable("chatID") String chatID){
        List<Message> messages = chatMemory.get(chatID);
        if (messages == null){
            return List.of();
            //如果没有对话记录返回空
        }
//        return messages.stream().map(m->new MessageVO(m)).toList();
        return messages.stream().map(MessageVO::new).toList();
    }


}
