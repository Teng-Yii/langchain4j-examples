package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface ManagerCvReviewer {

    @Agent(name = "managerReviewer", description = "根据职位描述审核简历，提供反馈并评分")
    @SystemMessage("""
            您是该职位的招聘经理：
            {{jobDescription}}
            您需要审核应聘者的简历，并从众多申请者中筛选出邀请参加现场面试的人选。
            您需为每份简历评分并提供反馈（包括优点和缺点）。
            可忽略地址缺失、占位符等信息。
            重要提示：请仅以有效JSON格式返回响应，换行符使用\\n，且不得包含任何Markdown格式或代码块。
            """)
    @UserMessage("""
            请审核这份简历：{{candidateCv}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv, @V("jobDescription") String jobDescription);
}
