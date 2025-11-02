package _5_conditional_workflow;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrganizingTools {

    @Tool
    public Date getCurrentDate(){
        return new Date();
    }

    @Tool("根据给定的职位描述ID，查找需要出席现场面试的人员的电子邮件地址和姓名。")
    public List<String> getInvolvedEmployeesForInterview(@P("职位描述ID") String jobDescriptionId){
        // dummy implementation for demo
        return new ArrayList<>(List.of(
                "Anna Bolena: hiring.manager@company.com",
                "Chris Durue: near.colleague@company.com",
                "Esther Finnigan: vp@company.com"));
    }

    @Tool("根据电子邮件地址为员工创建日程条目")
    public void createCalendarEntry(@P("员工电子邮件地址列表") List<String> emailAddress, @P("会议主题") String topic, @P("开始日期和时间格式为 yyyy-mm-dd hh:mm") String start, @P("结束日期和时间格式为 yyyy-mm-dd hh:mm") String end){
        // dummy implementation for demo
        System.out.println("*** 日历条目已创建 ***");
        System.out.println("主题: " + topic);
        System.out.println("开始时间: " + start);
        System.out.println("结束时间: " + end);
    }

    @Tool
    public int sendEmail(@P("收件人电子邮件地址列表") List<String> to, @P("抄送电子邮件地址列表") List<String> cc, @P("邮件主题") String subject, @P("body") String body){
        // dummy implementation for demo
        System.out.println("*** 邮件已发送 ***");
        System.out.println("发送给: " + to);
        System.out.println("抄送: " + cc);
        System.out.println("主体: " + subject);
        System.out.println("邮件内容: " + body);
        return 1234; // 虚拟邮箱Id
    }

    @Tool
    public void updateApplicationStatus(@P("职位描述ID") String jobDescriptionId, @P("候选人（名，姓）)") String candidateName, @P("新申请状态") String newStatus){
        // dummy implementation for demo
        System.out.println("*** 申请状态已更新 ***");
        System.out.println("职位描述ID: " + jobDescriptionId);
        System.out.println("候选人姓名: " + candidateName);
        System.out.println("新状态: " + newStatus);
    }
}
