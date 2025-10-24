
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiModerationModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Moderate;
import dev.langchain4j.service.ModerationException;

public class ServiceWithAutoModerationExample {

    interface Chat {

        @Moderate
        String chat(String text);
    }

    public static void main(String[] args) {

        ModerationModel moderationModel = OpenAiModerationModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-max")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen2-0.5b-instruct")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        Chat chat = AiServices.builder(Chat.class)
                .chatModel(chatModel)
                .moderationModel(moderationModel)
                .build();

        try {
            System.out.println(chat.chat("我会杀了你！！！"));
        } catch (ModerationException e) {
            System.out.println(e.getMessage());
            // Text "I WILL KILL YOU!!!" violates content policy
        }
    }
}
