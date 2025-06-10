package com.liviHub.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController("/ai")
public class AIController {

    @Autowired
    private ChatClient chatClient;

    @GetMapping(value="/generateStreamAsString",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "你是谁") String message) {
        Flux<String> response=chatClient.prompt()
                .user(message)
                .system(promptSystemSpec -> promptSystemSpec.param("current_date", LocalDate.now().toString()))
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY,100))
                .stream()
                .content();
        return response.concatWith(Flux.just("[complete]"));
    }
}
