package _5_conditional_workflow;

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

public class _5a_Conditional_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 200);  //  控制模型调用中显示的内容量
    }

    /**
     * 此示例演示了条件式代理工作流。
     * 根据评分和候选人档案，我们将执行以下操作之一：
     * - 调用代理程序，为与候选人的现场面试做好全部准备
     * - 调用代理程序，发送一封礼貌的邮件告知候选人我们不会继续推进*
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. 定义两个子代理：
        //      - EmailAssistant.java
        //      - InterviewOrganizer.java

        // 3. 使用代理服务创建所有代理
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools()) // 代理可以使用那里定义的所有工具
                .build();
        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .contentRetriever(RagProvider.loadHouseRulesRetriever()) // 这就是我们如何将RAG添加到智能体的方法
                .build();

        // 4. 构建条件工作流
        UntypedAgent candidateResponder = AgenticServices // 除非你定义生成的组合代理，否则请使用无类型代理，详见_2_顺序代理示例_2_Sequential_Agent_Example
                .conditionalBuilder()
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score >= 0.8, interviewOrganizer)
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score < 0.8, emailAssistant)
                .build();
        // 需知：当定义多个条件时，它们将按顺序依次执行。
        // 若需在此处实现并行执行，请使用异步代理，具体示例参见_5b_Conditional_Workflow_Example_Async

        // 5.从resources/documents/下的text files文件中加载参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview cvReviewFail = new CvReview(0.6, "简历内容不错，但缺少与后端职位相关的技术细节。");
        CvReview cvReviewPass = new CvReview(0.9, "该简历非常出色，完全符合后端职位的所有要求。");

        // 5. 由于我们使用的是无类型代理，因此需要传递所有输入参数的映射表。
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", cvReviewFail // 切换到 cvReviewFail 分支查看其他分支
        );

        // 5. 联系条件代理，根据审核结果回复候选人。
        candidateResponder.invoke(arguments);
        // 在此示例中，我们未对代理作用域（AgenticScope）进行实质性修改，且由于工具已执行最终操作，因此没有有意义的输出可打印。
        // 我们向控制台打印工具执行的操作（发送邮件、更新申请状态）

        // 在调试模式下观察日志时，工具调用结果'success'仍会发送至模型
        // 模型仍会返回类似"邮件已发送至John Doe告知其..."的响应

        // 备注：若工具是最终操作且无需后续调用模型，
        // 通常需添加`@Tool(returnBehavior = ReturnBehavior.IMMEDIATE)`属性
        // https://docs.langchain4j.dev/tutorials/tools#returning-immediately-the-result-of-a-tool-execution-request
        // !!! 但在代理工作流中，工具采用立即返回行为并不推荐，
        // 因为立即返回会将工具结果存储在代理作用域中，可能引发异常
        // 也可通过大型语言模型确定最佳工具/代理继续执行来实现路由行为，具体方式包括：
        // - 使用监督代理：操作代理组件，详见_7_supervisor_orchestration
        // - 将AiServices作为工具使用，例如：
        // RouterService routerService = AiServices.builder(RouterAgent.class)
        //        .chatModel(model)
        //        .tools(medicalExpert, legalExpert, technicalExpert)
        //        .build();
        //
        // 最佳方案取决于具体场景：
        //
        // - 使用条件代理时，需硬编码调用条件
        // - 而通过AiServices或Supervisor，由LLM决定调用哪些专家
        //
        // - 采用代理方案（条件代理/Supervisor）时，所有中间状态和调用链存储于AgenticScope
        // - 相比之下，使用AiServices时难以追踪调用链或中间状态

    }
}