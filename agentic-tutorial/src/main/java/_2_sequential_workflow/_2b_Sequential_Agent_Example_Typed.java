package _2_sequential_workflow;

import _1_basic_agent.CvGenerator;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import util.AgenticScopePrinter;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _2b_Sequential_Agent_Example_Typed {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 150);  // // 控制模型调用中显示的内容量
    }

    /**
     * 我们将实现与2a相同的顺序工作流，但这次将：
     * - 为组合代理使用类型化接口（SequenceCvGenerator）
     * - 从而能够使用带参数的方法替代.invoke(argsMap)
     * - 以自定义方式收集输出结果
     * - 在调用后检索并检查AgenticScope，用于调试或测试目的
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

        // 2. 在此包中定义顺序代理接口：
        //      - SequenceCvGenerator.java
        // 具有方法签名：ResultWithAgenticScope<Map<String, String>> generateTailoredCv(@V("lifeStory") String lifeStory, @V("instructions") String instructions);

        // 3. 使用AgenticServices创建两个子代理，操作方式与之前相同
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


        // 4. 从resources/documents/文件中加载参数
        // （这次无需将它们放入Map中），我们采用类型化接口
        // - user_life_story.txt
        // - job_description_backend.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String instructions = "根据以下职位描述调整简历。" + StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. 构建带自定义输出处理的类型化序列
        SequenceCvGenerator sequenceCvGenerator = AgenticServices
                .sequenceBuilder(SequenceCvGenerator.class) // 在此我们指定了类型化接口
                .subAgents(cvGenerator, cvTailor)
                .outputName("bothCvsAndLifeStory")
                .output(agenticScope -> { // 任何方法都是可行的，但我们会收集一些内部变量。
                    Map<String, String> bothCvsAndLifeStory = Map.of(
                            "lifeStory", agenticScope.readState("lifeStory", ""),
                            "masterCv", agenticScope.readState("masterCv", ""),
                            "tailoredCv", agenticScope.readState("tailoredCv", "")
                    );
                    return bothCvsAndLifeStory;
                })
                .build();

        // 6. 调用类型化的组合代理
        ResultWithAgenticScope<Map<String, String>> bothCvsAndScope = sequenceCvGenerator.generateTailoredCv(lifeStory, instructions);

        // 7. 提取结果和代理作用范围
        AgenticScope agenticScope = bothCvsAndScope.agenticScope();
        Map<String, String> bothCvsAndLifeStory = bothCvsAndScope.result();

        System.out.println("=== 用户信息（输入） ===");
        String userStory = bothCvsAndLifeStory.get("lifeStory");
        System.out.println(userStory.length() > 100 ? userStory.substring(0, 100) + " [truncated...]" : lifeStory);
        System.out.println("=== MASTER CV TYPED (（中间变量）) ===");
        String masterCv = bothCvsAndLifeStory.get("masterCv");
        System.out.println(masterCv.length() > 100 ? masterCv.substring(0, 100) + " [truncated...]" : masterCv);
        System.out.println("=== TAILORED CV TYPED (输出) ===");
        String tailoredCv = bothCvsAndLifeStory.get("tailoredCv");
        System.out.println(tailoredCv.length() > 100 ? tailoredCv.substring(0, 100) + " [truncated...]" : tailoredCv);

        // 无论无类型还是有类型的代理，都给出相同的定制化简历结果。
        // （任何差异均源于大型语言模型的非确定性特性），
        // 但类型化代理在使用上更为优雅且更安全，因为它具备编译时类型检查功能。

        System.out.println("=== AGENTIC SCOPE ===");
        System.out.println(AgenticScopePrinter.printPretty(agenticScope, 100));
        // 这将返回此对象（已填充）：
        // AgenticScope {
        //  "memoryId": "15b6266e-a84f-45eb-bb4d-92f5878df8a7",
        //  "state": {
        //    "instructions": "根据以下职位描述调整简历。职位描述ID：123A\r\n后端工程师（金融科技，安特卫普）\r\n-----------------------------------\r\n我们正在寻找一名**后端工程师**，协 [truncated...]",
        //    "bothCvsAndLifeStory": "{masterCv=[John Doe]  \n📍 Antwerp, Belgium | 📧 john.doe.dev@protonmail.com | 📞 +32 495 67 89 23  \n [truncated...]",
        //    "tailoredCv": "以下是根据职位描述 **123A（后端工程师 – 金融科技，安特卫普）** 优化后的简历。重点突出与金融科技、支付/对账系统、云原生技术栈（PostgreSQL, Kafka, Docker, Kub [truncated...]",
        //    "masterCv": "[John Doe]  \n📍 Antwerp, Belgium | 📧 john.doe.dev@protonmail.com | 📞 +32 495 67 89 23  \n🔗 [Linked [truncated...]",
        //    "lifeStory": "约翰·多伊常驻比利时安特卫普（地址：安特卫普市卡尔梅斯街12号，邮编2000），联系方式为john.doe.dev@protonmail.com或+32 495 67 89 23。活跃于领英和GitH [truncated...]"
        //  }
        //}
        System.out.println("=== 上下文即对话（对话中的所有消息） ===");
        System.out.println(AgenticScopePrinter.printConversation(agenticScope.contextAsConversation(), 100));

    }
}