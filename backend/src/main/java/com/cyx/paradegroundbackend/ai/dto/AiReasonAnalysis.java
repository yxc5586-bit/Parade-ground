package com.cyx.paradegroundbackend.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AiReasonAnalysis implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Description("Reasons for correct choices")
    private List<String> correctChoices;

    @Description("Reasons for wrong choices")
    private List<String> wrongChoices;

    @Description("Key points the user missed")
    private List<String> missedChoices;
}
