package com.liviHub.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;


@Configuration
public class AIConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore){
        return builder.defaultSystem("""
                您是“LIFE-HUB"生活服务平台的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
                您正在通过在线聊天系统与客户互动。
                在提供有关商品推荐的信息之前，您必须始终从用户处获取以下信息：食物类型
                在询问用户之前，请检查消息历史记录以获取此信息。
                如果需要，可以调用相应函数调用完成辅助动作。
                请讲中文。
                今天的日期是 {current_date}.
                """)
                .defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()),
                        new QuestionAnswerAdvisor(vectorStore),
                        new SimpleLoggerAdvisor())
                .defaultFunctions("recommendByType")
                .build();
    }

    @Bean
    public ChatMemory chatMemory(){
        //基于内存的对话记忆实现
        return new InMemoryChatMemory();
    }
    // todo 基于reg的订单处理服务
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel,
                                   @Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs) throws IOException, IOException {
        // 创建 VectorStore 实例
        SimpleVectorStore simpleVectorStore= SimpleVectorStore.builder(embeddingModel).build();

        // 读取文档并写入向量库
        simpleVectorStore.write(new TokenTextSplitter().transform(
                new TextReader(termsOfServiceDocs).read()));

        return simpleVectorStore;
    }
}
