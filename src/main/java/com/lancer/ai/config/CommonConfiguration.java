package com.lancer.ai.config;


import com.lancer.ai.constants.SystemConstants;
import com.lancer.ai.tools.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CommonConfiguration {
@Bean
public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
    return SimpleVectorStore.builder(embeddingModel).build();
}


    @Bean
    @Primary
    public EmbeddingModel embeddingModel(OpenAiEmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel model,ChatMemory chatMemory){
        return ChatClient
                .builder(model)//创建工厂，传入模型
//              模型改用多模态
//                .defaultOptions((ChatOptions.Builder) ChatOptions.builder().model("Qwen3-Omni-30B-A3B-Instruct").build())
                .defaultOptions(ChatOptions.builder().model("Qwen3.6-35B-A3B"))
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


    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatMemory chatMemory){
        return ChatClient
                .builder(model)//创建工厂，传入模型
                .defaultSystem(SystemConstants.GAME_PROMPT)//默认系统提示词
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build() )//日志环绕增强
                .build();//创建客户端
    }

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel model, ChatMemory chatMemory, CourseTools courseTools){
        return ChatClient
                .builder(model)//创建工厂，传入模型
                .defaultSystem(SystemConstants.Customer_Service_PROMPT)//提示词
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build() )//日志环绕增强
                .defaultTools(courseTools)
                .build();//创建客户端
    }

    @Bean
    public ChatClient pdfChatClient(OpenAiChatModel model, ChatMemory chatMemory, VectorStore vectorStore){
        return ChatClient
                .builder(model)//创建工厂，传入模型
                .defaultSystem("请根据上下文进行回答问题，遇到上下文没有的问题，不能随意捏造")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(), //日志环绕增强
                        QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().similarityThreshold(0.6).topK(2).build()).build()
                ).build();//创建客户端
    }

}
