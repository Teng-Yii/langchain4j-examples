import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;

import java.util.List;
import java.util.Map;


/**
 * 通过 ChatModelListener 实现观察者模式监听AI聊天模型的整个交互过程
 */
public class OpenAiObservabilityExamples {

    static class Observe_OpenAiChatModel {

        public static void main(String[] args) {

            ChatModelListener listener = new ChatModelListener() {

                // 请求发送时的回调
                @Override
                public void onRequest(ChatModelRequestContext requestContext) {
                    // 获取聊天请求信息
                    ChatRequest chatRequest = requestContext.chatRequest();

                    List<ChatMessage> messages = chatRequest.messages();
                    System.out.println(messages);   // [UserMessage { name = null contents = [TextContent { text = "给我讲一个关于java的笑话" }] }]

                    // 获取请求参数
                    ChatRequestParameters parameters = chatRequest.parameters();
                    System.out.println(parameters.modelName());     // qwen-flash
                    System.out.println(parameters.temperature());
                    System.out.println(parameters.topP());
                    System.out.println(parameters.topK());
                    System.out.println(parameters.frequencyPenalty());
                    System.out.println(parameters.presencePenalty());
                    System.out.println(parameters.maxOutputTokens());
                    System.out.println(parameters.stopSequences());
                    System.out.println(parameters.toolSpecifications());
                    System.out.println(parameters.toolChoice());
                    System.out.println(parameters.responseFormat());

                    if (parameters instanceof OpenAiChatRequestParameters openAiParameters) {
                        System.out.println(openAiParameters.maxCompletionTokens());
                        System.out.println(openAiParameters.logitBias());
                        System.out.println(openAiParameters.parallelToolCalls());
                        System.out.println(openAiParameters.seed());
                        System.out.println(openAiParameters.user());
                        System.out.println(openAiParameters.store());
                        System.out.println(openAiParameters.metadata());
                        System.out.println(openAiParameters.serviceTier());
                        System.out.println(openAiParameters.reasoningEffort());
                    }

                    System.out.println(requestContext.modelProvider());     // OPEN_AI

                    // 可以实现自定义添加属性
                    Map<Object, Object> attributes = requestContext.attributes();
                    attributes.put("my-attribute", "my-value");
                }

                // 收到响应时的回调
                @Override
                public void onResponse(ChatModelResponseContext responseContext) {
                    // 获取AI回复消息
                    ChatResponse chatResponse = responseContext.chatResponse();

                    AiMessage aiMessage = chatResponse.aiMessage();
                    System.out.println(aiMessage);

                    // 获取响应元数据
                    ChatResponseMetadata metadata = chatResponse.metadata();
                    System.out.println(metadata.id());
                    System.out.println(metadata.modelName());       // qwen-flash
                    System.out.println(metadata.finishReason());    // STOP

                    if (metadata instanceof OpenAiChatResponseMetadata openAiMetadata) {
                        System.out.println(openAiMetadata.created());
                        System.out.println(openAiMetadata.serviceTier());
                        System.out.println(openAiMetadata.systemFingerprint());
                    }

                    // 获取Token使用情况
                    TokenUsage tokenUsage = metadata.tokenUsage();
                    System.out.println(tokenUsage.inputTokenCount());
                    System.out.println(tokenUsage.outputTokenCount());
                    System.out.println(tokenUsage.totalTokenCount());
                    if (tokenUsage instanceof OpenAiTokenUsage openAiTokenUsage) {
                        System.out.println(openAiTokenUsage.inputTokensDetails().cachedTokens());
//                        System.out.println(openAiTokenUsage.outputTokensDetails().reasoningTokens());   // 空指针
                    }

                    ChatRequest chatRequest = responseContext.chatRequest();
                    System.out.println(chatRequest);    // ChatRequest { messages = [UserMessage { name = null contents = [TextContent { text = "给我讲一个关于java的笑话" }] }], parameters = OpenAiChatRequestParameters{modelName="qwen-flash", temperature=null, topP=null, topK=null, frequencyPenalty=null, presencePenalty=null, maxOutputTokens=null, stopSequences=[], toolSpecifications=[], toolChoice=null, responseFormat=null, maxCompletionTokens=null, logitBias={}, parallelToolCalls=null, seed=null, user=null, store=null, metadata={}, serviceTier=null, reasoningEffort=null, customParameters={}} }

                    System.out.println(responseContext.modelProvider());    // OPEN_AI

                    // 获取之前设置的属性
                    Map<Object, Object> attributes = responseContext.attributes();
                    System.out.println(attributes.get("my-attribute"));     // my-value
                }

                // 发生错误时的回调
                @Override
                public void onError(ChatModelErrorContext errorContext) {
                    // 获取错误信息
                    Throwable error = errorContext.error();
                    error.printStackTrace();

                    // 获取导致错误的请求
                    ChatRequest chatRequest = errorContext.chatRequest();
                    System.out.println(chatRequest);

                    System.out.println(errorContext.modelProvider());

                    // 获取之前设置的属性
                    Map<Object, Object> attributes = errorContext.attributes();
                    System.out.println(attributes.get("my-attribute"));
                }
            };

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName("qwen-flash")
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    // 监听当前模型的请求和响应
                    .listeners(List.of(listener))
                    .build();

            model.chat("给我讲一个关于java的笑话");
        }
    }
}
