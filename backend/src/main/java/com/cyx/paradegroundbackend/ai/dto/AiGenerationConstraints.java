package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AiGenerationConstraints implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Minimum number of candidate options")
    private Integer minOptions;

    @Description("Maximum number of candidate options")
    private Integer maxOptions;

    @Description("Whether real company names are forbidden")
    private Boolean useRealCompanyNames;

    @Description("Whether the business scene must be realistic")
    private Boolean businessSceneMustBeRealistic;
}
