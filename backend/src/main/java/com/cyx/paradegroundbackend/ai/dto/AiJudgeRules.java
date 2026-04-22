package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AiJudgeRules implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Full score value")
    private Integer fullScore;

    @Description("Whether humorous workplace style is expected")
    private Boolean outputHumorStyle;

    @Description("Whether fake company aliases must be used")
    private Boolean useFakeCompanyNames;
}
