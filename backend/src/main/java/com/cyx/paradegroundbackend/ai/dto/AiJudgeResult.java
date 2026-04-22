package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AiJudgeResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Score out of 100")
    private Integer score;

    @Description("Evaluation title")
    private String evaluationTitle;

    @Description("Evaluation text")
    private String evaluationText;

    @Description("Salary change amount")
    private Integer salaryChange;

    @Description("Updated salary after judging")
    private Integer updatedSalary;

    @Description("Suggested fake companies to apply to")
    private List<AiJobSuggestion> jobSuggestions;

    @Description("Why the answer received this result")
    private AiReasonAnalysis reasonAnalysis;

    @Description("Standard correct answers")
    private List<String> standardAnswers;

    @Description("Detailed natural language solution")
    private String detailedSolution;
}
