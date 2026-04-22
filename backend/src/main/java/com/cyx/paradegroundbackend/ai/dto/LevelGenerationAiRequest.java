package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
public class LevelGenerationAiRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Task name, fixed to generate_level")
    private String task;

    @Description("The user's current monthly salary in CNY")
    private Integer currentSalary;

    @Description("Optional preferred direction like backend")
    private String preferredDirection;

    @Description("Salary range to difficulty guidance")
    private Map<String, String> difficultyRule;

    @Description("User history statistics")
    private AiHistoryStats historyStats;

    @Description("Level generation constraints")
    private AiGenerationConstraints constraints;
}
