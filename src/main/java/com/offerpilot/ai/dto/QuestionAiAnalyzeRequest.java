package com.offerpilot.ai.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class QuestionAiAnalyzeRequest implements Serializable {
    private Long questionId;
    private String memoryId;
}
