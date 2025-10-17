package _9_human_in_the_loop;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import util.ChatModelProvider;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.util.Map;
import java.util.Scanner;

public class _9b_HumanInTheLoop_Chatbot_With_Memory {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    /**
     * 此示例演示了包含人机交互的往复循环，
     * 直到达到终止目标（退出条件）后，其余工作流才可继续执行。
     *该循环持续进行直至人工确认可用性，该状态由AiService进行验证。
     *若未找到可用时段，循环将在5次迭代后终止。
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {

        // 1. 定义子智能体
        MeetingProposer proposer = AgenticServices
                .agentBuilder(MeetingProposer.class)
                .chatModel(CHAT_MODEL)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15)) // 记住之前的记忆
                .outputName("proposal")
                .build();

        // 2. 添加一个AiService来判断是否已做出决策（由于任务非常简单，可以使用一个微型本地模型）
        DecisionsReachedService decisionService = AiServices.create(DecisionsReachedService.class, CHAT_MODEL);

        // 2. 定义人机交互智能体
        HumanInTheLoop humanInTheLoop = AgenticServices
                .humanInTheLoopBuilder()
                .description("向用户请求输入的agent")
                .outputName("candidateAnswer") // 输出用户回答
                .inputName("proposal") // 接收AI提议
                .requestWriter(request -> {
                    System.out.println(request);
                    System.out.print("> ");
                })
                .responseReader(() -> new Scanner(System.in).nextLine())
                .async(true) // 异步执行，不阻塞程序
                .build();

        // 3. 构建循环
        // 希望退出条件在每次循环中检查一次，而非每次代理调用后检查，
        // 因此我们使用序列模式将两个子agent打包并作为单个代理传递给循环。
        UntypedAgent agentSequence = AgenticServices
                .sequenceBuilder()
                .subAgents(proposer, humanInTheLoop)    // 按照顺序执行：提议+用户响应
                .output(agenticScope -> Map.of(
                        "proposal", agenticScope.readState("proposal"),
                        "candidateAnswer", agenticScope.readState("candidateAnswer")
                ))
                .outputName("proposalAndAnswer")
                // 此输出包含最后一次日期提案及候选人的答复，这些信息应足以供后续客服人员安排会议（或终止尝试）。
                .build();

        UntypedAgent schedulingLoop = AgenticServices
                .loopBuilder()  // 循环模式
                .subAgents(agentSequence)
                .exitCondition(scope -> {   // 退出条件
                    System.out.println("--- checking exit condition ---");
                    String response = (String) scope.readState("candidateAnswer");
                    String proposal = (String) scope.readState("proposal");
                    return response != null && decisionService.isDecisionReached(proposal, response);
                })
                .outputName("proposalAndAnswer")
                .maxIterations(5)
                .build();

        // 4. 执行循环模式的agent
        Map<String, Object> input = Map.of("meetingTopic", "on-site visit",     // 会议主题
                "candidateAnswer", "hi",                                            // 初始回答（需要预设）
                "memoryId", "user-1234");                                           // 用户id，用于记忆管理

        var lastProposalAndAnswer = schedulingLoop.invoke(input);

        System.out.println("=== Result: last proposalAndAnswer ===");
        System.out.println(lastProposalAndAnswer);
    }
}
