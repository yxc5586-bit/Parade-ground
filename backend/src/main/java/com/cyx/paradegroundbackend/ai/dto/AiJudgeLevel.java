package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AiJudgeLevel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Level id")
    private String levelId;

    @Description("Level name")
    private String levelName;

    @Description("Difficulty label")
    private String difficulty;

    @Description("Detailed business requirement")
    private AiLevelRequirement requirement;

    @Description("All candidate options")
    private List<AiLevelOption> options;

    @Description("Correct option ids")
    private List<String> correctOptionIds;
}
