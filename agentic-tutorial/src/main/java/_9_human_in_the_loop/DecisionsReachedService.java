package _9_human_in_the_loop;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.guardrail.InputGuardrails;

public interface DecisionsReachedService {
    @InputGuardrails({MySecurityGuard.class})
    @SystemMessage("根据互动情况，若已达成决议则返回 true，若需进一步讨论以找到解决方案则返回 false。")
    @UserMessage("""
          目前的互动:
          秘书: {{proposal}}
          被邀请人: {{candidateAnswer}}""")
    boolean isDecisionReached(@V("proposal") String proposal, @V("candidateAnswer") String candidateAnswer);
}
