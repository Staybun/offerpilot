package com.offerpilot.ai.vo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAiChatVO implements Serializable {
    private String memoryId;
    private String answer;
}
