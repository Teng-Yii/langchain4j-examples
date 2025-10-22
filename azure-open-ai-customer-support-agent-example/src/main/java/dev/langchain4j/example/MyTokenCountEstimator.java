package dev.langchain4j.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.TokenCountEstimator;

import java.util.Map;

import static com.knuddels.jtokkit.api.EncodingType.O200K_BASE;
import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.Utils.isNullOrBlank;

public class MyTokenCountEstimator implements TokenCountEstimator {
    private static final EncodingRegistry ENCODING_REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Encoding encoding = ENCODING_REGISTRY.getEncoding(O200K_BASE);

    @Override
    public int estimateTokenCountInText(String text) {
        return encoding.countTokensOrdinary(text);
    }

    @Override
    public int estimateTokenCountInMessage(ChatMessage message) {
        int tokenCount = 1; // 1 token for role
        tokenCount += 3; // extra tokens per each message

        if (message instanceof SystemMessage) {
            tokenCount += estimateTokenCountIn((SystemMessage) message);
        } else if (message instanceof UserMessage) {
            tokenCount += estimateTokenCountIn((UserMessage) message);
        } else if (message instanceof AiMessage) {
            tokenCount += estimateTokenCountIn((AiMessage) message);
        } else if (message instanceof ToolExecutionResultMessage) {
            tokenCount += estimateTokenCountIn((ToolExecutionResultMessage) message);
        } else {
            throw new IllegalArgumentException("Unknown message type: " + message);
        }

        return tokenCount;
    }

    @Override
    public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
        return 0;
    }

    private int estimateTokenCountIn(SystemMessage systemMessage) {
        return estimateTokenCountInText(systemMessage.text());
    }

    private int estimateTokenCountIn(UserMessage userMessage) {
        int tokenCount = 0;

        for (Content content : userMessage.contents()) {
            if (content instanceof TextContent) {
                tokenCount += estimateTokenCountInText(((TextContent) content).text());
            } else {
                throw illegalArgument("Unknown content type: " + content);
            }
        }

        if (userMessage.name() != null) {
            tokenCount += 1; // extra tokens per name
            tokenCount += estimateTokenCountInText(userMessage.name());
        }

        return tokenCount;
    }

    private int estimateTokenCountIn(AiMessage aiMessage) {
        int tokenCount = 0;

        if (aiMessage.text() != null) {
            tokenCount += estimateTokenCountInText(aiMessage.text());
        }

        if (aiMessage.hasToolExecutionRequests()) {
            tokenCount += 6;
            if (aiMessage.toolExecutionRequests().size() == 1) {
                tokenCount -= 1;
                ToolExecutionRequest toolExecutionRequest = aiMessage.toolExecutionRequests().get(0);
                tokenCount += estimateTokenCountInText(toolExecutionRequest.name()) * 2;
                tokenCount += estimateTokenCountInText(toolExecutionRequest.arguments());
            } else {
                tokenCount += 15;
                for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                    tokenCount += 7;
                    tokenCount += estimateTokenCountInText(toolExecutionRequest.name());

                    if (isNullOrBlank(toolExecutionRequest.arguments())) {
                        continue;
                    }

                    try {
                        Map<?, ?> arguments = OBJECT_MAPPER.readValue(toolExecutionRequest.arguments(), Map.class);
                        for (Map.Entry<?, ?> argument : arguments.entrySet()) {
                            tokenCount += 2;
                            tokenCount += estimateTokenCountInText(String.valueOf(argument.getKey()));
                            tokenCount += estimateTokenCountInText(String.valueOf(argument.getValue()));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return tokenCount;
    }

    private int estimateTokenCountIn(ToolExecutionResultMessage toolExecutionResultMessage) {
        return estimateTokenCountInText(toolExecutionResultMessage.text());
    }
}

