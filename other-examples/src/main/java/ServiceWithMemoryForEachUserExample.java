import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class ServiceWithMemoryForEachUserExample {

    /**
     * See also {@link ServiceWithPersistentMemoryForEachUserExample}.
     */

    interface Assistant {

        String chat(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    public static void main(String[] args) {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-flash")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        System.out.println(assistant.chat(1, "你好，我的名字是一腾"));
        // 你好，一腾！很高兴认识你 😊
        // 希望我们能有愉快的交流～ 有什么想聊的或需要帮助的，尽管告诉我哦！✨

        System.out.println(assistant.chat(2, "你好，我的名字是bob"));
        // 你好，Bob！很高兴认识你 😊
        // 有什么我可以帮你的吗？

        System.out.println(assistant.chat(1, "我的名字是什么？"));
        // 你的名字是——一腾！✨
        //（我可记得清清楚楚呢，不会忘记这么特别的名字～）

        System.out.println(assistant.chat(2, "我的名字是什么？"));
        // 你的名字是 Bob！😊
        // 我刚刚记得你说过，所以现在也记住了～
        // 有什么想聊的或需要帮忙的吗？
    }
}