package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class LevelQuestionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String levelId;

    private String levelName;

    private String difficulty;

    private String salaryRange;

    private Object tags;

    private Object requirement;

    private Object options;

    public static LevelQuestionVO fromEntity(LevelInfo levelInfo) {
        if (levelInfo == null) {
            return null;
        }
        LevelQuestionVO levelQuestionVO = new LevelQuestionVO();
        levelQuestionVO.setLevelId(levelInfo.getLevelId());
        levelQuestionVO.setLevelName(levelInfo.getLevelName());
        levelQuestionVO.setDifficulty(levelInfo.getDifficulty());
        levelQuestionVO.setSalaryRange(levelInfo.getSalaryRange());
        levelQuestionVO.setTags(parseJsonOrRaw(levelInfo.getTags()));
        levelQuestionVO.setRequirement(parseJsonOrRaw(levelInfo.getRequirement()));
        levelQuestionVO.setOptions(parseJsonOrRaw(levelInfo.getOptions()));
        return levelQuestionVO;
    }

    public static Object parseJsonOrRaw(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String trimmed = text.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            try {
                return OBJECT_MAPPER.readValue(trimmed, Object.class);
            } catch (Exception ignored) {
                return text;
            }
        }
        return text;
    }
}
