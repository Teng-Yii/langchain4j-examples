package dev.langchain4j.example.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, streamingChatModel = "qwen2.5-7b-instruct-1m")
public interface StreamingAssistant {

    @SystemMessage("你是一个礼貌的助手")
    Flux<String> chat(String userMessage);
}