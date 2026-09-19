package com.offerpilot.ai.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class QuestionAiAnalysisVO implements Serializable {
    private String coreIdea;
    private List<String> knowledgePoints;
    private List<String> stepByStepAnalysis;
    private List<String> commonMistakes;
    private String interviewAnswer;
    private String summary;
}
