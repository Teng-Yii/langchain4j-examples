import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static org.mapdb.Serializer.STRING;

public class ServiceWithPersistentMemoryExample {

    /**
     * See also {@link ServiceWithMemoryExample} and {@link ServiceWithPersistentMemoryForEachUserExample}.
     */

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryStore(new PersistentChatMemoryStore())
                .build();

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-flash")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();

//        String answer = assistant.chat("你好，我的名字是一腾");
//        System.out.println(answer); // 你好，一腾！很高兴认识你～ 🌟 今天有什么想聊的吗？或者需要我帮忙做点什么？（*^▽^*）

        // 现在，将上面两行代码注释掉，取消下面两行代码的注释，然后再次运行。

         String answerWithName = assistant.chat("我的名字是什么？");
         System.out.println(answerWithName); // 你的名字是——一腾！🌟（轻轻眨眨眼）记得你刚才亲口告诉我的哦，像一颗小星星一样闪亮的名字呢～✨ 有什么特别的故事吗？比如为什么叫“一腾”呀？(•̀ᴗ•́)و
    }

    // 你可以创建自己的ChatMemoryStore实现，并在需要时存储聊天记忆。
    static class PersistentChatMemoryStore implements ChatMemoryStore {

        // 创建一个文件型数据库，启用事务支持
        private final DB db = DBMaker.fileDB("chat-memory.db").transactionEnable().make();
        private final Map<String, String> map = db.hashMap("messages", STRING, STRING).createOrOpen();

        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
            String json = map.get((String) memoryId);
            return ChatMessageDeserializer.messagesFromJson(json);
        }

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            String json = messagesToJson(messages);
            map.put((String) memoryId, json);
            db.commit();
        }

        @Override
        public void deleteMessages(Object memoryId) {
            map.remove((String) memoryId);
            db.commit();
        }
    }
}
