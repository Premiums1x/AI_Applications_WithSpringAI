package com.lancer.ai.repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{
    private final Map<String,List<String>> chatHistory = new HashMap<>();

    @Override
    public void save(String type, String chatID) {
//        判断是否已有该业务类型，无则新增并初始化；有则取出
        List<String> chatIDs = chatHistory.computeIfAbsent(type, k -> new ArrayList<>());
        if (chatIDs.contains(chatID)){
            return;
        }
        chatIDs.add(chatID);
    }

    @Override
    public List<String> getChatIDs(String type) {
//        如果有就根据key去取，否则返回默认值:空List
        return chatHistory.getOrDefault(type,List.of());
    }
}
