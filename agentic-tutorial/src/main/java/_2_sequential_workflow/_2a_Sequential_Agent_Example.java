package _2_sequential_workflow;

import _1_basic_agent.CvGenerator;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _2a_Sequential_Agent_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // 控制模型调用中显示的内容量
    }

    /**
     * 此示例演示如何实现两个智能体：
     * - CvGenerator（接收个人经历并生成完整主简历）
     * - CvTailor（接收主简历并根据特定要求（职位描述、反馈等）进行定制）
     * 随后我们将通过固定工作流依次调用它们：
     * 使用序列构建器，并演示如何在它们之间传递参数。
     * 组合多个智能体时，所有输入、中间及输出参数与调用链均存储于智能体作用域中，可供高级用例调用。
     */

    // 1. 定义将驱动智能体的模型
    private static final ChatModel CHAT_MODEL = OpenAiChatModel.builder()
            .apiKey(System.getenv("DASHSCOPE_API_KEY"))
            .modelName("qwen-flash")
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
            .logRequests(true)
            .logResponses(true)
            .build();

    public static void main(String[] args) throws IOException {

        // 2. 定义两个子代理：
        //      - CvGenerator.java
        //      - CvTailor.java

        // 3. 使用代理服务创建两个代理
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputName("masterCv") // 如果你想将这个变量从代理1传递给代理2，
                // 然后确保此处的输出名称与输入变量名称匹配
                // 在第二个代理接口 agent_interfaces/CvTailor.java 中指定
                .build();
        CvTailor cvTailor = AgenticServices
                .agentBuilder(CvTailor.class)
                .chatModel(CHAT_MODEL) // 请注意，也可以为不同的代理使用不同的模型。
                .outputName("tailoredCv") // 我们需要定义输出对象的名称
                // 如果在此处放置"masterCv"，原始主CV文件将被覆盖。
                // 由第二个代理执行。虽然在此情况下我们并不需要此功能，但它本身具有实用价值。
                .build();

        ////////////////// UNTYPED 示例 //////////////////////

        // 4. 构建顺序工作流
        UntypedAgent tailoredCvGenerator = AgenticServices // 除非你定义生成的组合代理，否则请使用无类型代理，详见下文。
                .sequenceBuilder()
                .subAgents(cvGenerator, cvTailor) // 数量不限，顺序重要
                .outputName("tailoredCv") // 这是组合式智能体的最终输出结果
                // 请注意，您可以将属于代理作用域的任何字段作为输出使用。
                // 例如，你可以输出'masterCv'而不是tailoredCv（即使在此情况下这毫无意义）
                .build();

        // 4. 从resources/documents/文件中加载参数
        // - user_life_story.txt
        // - job_description_backend.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String instructions = "根据以下职位描述调整简历。" + StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. 由于我们使用的是无类型代理，因此需要传递一个参数映射表。
        Map<String, Object> arguments = Map.of(
                "lifeStory", lifeStory, // 匹配 agent_interfaces/CvGenerator.java 中的变量名
                "instructions", instructions // 匹配 agent_interfaces/CvTailor.java 中的变量名
        );

        // 5. 调用组合式代理生成定制简历
        String tailoredCv = (String) tailoredCvGenerator.invoke(arguments);

        // 6. 并打印生成的简历
        System.out.println("=== TAILORED CV UNTYPED ===");
        System.out.println((String) tailoredCv);
        // 当你使用 job_description_fullstack.txt 作为输入时，你可以观察到简历看起来截然不同

        // 在示例2b中，我们将构建相同的顺序代理，但采用类型化输出。
        // 我们将检查代理作用范围

    }
}