package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AiGeneratedLevel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Level name")
    private String levelName;

    @Description("Difficulty label")
    private String difficulty;

    @Description("Salary range label")
    private String salaryRange;

    @Description("Level tags")
    private List<String> tags;

    @Description("Detailed business requirement")
    private AiLevelRequirement requirement;

    @Description("Candidate options, 12 to 18 items")
    private List<AiLevelOption> options;

    @Description("Correct option ids")
    private List<String> correctOptionIds;

    @Description("Analysis direction for the level")
    private String analysisDirection;
}
