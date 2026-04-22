package com.cyx.paradegroundbackend.ai.service;

import com.cyx.paradegroundbackend.ai.dto.AiGeneratedLevel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface LevelGenerationAiService {

    @SystemMessage(fromResource = "prompts/level-generator-system-prompt.txt")
    @UserMessage(fromResource = "prompts/level-generator-user-prompt.txt")
    AiGeneratedLevel generateLevel(@V("input") String input);
}
