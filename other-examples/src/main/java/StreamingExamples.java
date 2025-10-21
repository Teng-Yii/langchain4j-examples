import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.LambdaStreamingResponseHandler;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.*;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static java.util.Arrays.asList;

public class StreamingExamples {

    static class StreamingChatModel_Example {

        public static void main(String[] args) {

            StreamingChatModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName("qwen-flash")
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    .build();

            List<ChatMessage> messages = asList(
                    systemMessage("你是个非常刻薄的助理"),
                    userMessage("给我讲一个笑话")
            );

            CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

            // 通过lambda表达式创建 StreamingChatResponseHandler
            model.chat("Tell me a joke", LambdaStreamingResponseHandler.onPartialResponse(System.out::print));

            // 同时为 onPartialResponse() 和 onError() 事件定义操作
            model.chat("Tell me a joke", LambdaStreamingResponseHandler.onPartialResponseAndError(System.out::print, Throwable::printStackTrace));

            model.chat(messages, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    System.out.println("onPartialResponse: " + partialResponse);
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking) {
                    System.out.println("onPartialThinking: " + partialThinking);
                }

                @Override
                public void onPartialToolCall(PartialToolCall partialToolCall) {
                    System.out.println("onPartialToolCall: " + partialToolCall);
                }

                @Override
                public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                    System.out.println("onCompleteToolCall: " + completeToolCall);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    System.out.println("onCompleteResponse: " + completeResponse);
                    // 标记异步操作完成，否则一直阻塞主线程
                    futureChatResponse.complete(completeResponse);
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                    // 设置异常结果
                    futureChatResponse.completeExceptionally(error);
                }
            });

            // 避免主程序在流式响应完成前退出
            futureChatResponse.join();
        }
    }

    static class StreamingLanguageModel_Example {

        public static void main(String[] args) {

            StreamingLanguageModel model = OpenAiStreamingLanguageModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName("qwen-flash")
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    .build();

            CompletableFuture<Response<String>> futureResponse = new CompletableFuture<>();

            model.generate("给我讲一个笑话", new StreamingResponseHandler<>() {

                @Override
                public void onNext(String token) {
                    System.out.println("token: " + token);
                }

                @Override
                public void onComplete(Response<String> response) {
                    futureResponse.complete(response);
                }

                @Override
                public void onError(Throwable error) {
                    futureResponse.completeExceptionally(error);
                }
            });

            futureResponse.join();
        }
    }
}
