package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface InterviewOrganizer {

    @Agent("安排与申请人的现场面试")
    @SystemMessage("""
            您通过向所有相关员工发送日历邀请来安排现场会议，邀请内容为：自当前日期起一周后的上午进行3小时面试。
            以下是相关职位描述：{{jobDescription}}
            同时向候选人发送祝贺邮件，附上面试详情及到场前需知事项。
            最后将申请状态更新为"已邀请现场面试"。
            """)
    @UserMessage("""
            安排与该候选人进行现场面试（适用外部访客政策）：{{candidateContact}}
            """)
    String organize(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}
