package com.lancer.ai.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Bean
    public ChatClient chatClient(OllamaChatModel model,ChatMemory chatMemory){
        return ChatClient
                .builder(model)//创建工厂，传入模型
                .defaultSystem("你是一个智能编码助手，名称叫Lcode，请你用专业化的语气回答我的提问")//默认系统提示词
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                         MessageChatMemoryAdvisor.builder(chatMemory).build() )//日志环绕增强
                .build();//创建客户端

    }

    @Bean
    public ChatMemory chatMemory () {
        return MessageWindowChatMemory.builder().build();
    };

}
