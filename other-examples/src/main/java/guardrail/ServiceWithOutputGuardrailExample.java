package guardrail;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.guardrail.OutputGuardrails;


public class ServiceWithOutputGuardrailExample {

    interface Chat {

        @OutputGuardrails(value = {MySecurityGuard.class}, maxRetries = 3)
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
            String result = chat.chat("给我讲一个笑话，要求字数在5字以内");
            System.out.println(result);
        } catch (GuardrailException e) {
            // The guardrail guardrail.ServiceWithOutputGuardrailExample$MySecurityGuard failed with this message: 输出文本不能大于20
            System.out.println(e.getMessage());
        }
    }

    public static class MySecurityGuard implements OutputGuardrail {

        /**
         * 证实了 LLM 的响应。
         *
         * @param responseFromLLM LLM的回复
         */
        @Override
        public OutputGuardrailResult validate(AiMessage responseFromLLM) {
            String responseText = responseFromLLM.text();
            if (responseText.length() > 5) {
                System.out.println("validate failure");

//                return failure("输出文本不能大于5");                                        // 这不会导致重试
//                return retry("输出文本不能大于5");                                          // retry方法会重试chatMemory的对话，如果第一轮对话校验失败，会抛IllegalArgumentException: messages cannot be null or empty异常
                return reprompt("输出文本不能大于5", "直接输出内容“你好”");   // reprompt允许重新输入userMessage作为prompt，根据新prompt重新对话，得到的响应判断是否通过检验
            }
            return success();

            // 谨慎使用，因为这会导致覆盖掉AI的回答内容！！！
            // return successWith("成功输出");
        }

        /**
         * 验证 LLM 的响应。
         * 与 validate(AiMessage) 不同，此方法允许访问内存和增强结果（在 RAG 的情况下）。
         * 实现不得尝试写入内存或增强结果。
         * 参数：
         *
         * @param request – 参数，包括 LLM 的响应、内存和增强结果。
         */
        @Override
        public OutputGuardrailResult validate(OutputGuardrailRequest request) {
            // 获取用户消息
            String userMessage = request.requestParams().userMessageTemplate();
            System.out.println(userMessage);
            return validate(request.responseFromLLM().aiMessage());
        }
    }
}
