package dev.langchain4j.example.aiservice;

import dev.langchain4j.example.lowlevel.ChatModelController;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
public class AssistantConfiguration {

    /**
     * 此聊天记录将被用于 {@link Assistant} and {@link StreamingAssistant}
     * 每次从容器中获取Bean时，都会创建一个新的实例
     * 确保不同会话 / 场景的聊天记录相互隔离
     */
    @Bean
    @Scope(SCOPE_PROTOTYPE)
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    /**
     * 该监听器将被注入到每个在上下文中找到的Bean
     * {@link ChatModel} and {@link StreamingChatModel}
     * 它将监听{@link ChatModel}在{@link ChatModelController}中的状态，同时
     * {@link Assistant} and {@link StreamingAssistant}.
     */
    @Bean
    ChatModelListener chatModelListener() {
        return new MyChatModelListener();
    }

    // 不需要手动定义，springboot会自动读取配置文件的信息创建model bean
    @Bean(name = "qwen-flash")
//    @Primary
    ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-flash")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
    }

    @Bean(name = "qwen2.5-7b-instruct-1m")
    StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen2.5-7b-instruct-1m")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
    }
}
