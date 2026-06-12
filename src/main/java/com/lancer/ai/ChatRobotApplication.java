package com.lancer.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//用了Mybatis记得要有mapper扫描：

@MapperScan("com.lancer.ai.mapper")
@SpringBootApplication
public class ChatRobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatRobotApplication.class, args);
    }

}
