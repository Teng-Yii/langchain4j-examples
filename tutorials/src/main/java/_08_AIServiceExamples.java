import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;

public class _08_AIServiceExamples {

    static ChatModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("DASHSCOPE_API_KEY"))
            .modelName("qwen-flash")
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
            .timeout(ofSeconds(60))
            .build();

    /// /////////////// 简单示例 //////////////////////

    static class Simple_AI_Service_Example {

        interface Assistant {

            String chat(String message);
        }

        public static void main(String[] args) {

            Assistant assistant = AiServices.create(Assistant.class, model);

            String userMessage = "翻译 'Plus-Values des cessions de valeurs mobilières, de droits sociaux et gains assimilés'为中文";

            String answer = assistant.chat(userMessage);

            System.out.println(answer); // 证券出售的增值收益、股权收益及类似收益
        }
    }

    /// /////////////// 带有消息和变量 //////////////////////

    static class AI_Service_with_System_Message_Example {

        interface Chef {

            @SystemMessage("您是一位专业的厨师。您友善、礼貌、简洁。")
            String answer(String question);
        }

        public static void main(String[] args) {

            Chef chef = AiServices.create(Chef.class, model);

            String answer = chef.answer("我应该烤鸡多长时间？");

//            """
//                    您好！烤鸡的时间取决于鸡的大小和烤箱温度。一般来说：
//
//                    - **预热烤箱至180°C（350°F）**
//                    - **每500克（约1磅）鸡肉，烤20-25分钟**
//
//                    例如：
//                    - 1.5公斤的整鸡：大约需要1小时15分钟到1小时30分钟。
//                    - 2公斤的鸡：大约需要1小时40分钟到2小时。
//
//                    建议用肉类温度计插入鸡腿最厚处，温度达到**74°C（165°F）**即可，确保鸡肉熟透又多汁。
//
//                    如果喜欢外皮酥脆，最后10分钟可以调高温度到200°C上色。
//
//                    需要我帮您搭配一个简单的腌料或配菜吗？
//                    """
            System.out.println(answer);
        }
    }

    static class AI_Service_with_System_and_User_Messages_Example {

        interface TextUtils {

            @SystemMessage("您是 {{language}} 的专业翻译人员")
            @UserMessage("翻译以下文本：{{text}}")
            String translate(@V("text") String text, @V("language") String language);

            @SystemMessage("用 {{n}} 个要点概括用户发送的每条消息。仅提供要点。")
            List<String> summarize(@UserMessage String text, @V("n") int n);
        }

        public static void main(String[] args) {

            TextUtils utils = AiServices.create(TextUtils.class, model);

            String translation = utils.translate("Hello, how are you?", "中文");
            System.out.println(translation); // 你好，你好吗？

            String text = "AI，即人工智能，是计算机科学的一个分支，旨在创造能够模仿人类智能的机器。其范围从识别模式或语音等简单任务，到做出决策或预测等更复杂的任务。";

            List<String> bulletPoints = utils.summarize(text, 3);
            bulletPoints.forEach(System.out::println);
            // [
            // "AI 是计算机科学的一个分支，致力于创造模仿人类智能的机器。"
            // "- 它涵盖从识别模式、语音到决策和预测等各类任务。",
            // "- 任务复杂度从简单到复杂不等，体现AI的广泛应用。"
            // ]
        }
    }

    /// ///////////////// 提取不同的数据类型 ////////////////////

    static class Sentiment_Extracting_AI_Service_Example {

        enum Sentiment {
            POSITIVE, NEUTRAL, NEGATIVE
        }

        interface SentimentAnalyzer {

            @UserMessage("分析{{it}}的情绪")
            Sentiment analyzeSentimentOf(String text);

            @UserMessage("Does {{it}} have a positive sentiment?")
            boolean isPositive(String text);
        }

        public static void main(String[] args) {

            SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, model);

            Sentiment sentiment = sentimentAnalyzer.analyzeSentimentOf("It is good!");
            System.out.println(sentiment); // POSITIVE

            boolean positive = sentimentAnalyzer.isPositive("It is bad!");
            System.out.println(positive); // false
        }
    }

    static class Hotel_Review_AI_Service_Example {

        public enum IssueCategory {
            /**
             * 维护问题
             */
            MAINTENANCE_ISSUE,
            /**
             * 服务问题
             */
            SERVICE_ISSUE,
            /**
             * 舒适度问题
             */
            COMFORT_ISSUE,
            /**
             * 设施问题
             */
            FACILITY_ISSUE,
            /**
             * 清洁问题
             */
            CLEANLINESS_ISSUE,
            /**
             * 网络连接问题
             */
            CONNECTIVITY_ISSUE,
            /**
             * 入住问题
             */
            CHECK_IN_ISSUE,
            /**
             * 整体体验问题
             */
            OVERALL_EXPERIENCE_ISSUE
        }

        interface HotelReviewIssueAnalyzer {

            @UserMessage("请分析以下评论：|||{{it}}|||")
            List<IssueCategory> analyzeReview(String review);
        }

        public static void main(String[] args) {

            HotelReviewIssueAnalyzer hotelReviewIssueAnalyzer = AiServices.create(HotelReviewIssueAnalyzer.class, model);

            String review = """
                    我们在酒店的住宿体验好坏参半。酒店地理位置绝佳，距离海滩仅几步之遥，
                    这让我们的日常出行非常便捷。房间宽敞，装修精美，
                    营造出舒适宜人的环境。然而，入住期间我们也遇到了一些问题。
                    我们房间的空调运转不正常，晚上睡得非常不舒服。
                    此外，客房服务很慢，我们不得不多次打电话才能拿到额外的毛巾。尽管酒店员工友好，自助早餐也相当美味，但这些问题仍然严重影响了我们的入住体验。""";

            List<IssueCategory> issueCategories = hotelReviewIssueAnalyzer.analyzeReview(review);

            // 应该输出 [MAINTENANCE_ISSUE, SERVICE_ISSUE, COMFORT_ISSUE, OVERALL_EXPERIENCE_ISSUE]
            // 实际输出：[COMFORT_ISSUE, SERVICE_ISSUE, MAINTENANCE_ISSUE]
            System.out.println(issueCategories);
        }
    }

    static class Number_Extracting_AI_Service_Example {

        interface NumberExtractor {

            @UserMessage("从 {{it}} 中提取数字")
            int extractInt(String text);

            @UserMessage("从 {{it}} 中提取数字")
            long extractLong(String text);

            @UserMessage("从 {{it}} 中提取数字")
            BigInteger extractBigInteger(String text);

            @UserMessage("从 {{it}} 中提取数字")
            float extractFloat(String text);

            @UserMessage("从 {{it}} 中提取数字")
            double extractDouble(String text);

            @UserMessage("从 {{it}} 中提取数字")
            BigDecimal extractBigDecimal(String text);
        }

        public static void main(String[] args) {

            NumberExtractor extractor = AiServices.create(NumberExtractor.class, model);

            String text = "经过无数个千年的计算，超级计算机“深思”终于宣布，生命、宇宙以及一切的终极问题的答案是四十二。";

            int intNumber = extractor.extractInt(text);
            System.out.println(intNumber); // 42

            long longNumber = extractor.extractLong(text);
            System.out.println(longNumber); // 42

            BigInteger bigIntegerNumber = extractor.extractBigInteger(text);
            System.out.println(bigIntegerNumber); // 42

            float floatNumber = extractor.extractFloat(text);
            System.out.println(floatNumber); // 42.0

            double doubleNumber = extractor.extractDouble(text);
            System.out.println(doubleNumber); // 42.0

            BigDecimal bigDecimalNumber = extractor.extractBigDecimal(text);
            System.out.println(bigDecimalNumber); // 42.0
        }
    }

    static class Date_and_Time_Extracting_AI_Service_Example {

        interface DateTimeExtractor {

            @UserMessage("从 {{it}} 中提取日期")
            LocalDate extractDateFrom(String text);

            @UserMessage("从 {{it}} 中提取时间")
            LocalTime extractTimeFrom(String text);

            @UserMessage("从 {{it}} 中提取日期和时间")
            LocalDateTime extractDateTimeFrom(String text);
        }

        public static void main(String[] args) {

            DateTimeExtractor extractor = AiServices.create(DateTimeExtractor.class, model);

            String text = "1968 年美国独立日庆祝活动结束后，夜晚弥漫着宁静的气氛，距离午夜仅差十五分钟。";

            LocalDate date = extractor.extractDateFrom(text);
            System.out.println(date); // 1968-07-04

            LocalTime time = extractor.extractTimeFrom(text);
            System.out.println(time); // 23:45

            LocalDateTime dateTime = extractor.extractDateTimeFrom(text);
            System.out.println(dateTime); // 1968-07-04T23:45
        }
    }

    static class POJO_Extracting_AI_Service_Example {

        static class Person {

            @Description("first name of a person")
            // 您可以添加可选描述以帮助 LLM 更好地理解
            private String firstName;
            private String lastName;
            private LocalDate birthDate;

            @Override
            public String toString() {
                return "Person {" +
                        " firstName = \"" + firstName + "\"" +
                        ", lastName = \"" + lastName + "\"" +
                        ", birthDate = " + birthDate +
                        " }";
            }
        }

        interface PersonExtractor {

            @UserMessage("从以下文本中提取一个人返回：{{it}}")
//            @UserMessage("从以下文本中提取一个人并以JSON格式返回：{{it}}")
            Person extractPersonFrom(String text);
        }

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName("qwen-plus")
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    // 当使用支持“json 模式”功能的 LLM 提取 POJO 时
                    // （例如，OpenAI、Azure OpenAI、Vertex AI Gemini、Ollama 等），
                    // 建议启用它（json 模式）以获得更可靠的结果。
                    // 当使用此功能时，LLM 将被强制输出有效的 JSON。
                    // .responseFormat("json_schema")   // TODO 经过实际测试后，发现只有不设置结构化返回时，才能正确匹配自定义对象中的字段
                    .strictJsonSchema(true) // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode
                    .timeout(ofSeconds(60))
                    .build();

            PersonExtractor extractor = AiServices.create(PersonExtractor.class, model);

            String text = "1968年，独立日的余晖渐渐消逝，一个名叫腾的孩子在宁静的夜空下降临。这个姓一的新生儿，标志着一段新旅程的开始。";

            Person person = extractor.extractPersonFrom(text);

            System.out.println(person); // Person { firstName = "John", lastName = "Doe", birthDate = 1968-07-04 }
        }
    }

    /// /////////////////// 描述 ////////////////////////

    static class POJO_With_Descriptions_Extracting_AI_Service_Example {

        static class Recipe {

            @Description("简称，最多 3 个字")
            private String title;

            @Description("简短描述，最多 2 句话")
            private String description;

            @Description("每个步骤应使用 6 到 8 个词来描述，每个步骤应押韵")
            private List<String> steps;

            private Integer preparationTimeMinutes;

            @Override
            public String toString() {
                return "Recipe {" +
                        " title = \"" + title + "\"" +
                        ", description = \"" + description + "\"" +
                        ", steps = " + steps +
                        ", preparationTimeMinutes = " + preparationTimeMinutes +
                        " }";
            }
        }

        @StructuredPrompt("创建一份仅使用 {{ingredients}} 即可烹制的 {{dish}} 菜谱")
        static class CreateRecipePrompt {

            private String dish;
            private List<String> ingredients;
        }

        interface Chef {

            Recipe createRecipeFrom(String... ingredients);

            Recipe createRecipe(CreateRecipePrompt prompt);
        }

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName("qwen-flash")
                    .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                    // 强烈建议不使用responseFormat("json_schema")，否则模型返回的json字段名称和定义不匹配
                    // .responseFormat("json_schema")
                    .strictJsonSchema(true) // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode
                    .timeout(ofSeconds(60))
                    .build();

            Chef chef = AiServices.create(Chef.class, model);

            Recipe recipe = chef.createRecipeFrom("cucumber", "tomato", "feta", "onion", "olives", "lemon");

            // Recipe { title = "地中海沙拉", description = "清新爽口的希腊风味沙拉，搭配柠檬汁提味，适合夏日享用。富含维生素与健康脂肪。", steps = [切块黄瓜，清爽如歌, 番茄滚刀，红润如火, 洋葱丝细，香气四溢, 橄榄洗净，油亮光泽, 奶酪碎撒，洁白如雪, 柠檬汁淋，酸香扑鼻], preparationTimeMinutes = 15 }
            System.out.println(recipe);

            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "oven dish";
            prompt.ingredients = asList("cucumber", "tomato", "feta", "onion", "olives", "potatoes");

            Recipe anotherRecipe = chef.createRecipe(prompt);
            // Recipe { title = "烤蔬盘", description = "简单美味的烤蔬菜，用五种食材轻松完成。温暖香气，适合全家享用。", steps = [切块洗净，番茄洋葱, 土豆橄榄，分层铺展, 撒上奶酪，胡椒轻拌, 入炉烘烤，十五分钟, 翻面调匀，金黄透亮, 取出静置，香溢满堂], preparationTimeMinutes = 25 }
            System.out.println(anotherRecipe);
        }
    }


    /// /////////////////////// 使用记忆 /////////////////////////

    static class ServiceWithMemoryExample {

        interface Assistant {

            String chat(String message);
        }

        public static void main(String[] args) {

            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .build();

            String answer = assistant.chat("你好，我的名字是一腾");
            System.out.println(answer); // 你好，一腾！很高兴认识你～(•̀ᴗ•́)و 有什么我可以帮你的吗？或者你想聊些什么有趣的话题呢？

            String answerWithName = assistant.chat("我的名字是什么");
            System.out.println(answerWithName); // 你的名字是——一腾！（轻轻眨眨眼）
        }
    }

    static class ServiceWithMemoryForEachUserExample {

        interface Assistant {

            String chat(@MemoryId int memoryId, @UserMessage String userMessage);
        }

        public static void main(String[] args) {

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(model)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            System.out.println(assistant.chat(1, "你好，我的名字是一腾"));

            System.out.println(assistant.chat(2, "你好，我的名字是张三"));

            System.out.println(assistant.chat(1, "我的名字是什么"));

            System.out.println(assistant.chat(2, "我的名字是什么"));
        }
    }
}
