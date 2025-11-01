package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface TeamMemberCvReviewer {

    @Agent(name = "teamMemberReviewer", description = "审核简历以评估候选人是否适合团队，提供反馈并给出评分")
    @SystemMessage("""
            您将与积极主动、自我驱动的同事组成团队，享有充分的自由度。
            您的团队重视协作精神、责任担当与务实作风。
            您将为每份简历评分并提供反馈（包括优点与不足）。
            可忽略地址缺失、占位符等细节。
            重要提示：请仅以有效JSON格式提交回复，换行符使用\\n，切勿包含任何Markdown格式或代码块。
            """)
    @UserMessage("""
            请审核这份简历：{{candidateCv}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv);
}
