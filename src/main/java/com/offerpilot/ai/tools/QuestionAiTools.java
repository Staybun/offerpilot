package com.offerpilot.ai.tools;

import com.offerpilot.model.entity.Question;
import com.offerpilot.service.QuestionService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component("questionAiTools")
public class QuestionAiTools {
    @Resource
    private QuestionService questionService;

    @Tool("根据题目 ID 查询题目标题、内容、标签和参考答案")
    public String getQuestion(@P("题目 ID") Long questionId) {
        Question question = questionService.getById(questionId);
        if (question == null) {
            return "题目不存在";
        }
        return "标题：" + question.getTitle() + "\n内容：" + question.getContent()
                + "\n标签：" + question.getTags() + "\n参考答案：" + question.getAnswer();
    }
}
