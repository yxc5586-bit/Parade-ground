package com.cyx.paradegroundbackend.ai.service;

import com.cyx.paradegroundbackend.ai.dto.AiJudgeResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/** AI答案评审服务 — 使用LangChain4j调用OpenRouter，根据答案和标准答案评审并生成薪资建议 */
@AiService
public interface AnswerJudgeAiService {

    @SystemMessage(fromResource = "prompts/answer-judge-system-prompt.txt")
    @UserMessage(fromResource = "prompts/answer-judge-user-prompt.txt")
    AiJudgeResult judgeAnswer(@V("input") String input);
}
