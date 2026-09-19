package com.offerpilot.ai.assistant;

import com.offerpilot.ai.vo.QuestionAiAnalysisVO;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService(chatMemoryProvider = "chatMemoryProvider", tools = "questionAiTools")
public interface QuestionAiAssistant {

    @SystemMessage("""
            你是资深 Java 后端面试官和学习导师。请根据题目信息生成准确、易懂、适合面试表达的结构化解析。
            不得编造信息；输出必须能映射到 QuestionAiAnalysisVO，其中知识点、分析步骤和易错点必须是数组。
            """)
    @UserMessage("""
            题目 ID：{{questionId}}
            标题：{{title}}
            内容：{{content}}
            标签：{{tags}}
            参考答案：{{answer}}
            请给出核心思路、知识点、分步解析、易错点、面试回答建议和总结。
            """)
    QuestionAiAnalysisVO analyzeQuestion(@MemoryId String memoryId,
            @V("questionId") Long questionId, @V("title") String title,
            @V("content") String content, @V("tags") String tags,
            @V("answer") String answer);

    @SystemMessage("你是 Java 后端面试学习助手，请结合会话记忆回答追问；信息不足时可调用题目查询工具。")
    @UserMessage("当前题目 ID：{{questionId}}\n用户追问：{{message}}")
    String chatAboutQuestion(@MemoryId String memoryId,
            @V("questionId") Long questionId, @V("message") String message);
}
