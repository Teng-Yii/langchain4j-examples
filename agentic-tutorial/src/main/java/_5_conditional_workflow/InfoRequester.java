package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface InfoRequester {

    @Agent("向候选人发送电子邮件以获取更多信息")
    @SystemMessage("""
            您向候选人发送一封友好的邮件，请求提供公司审核申请所需的补充材料。请明确告知他们的申请仍在审核中。
            """)
    @UserMessage("""
            人力资源审核及缺失信息说明：{{cvReview}}
            候选人联系方式：{{candidateContact}}
            职位描述：{{jobDescription}}
            """)
    String send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription, @V("cvReview") CvReview hrReview);
}
