package _1_basic_agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CvGenerator {
    @UserMessage("""
            以下是我的人生经历与职业轨迹信息，
            请据此整理成一份简洁完整的简历。
            请勿虚构事实，亦勿遗漏技能或经验。
            此简历后续将进行精简，现阶段请确保内容完整。
            仅提交简历文件，无需附加其他文本。
            我的个人经历：{{lifeStory}}
            """)
    @Agent("根据用户提供的信息生成一份简洁的简历")
    //@SystemMessage("根据用户提供的信息生成一份简洁的简历")
    String generateCv(@V("lifeStory") String userInfo);
}
