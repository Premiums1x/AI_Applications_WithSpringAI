package com.lancer.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//用了Mybatis记得要有mapper扫描：

@MapperScan("com.lancer.ai.mapper")
//排除Redis同名的vectorStore
@SpringBootApplication(exclude = RedisVectorStoreAutoConfiguration.class)
public class ChatRobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatRobotApplication.class, args);
    }

}
