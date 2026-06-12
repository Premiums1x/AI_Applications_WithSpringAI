package com.lancer.ai.controller;

import ModulesEnum.ServiceTypes;
import com.lancer.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

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
    @RequestMapping(value = "/chat/stream",produces = "text/html;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam("prompt") String prompt,
                                   @RequestParam("chatId") String chatID,
//                                   改为可选，不然如果后面聊天不传文件了会报错认为缺少参数。
                                   @RequestParam(value = "files",required = false)List<MultipartFile> files){
//        原来 String prompt 和 String chatID 没加注解，Spring MVC
//      会把它们当作可选参数。如果请求里没带这些参数（比如有人直接访问地址、浏览器预检请求等），
//      它们就是 null，传给 ChatClient.user(null) 就会抛出 IllegalArgumentException: value cannot   be null。
//      加了 @RequestParam 后，参数缺失时 Spring 会直接返回 400 错误，不会走到 ChatClient 的代码。


//        1.保存会话id，再发请求：
        chatHistoryRepository.save(String.valueOf(ServiceTypes.chat),chatID);

//        2.发请求(判断有无附件，决定聊天类型)
        if (files == null){
            return textChat(prompt,chatID);
        }else {
            return multiModalChat(prompt,chatID,files);
        }
    }

    private Flux<String> multiModalChat(String prompt, String chatID, List<MultipartFile> files) {
//        1.解析多媒体：
        List<Media> medias = files.stream().map(file -> new Media(MimeType.valueOf(Objects.requireNonNull(file.getContentType())), file.getResource()))
                .toList();


        return chatClient
                .prompt()
//                提示词改为传递多模态类型
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatID))//用户提示词构建好后拦截
                .stream()
                .content();
    }

    private Flux<String> textChat(String prompt, String chatID) {
        return chatClient
                .prompt()
                .user(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatID))//用户提示词构建好后拦截
                .stream()
                .content();
    }


}
