package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface HrCvReviewer {

    @Agent(name = "hrReviewer", description = "审核简历以确认候选人是否符合人力资源要求，提供反馈并给出评分。")
    @SystemMessage("""
            您在人力资源部门工作，负责审核简历以填补符合以下要求的职位：
            {{hrRequirements}}
            您需为每份简历评分并提供反馈（包括优点和缺点）。
            可忽略地址缺失和占位符等信息。
            重要提示：请仅以有效JSON格式返回响应，换行符使用\\n，且不得包含任何Markdown格式或代码块。
            """)
    @UserMessage("""
            请审核此简历：{{candidateCv}} 及随附的电话面试记录：{{phoneInterviewNotes}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv, @V("phoneInterviewNotes") String phoneInterviewNotes, @V("hrRequirements") String hrRequirements);
}
