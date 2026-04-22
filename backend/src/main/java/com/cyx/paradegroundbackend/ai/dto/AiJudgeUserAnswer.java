package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AiJudgeUserAnswer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Selected option ids")
    private List<String> selectedOptionIds;
}
