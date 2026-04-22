package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AiJudgeUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("User id")
    private Long userId;

    @Description("Current monthly salary in CNY")
    private Integer currentSalary;
}
