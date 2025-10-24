import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.GuardrailException;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.guardrail.InputGuardrails;

import java.util.List;

public class ServiceWithInputGuardrailExample {

    interface Chat {

        @InputGuardrails({MySecurityGuard.class})
        String chat(String text);
    }

    public static void main(String[] args) {

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen2-0.5b-instruct")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        Chat chat = AiServices.builder(Chat.class)
                .chatModel(chatModel)
                .build();

        try {
            System.out.println(chat.chat("我会杀了你！！！"));
        } catch (GuardrailException e) {
            // The guardrail ServiceWithInputGuardrailExample$MySecurityGuard failed with this message: 我会杀了你！！！输入不合法
            System.out.println(e.getMessage());
        }
    }

    public static class MySecurityGuard implements InputGuardrail {

        @Override
        public InputGuardrailResult validate(InputGuardrailRequest request) {
            UserMessage userMessage = request.userMessage();
            List<Content> contents = userMessage.contents();
            TextContent content = (TextContent) contents.get(0);
            if (content.text().contains("杀")) {
                return failure(content.text() + "输入不合法");
            }
            return InputGuardrailResult.success();
        }
    }
}
