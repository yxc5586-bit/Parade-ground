package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 关卡题面VO — 前端答题页使用的完整关卡数据，将JSON字符串字段解析为Object返回。
 * 同时被精选关卡列表页复用（列表场景下前端可忽略requirement/options字段）。
 */
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
