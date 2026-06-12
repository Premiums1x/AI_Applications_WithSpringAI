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
public class CustomerController {
        private final ChatClient serviceChatClient;
        private final ChatHistoryRepository chatHistoryRepository;
        //自动注入接口实现类

//        @GetMapping("/service")
//        public String chat(@RequestParam String prompt){
//            return serviceChatClient
//                    .prompt()
//                    .user(prompt)//用户提示词
//                    .call()//阻塞式
//                    .content();
//        }

        //    流式,但需设置返回类型和字符编码，否则乱码
        @GetMapping(value = "/service",produces = "text/html;charset=UTF-8")
        public String streamChat(@RequestParam String prompt, @RequestParam String chatID){

//        1.保存会话id，再发请求：
//            注意这里类型写service，和bot区分开
            chatHistoryRepository.save(String.valueOf(ServiceTypes.service),chatID);

//        2.发请求
            return serviceChatClient
                    .prompt()
                    .user(prompt)
                    .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatID))//用户提示词构建好后拦截
//                    .stream()
                    .call()
                    .content();

        }




}
