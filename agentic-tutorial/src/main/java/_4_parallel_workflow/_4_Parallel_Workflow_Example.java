package _4_parallel_workflow;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

public class _4_Parallel_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // 控制模型调用中显示的内容量
    }

    /**
     * 此示例演示如何实现3个并行CvReviewer代理，它们将：
     * 同时评估简历。我们将实现三个代理：
     * - ManagerCvReviewer（评估候选人胜任岗位的潜力）
     * 输入：简历和职位描述
     * - TeamMemberCvReviewer（评估候选人融入团队的适配度）
     * 输入：简历
     * - HrCvReviewer（从人力资源角度核查候选人资质）
     * 输入：简历、人力资源要求
     */

    // 1. 定义驱动agent的模型
    private static final ChatModel CHAT_MODEL = OpenAiChatModel.builder()
            .apiKey(System.getenv("DASHSCOPE_API_KEY"))
            .modelName("qwen-flash")
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
            .logRequests(true)
            .logResponses(true)
            .build();

    public static void main(String[] args) throws IOException {

        // 2. 定义三个子agent
        //      - HrCvReviewer.java
        //      - ManagerCvReviewer.java
        //      - TeamMemberCvReviewer.java

        // 3. 使用AgenticServices创建所有代理
        HrCvReviewer hrCvReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("hrReview") // 每次迭代都会覆盖此值，同时它也将作为我们想要观察的最终输出结果。
                .build();

        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("managerReview") // 这将覆盖原始输入指令，并在每次迭代中被覆盖，作为CvTailor的新指令使用。
                .build();

        TeamMemberCvReviewer teamMemberCvReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("teamMemberReview") // 这将覆盖原始输入指令，并在每次迭代中被覆盖，作为CvTailor的新指令使用。
                .build();

        // 4.构建并行工作流使用到的线程池
        var executor = Executors.newFixedThreadPool(3);  // 保留一个引用以便稍后关闭

        UntypedAgent cvReviewGenerator = AgenticServices // 除非你定义生成的组合代理，否则请使用无类型代理，详见_2_顺序代理示例
                .parallelBuilder()
                .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer) // 数量可以随心所欲
                .executor(executor) // 可选，默认使用内部缓存线程池，该线程池将在执行完成后自动关闭。
                .outputName("fullCvReview") // 这是我们想要观察的最终输出结果
                .output(agenticScope -> {
                    // 从agentic scope中读取每位评审者的输出结果
                    CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
                    CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
                    // 返回一个打包的评论，包含平均评分（或任何其他你想要的聚合数据）
                    String feedback = String.join("\n",
                            "HR Review: " + hrReview.feedback,
                            "Manager Review: " + managerReview.feedback,
                            "Team Member Review: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;

                    return new CvReview(avgScore, feedback);
                })
                .build();

        // 5.从 resources/documents/ 加载原始参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        // 6. 由于我们使用的是无类型代理，因此需要传递一个参数映射表。
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "jobDescription", jobDescription
                , "hrRequirements", hrRequirements
                , "phoneInterviewNotes", phoneInterviewNotes
        );

        // 7. 调用组合式代理生成定制简历
        var review = cvReviewGenerator.invoke(arguments);

        // 8. 并打印生成的简历
        System.out.println("=== 简历反馈结果 ===");
        System.out.println(review);

        // 9. 关闭线程池
        executor.shutdown();
    }
}