package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AiJobSuggestion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Fake company alias, never a real company name")
    private String companyAlias;

    @Description("Why the company fits the user")
    private String fitReason;

    @Description("Suggested salary range")
    private String salaryRange;
}
