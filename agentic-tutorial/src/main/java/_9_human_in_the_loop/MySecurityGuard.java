package _9_human_in_the_loop;

import dev.langchain4j.guardrail.InputGuardrail;


public class MySecurityGuard implements InputGuardrail {

    public boolean validate(String input) {
        // 检验用户输入是否安全
        return !input.toLowerCase().contains("bad word");
    }
}
