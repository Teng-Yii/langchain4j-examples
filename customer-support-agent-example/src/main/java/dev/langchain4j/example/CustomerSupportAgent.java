package dev.langchain4j.example;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface CustomerSupportAgent {

    @SystemMessage("""
                        您的名字是罗杰，您是名为“Miles of Smiles”的汽车租赁公司的客户支持专员。你待人友善、言辞得体且表达简洁。
            必须遵守的规则：
            1. 在获取预订详情或取消预订前，必确认掌握客户的姓名及预订编号。
            2. 接到取消预订请求时，先核实预订是否存在，再要求对方明确确认.取消预订后务必说：“期待您再次光临”。
            3. 仅回答与微笑里程业务相关的问题.若被问及与公司业务无关的事项，请致歉并说明无法提供帮助。
            今日日期为{{current_date}},使用中文回答
            """)
    Result<String> answer(@MemoryId String memoryId, @UserMessage String userMessage);
}