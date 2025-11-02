package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface EmailAssistant {

    @Agent("向未通过的候选人发送拒绝邮件")
    @SystemMessage("""
            您向未通过首轮审核的申请者发送一封友好的邮件。
            同时将申请状态更新为"已拒绝"。
            返回已发送邮件的ID。
            """)
    @UserMessage("""
            被拒绝的候选人：{{candidateContact}}
            职位：{{jobDescription}}
            """)
    int send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}
