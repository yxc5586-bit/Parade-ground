package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AnswerJudgeAiRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Task name, fixed to judge_level_answer")
    private String task;

    @Description("User info")
    private AiJudgeUser user;

    @Description("Level question and standard answers")
    private AiJudgeLevel level;

    @Description("User selected answers")
    private AiJudgeUserAnswer userAnswer;

    @Description("Judging rules")
    private AiJudgeRules rules;
}
