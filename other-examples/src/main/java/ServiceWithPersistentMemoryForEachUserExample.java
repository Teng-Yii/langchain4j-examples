import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static org.mapdb.Serializer.INTEGER;
import static org.mapdb.Serializer.STRING;

public class ServiceWithPersistentMemoryForEachUserExample {

    interface Assistant {

        String chat(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    public static void main(String[] args) {

        PersistentChatMemoryStore store = new PersistentChatMemoryStore();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-flash")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .build();

//        System.out.println(assistant.chat(1, "你好，我的名字是一腾"));    // 你好，一腾！很高兴认识你～✨ 你的名字听起来很有力量感呢，是不是有什么特别的寓意呀？或者你希望别人怎么称呼你呢？(•̀ᴗ•́)و
//        System.out.println(assistant.chat(2, "嗨，我的名字是bob"));      // 嗨，Bob！很高兴认识你 😊 有什么我可以帮你的吗？

        // 现在，将上面的两行代码注释掉，取消下面两行的注释，然后再次运行。

        // 哈哈，我刚刚才认识你呀～你的名字是“一腾”哦！✨
        // “一”代表独一无二的开始，“腾”有腾飞、向上的意思，听起来就像一只展翅高飞的小鸟呢～是不是很有气势？🐦💨
        //（悄悄说：这个名字让我想到“一鸣惊人”的那种冲劲，感觉你一定是个充满能量的人！）
        System.out.println(assistant.chat(1, "我的名字是什么？"));
        // 你的名字是 Bob 哦！😊 我记得很清楚，你刚才告诉我的。有什么关于 Bob 的故事或者想聊的话题吗？
        System.out.println(assistant.chat(2, "我的名字是什么？"));
    }

    // 你可以创建自己的ChatMemoryStore实现，并在需要时存储聊天记忆。
    static class PersistentChatMemoryStore implements ChatMemoryStore {

        private final DB db = DBMaker.fileDB("multi-user-chat-memory.db").transactionEnable().make();
        private final Map<Integer, String> map = db.hashMap("messages", INTEGER, STRING).createOrOpen();

        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
            String json = map.get((int) memoryId);
            return messagesFromJson(json);
        }

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            String json = messagesToJson(messages);
            map.put((int) memoryId, json);
            db.commit();
        }

        @Override
        public void deleteMessages(Object memoryId) {
            map.remove((int) memoryId);
            db.commit();
        }
    }
}