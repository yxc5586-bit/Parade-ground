package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AiHistoryStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Total number of completed levels")
    private Integer totalLevels;

    @Description("Average score across completed levels")
    private Integer avgScore;

    @Description("Tags the user performs strongly on")
    private List<String> strongTags;

    @Description("Tags the user performs weakly on")
    private List<String> weakTags;
}
