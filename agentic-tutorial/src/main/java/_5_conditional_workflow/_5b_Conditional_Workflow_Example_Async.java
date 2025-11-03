package _5_conditional_workflow;

import _4_parallel_workflow.ManagerCvReviewer;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _5b_Conditional_Workflow_Example_Async {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 150);
    }

    /**
     * 此示例演示了多个满足条件和异步代理，它们将：
     * 允许连续的代理并行调用，从而加快执行速度。
     * 在此示例中：
     * - 条件1：若人事审核结果良好，则简历将传递给经理审核；
     * - 条件2：若人事审核显示信息不全，则联系候选人获取更多信息。
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. 创建所有异步代理
        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .async(true) // 异步代理
                .outputName("managerReview")
                .build();
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .async(true)
                .tools(new OrganizingTools())
                .outputName("sentEmailId")
                .build();
        InfoRequester infoRequester = AgenticServices.agentBuilder(InfoRequester.class)
                .chatModel(CHAT_MODEL)
                .async(true)
                .tools(new OrganizingTools())
                .outputName("sentEmailId")
                .build();

        // 2. 构建异步条件工作流
        UntypedAgent candidateResponder = AgenticServices
                .conditionalBuilder()
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score >= 0.8; // 若人力资源部门审核通过，请发送给经理进行审核。
                }, managerCvReviewer)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score < 0.8; // 若人力资源部门未通过审核，发送拒绝邮件
                }, emailAssistant)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.feedback.toLowerCase().contains("missing information:");
                }, infoRequester) // 如有需要，可向候选人索取更多信息
                .output(agenticScope ->
                        (agenticScope.readState("managerReview", new CvReview(0, "无需经理审核"))).toString() +
                                "\n" + agenticScope.readState("sentEmailId", 0)
                ) // 最终输出是经理审核（如有）。
                .build();

        // 3. 输入参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview hrReview = new CvReview(
                0.85,
                """
                        优秀候选人，薪资预期合理，且能在期望时间内入职。
                        缺失信息：在比利时的工作许可状态详情。
                        """
        );

        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", hrReview
        );


        // 4. 运行条件异步工作流
        candidateResponder.invoke(arguments);

        System.out.println("=== 异步条件工作流执行完成 ===");
    }
}
