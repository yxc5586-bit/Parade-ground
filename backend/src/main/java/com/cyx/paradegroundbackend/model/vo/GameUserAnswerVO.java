package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class GameUserAnswerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Object selectedOptionIds;

    private Integer clientSpendSeconds;

    public static GameUserAnswerVO fromEntity(AnswerRecord answerRecord) {
        GameUserAnswerVO vo = new GameUserAnswerVO();
        vo.setSelectedOptionIds(LevelQuestionVO.parseJsonOrRaw(answerRecord.getSelectedOptionIds()));
        vo.setClientSpendSeconds(answerRecord.getClientSpendSeconds());
        return vo;
    }
}
