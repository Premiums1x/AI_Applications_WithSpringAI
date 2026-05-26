package com.lancer.ai.controller;


import ModulesEnum.ServiceTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class GameController {
    private final ChatClient gameChatClient;
    //    流式,但需设置返回类型和字符编码，否则乱码
    @GetMapping(value = "/game",produces = "text/html;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam String prompt, @RequestParam String chatID){


//        2.发请求
        return gameChatClient
                .prompt()
                .user(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatID))//用户提示词构建好后拦截
                .stream()
                .content();
    }

}
