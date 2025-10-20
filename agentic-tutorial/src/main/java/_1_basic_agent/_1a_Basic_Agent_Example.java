package _1_basic_agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;

public class _1a_Basic_Agent_Example {

    /**
     *本示例演示如何实现基础智能体以展示语法
     *请注意，智能体仅在与其他智能体组合使用时才具实用价值，后续步骤将展示相关应用。
     *若仅需单一智能体，建议采用AiService服务。
     *
     *此基础智能体可将用户的人生经历转化为简洁完整的简历。
     *运行此程序可能需要较长时间，因为生成的简历篇幅较长，模型处理需耗费一定时间。
     */

    // 设置日志级别
    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    public static void main(String[] args) throws IOException {

        // 1. 定义为agent提供能力的模型
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-flash")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .logRequests(true)
                .logResponses(true)
                .build();

        // 2. 在agent_interfaces/CvGenerator.java中定义智能体行为

        // 3. 使用代理服务创建代理
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(chatModel)
                .outputName("masterCv") // 可选地定义输出对象的名称
                .build();

        // 4. 从resources/documents/user_life_story.txt加载文本文件
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");

        // 5. 我们调用生成简历的代理程序
        String cv = cvGenerator.generateCv(lifeStory);

        // 6. 输出生成简历的内容
        System.out.println("=== CV ===");
        System.out.println(cv);

        // 在示例1b中，我们将构建相同的智能体，但采用结构化输出形式。

    }
}