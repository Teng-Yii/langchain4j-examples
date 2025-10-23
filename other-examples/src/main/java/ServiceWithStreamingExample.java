import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServiceWithStreamingExample {

    interface Assistant {

        TokenStream chat(String message);
    }

    public static void main(String[] args) throws Exception {

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-flash")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        Assistant assistant = AiServices.create(Assistant.class, model);

        TokenStream tokenStream = assistant.chat("给我讲一个笑话");

        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        tokenStream.onPartialResponse(System.out::print)
                .onCompleteResponse(futureResponse::complete)
                .onError(futureResponse::completeExceptionally)
                .start();

        ChatResponse chatResponse = futureResponse.get(30, SECONDS);
        System.out.println("\n" + chatResponse);
    }
}
