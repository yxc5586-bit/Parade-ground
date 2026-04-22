package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AiLevelOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Option id, usually A-Z")
    private String id;

    @Description("Option content shown to the user")
    private String content;

    @Description("Option type, fixed to option")
    private String type;
}
