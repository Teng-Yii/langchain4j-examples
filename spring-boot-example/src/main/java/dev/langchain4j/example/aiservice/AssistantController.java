package dev.langchain4j.example.aiservice;

import dev.langchain4j.service.spring.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * This is an example of using an {@link AiService}, a high-level LangChain4j API.
 */
@RestController
public class AssistantController {

    private final Assistant assistant;
    private final StreamingAssistant streamingAssistant;

    public AssistantController(Assistant assistant, StreamingAssistant streamingAssistant) {
        this.assistant = assistant;
        this.streamingAssistant = streamingAssistant;
    }

    // http://localhost:8082/assistant?message=现在几点了
    @GetMapping("/assistant")
    public String assistant(@RequestParam(value = "message", defaultValue = "现在几点了？") String message) {
        return assistant.chat(message);
    }

    /**
     * 流式输出，指定响应的内容类型"text/event-stream"，SSE的标准MIME类型
     * http://localhost:8082/streamingAssistant?message=现在几点了
     * @param message
     * @return
     */
    @GetMapping(value = "/streamingAssistant", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(
            @RequestParam(value = "message", defaultValue = "现在几点了？") String message) {
        return streamingAssistant.chat(message);
    }
}
