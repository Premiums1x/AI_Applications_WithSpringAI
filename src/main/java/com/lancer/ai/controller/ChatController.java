package com.lancer.ai.controller;

import ModulesEnum.ServiceTypes;
import com.lancer.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
//为chatClient生成构造方法，Spring自动注入

@RestController
@RequestMapping("/ai")
public class ChatController {
    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;
    //自动注入接口实现类


    @GetMapping("/chat")
    public String chat(@RequestParam String prompt){
        return chatClient
                .prompt()
                .user(prompt)//用户提示词
                .call()//阻塞式
                .content();
    }

//    流式,但需设置返回类型和字符编码，否则乱码
    @GetMapping(value = "/chat/stream",produces = "text/html;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam String prompt, @RequestParam String chatID){
//        原来 String prompt 和 String chatID 没加注解，Spring MVC
//      会把它们当作可选参数。如果请求里没带这些参数（比如有人直接访问地址、浏览器预检请求等），
//      它们就是 null，传给 ChatClient.user(null) 就会抛出 IllegalArgumentException: value cannot   be null。
//      加了 @RequestParam 后，参数缺失时 Spring 会直接返回 400 错误，不会走到 ChatClient 的代码。


//        1.保存会话id，再发请求：
        chatHistoryRepository.save(String.valueOf(ServiceTypes.chat),chatID);

//        2.发请求
        return chatClient
                .prompt()
                .user(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatID))//用户提示词构建好后拦截
                .stream()
                .content();
    }


}
