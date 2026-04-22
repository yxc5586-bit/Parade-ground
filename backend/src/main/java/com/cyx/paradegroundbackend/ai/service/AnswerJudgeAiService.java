package com.cyx.paradegroundbackend.ai.service;

import com.cyx.paradegroundbackend.ai.dto.AiJudgeResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AnswerJudgeAiService {

    @SystemMessage(fromResource = "prompts/answer-judge-system-prompt.txt")
    @UserMessage(fromResource = "prompts/answer-judge-user-prompt.txt")
    AiJudgeResult judgeAnswer(@V("input") String input);
}
