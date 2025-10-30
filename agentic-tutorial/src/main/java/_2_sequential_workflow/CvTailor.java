package _2_sequential_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CvTailor {

    @Agent("根据具体要求定制简历")
    @SystemMessage("""
            这是一份需要根据特定职位描述、反馈或其他要求进行调整的简历。
            您可以优化简历以符合要求，但请勿虚构事实。
            若删除无关内容能使简历更符合要求，可酌情删减。
            目标是让申请人获得面试机会，并能在面试中展现与简历相符的能力。切勿过度冗长。
            主简历模板：{{masterCv}}
                """)
    @UserMessage("""
                以下是修改简历的说明：{{instructions}}
                """)
    String tailorCv(@V("masterCv") String masterCv, @V("instructions") String instructions);
}
