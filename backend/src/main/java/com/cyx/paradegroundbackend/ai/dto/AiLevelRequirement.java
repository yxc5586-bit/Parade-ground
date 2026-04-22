package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AiLevelRequirement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Business background")
    private String background;

    @Description("Business target")
    private String target;

    @Description("Participating roles")
    private List<String> roles;

    @Description("Core process points")
    private List<String> coreProcesses;

    @Description("Business rules")
    private List<String> rules;

    @Description("Performance requirements")
    private List<String> performanceRequirements;

    @Description("Stability requirements")
    private List<String> stabilityRequirements;

    @Description("Security requirements")
    private List<String> securityRequirements;

    @Description("Exception handling requirements")
    private List<String> exceptionHandling;

    @Description("Launch and rollout requirements")
    private List<String> launchRequirements;

    @Description("General non-functional requirements")
    private List<String> nonFunctionalRequirements;
}
