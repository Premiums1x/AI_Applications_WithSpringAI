package com.lancer.ai;

import com.lancer.ai.util.VectorDistanceUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ChatRobotApplicationTests {
    @Autowired
    OpenAiEmbeddingModel embeddingModel;
//    @Autowired(required = false)  // 如果没有配置RedisTemplate也不会导致测试启动失败
//    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private VectorStore vectorStore;


    @Test
    void contextLoads() {
    }

    @Test
    void testembedding(){

        float[] hahahas = embeddingModel.embed("hahaha");
        System.out.println(Arrays.toString(hahahas));
    }
    @Test
    public void testEmbedding() {
        // 1.测试数据
        // 1.1.用来查询的文本，国际冲突
        String query = "global conflicts";

        // 1.2.用来做比较的文本
        String[] texts = new String[]{
                "哈马斯称加沙下阶段停火谈判仍在进行 以方尚未做出承诺",
                "土耳其、芬兰、瑞典与北约代表将继续就瑞典“入约”问题进行谈判",
                "日本航空基地水井中检测出有机氟化物超标",
                "国家游泳中心（水立方）：恢复游泳、嬉水乐园等水上项目运营",
                "我国首次在空间站开展舱外辐射生物学暴露实验",
        };
        // 2.向量化
        // 2.1.先将查询文本向量化
        float[] queryVector = embeddingModel.embed(query);

        // 2.2.再将比较文本向量化，放到一个数组
        List<float[]> textVectors = embeddingModel.embed(Arrays.asList(texts));

        // 3.比较欧氏距离
        // 3.1.把查询文本自己与自己比较，肯定是相似度最高的
        System.out.println(VectorDistanceUtils.euclideanDistance(queryVector, queryVector));
        // 3.2.把查询文本与其它文本比较
        for (float[] textVector : textVectors) {
            System.out.println(VectorDistanceUtils.euclideanDistance(queryVector, textVector));
        }
        System.out.println("------------------");

        // 4.比较余弦距离
        // 4.1.把查询文本自己与自己比较，肯定是相似度最高的
        System.out.println(VectorDistanceUtils.cosineDistance(queryVector, queryVector));
        // 4.2.把查询文本与其它文本比较
        for (float[] textVector : textVectors) {
            System.out.println(VectorDistanceUtils.cosineDistance(queryVector, textVector));
        }
    }

//    @Test
//    void testRedisConnection() {
//        // 如果 redisTemplate 没注入成功，说明依赖或自动配置有问题
//        assertThat(redisTemplate).as("RedisTemplate should be auto-configured").isNotNull();
//
//        // 最简单的连通测试：set 一个临时 key，再 get 回来
//        String testKey = "test:connection";
//        String testValue = "OK";
//
//        redisTemplate.opsForValue().set(testKey, testValue);
//        String result = redisTemplate.opsForValue().get(testKey);
//
//        assertThat(result).isEqualTo(testValue);
//        System.out.println("✅ Redis connection successful! Value read back: " + result);
//    }

    @Test
    public void testVectorStore(){
        //配置资源路径，FileSystem是本地读取
        Resource resource = new FileSystemResource("国家教育.pdf");
        // 1.创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource, //
                // 文件源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1) // 每1页PDF作为一个Document
                        .build()
        );

//        读取，转化为Document
        List<Document> docs = reader.read();

//        写入向量库：
        vectorStore.add(docs);

        SearchRequest searchRequest = SearchRequest.builder()
                .query("国家安全宗旨是什么")
                .topK(3)
                .similarityThreshold(0.5)
                .build();

//      查询：
        List<Document> results = vectorStore.similaritySearch(searchRequest);

        if (results == null){
            System.out.println("没有对应内容");
        }else {
            for (Document result : results) {
                result.getId();
                result.getText();
                result.getScore();
            }

        }

    }

}
