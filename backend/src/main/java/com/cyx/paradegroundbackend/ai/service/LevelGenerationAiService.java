package com.cyx.paradegroundbackend.ai.service;

import com.cyx.paradegroundbackend.ai.dto.AiGeneratedLevel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/** AI关卡生成服务 — 使用LangChain4j调用OpenRouter，根据prompts模板和用户输入生成关卡JSON */
@AiService
public interface LevelGenerationAiService {

    @SystemMessage(fromResource = "prompts/level-generator-system-prompt.txt")
    @UserMessage(fromResource = "prompts/level-generator-user-prompt.txt")
    AiGeneratedLevel generateLevel(@V("input") String input);
}
