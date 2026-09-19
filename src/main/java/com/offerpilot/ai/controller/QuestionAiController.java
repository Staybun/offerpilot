package com.offerpilot.ai.controller;

import com.offerpilot.ai.assistant.QuestionAiAssistant;
import com.offerpilot.ai.dto.QuestionAiAnalyzeRequest;
import com.offerpilot.ai.dto.QuestionAiChatRequest;
import com.offerpilot.ai.vo.QuestionAiAnalysisVO;
import com.offerpilot.ai.vo.QuestionAiChatVO;
import com.offerpilot.common.BaseResponse;
import com.offerpilot.common.ErrorCode;
import com.offerpilot.common.ResultUtils;
import com.offerpilot.exception.BusinessException;
import com.offerpilot.model.entity.Question;
import com.offerpilot.model.entity.User;
import com.offerpilot.service.QuestionService;
import com.offerpilot.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question/ai")
public class QuestionAiController {
    @Resource
    private QuestionAiAssistant questionAiAssistant;
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;

    @PostMapping("/analyze")
    public BaseResponse<QuestionAiAnalysisVO> analyze(@RequestBody QuestionAiAnalyzeRequest body,
            HttpServletRequest request) {
        if (body == null || body.getQuestionId() == null || body.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        Question question = questionService.getById(body.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String memoryId = resolveMemoryId(body.getMemoryId(), user.getId(), question.getId());
        QuestionAiAnalysisVO result = questionAiAssistant.analyzeQuestion(memoryId, question.getId(),
                question.getTitle(), question.getContent(), question.getTags(), question.getAnswer());
        return ResultUtils.success(result);
    }

    @PostMapping("/chat")
    public BaseResponse<QuestionAiChatVO> chat(@RequestBody QuestionAiChatRequest body,
            HttpServletRequest request) {
        if (body == null || body.getQuestionId() == null || StringUtils.isBlank(body.getMessage())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        String memoryId = resolveMemoryId(body.getMemoryId(), user.getId(), body.getQuestionId());
        String answer = questionAiAssistant.chatAboutQuestion(memoryId, body.getQuestionId(), body.getMessage());
        return ResultUtils.success(new QuestionAiChatVO(memoryId, answer));
    }

    private String resolveMemoryId(String requested, Long userId, Long questionId) {
        return StringUtils.isNotBlank(requested) ? requested : userId + ":question:" + questionId;
    }
}
