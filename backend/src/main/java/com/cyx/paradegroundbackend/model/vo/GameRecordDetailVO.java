package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GameRecordDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private String levelId;

    private LevelQuestionVO question;

    private GameUserAnswerVO userAnswer;

    private GameResultVO result;

    private LocalDateTime createTime;

    public static GameRecordDetailVO fromEntity(AnswerRecord answerRecord, LevelInfo levelInfo) {
        GameRecordDetailVO vo = new GameRecordDetailVO();
        vo.setRecordId(answerRecord.getId());
        vo.setLevelId(answerRecord.getLevelId());
        vo.setQuestion(LevelQuestionVO.fromEntity(levelInfo));
        vo.setUserAnswer(GameUserAnswerVO.fromEntity(answerRecord));
        vo.setResult(GameResultVO.fromEntity(answerRecord));
        vo.setCreateTime(answerRecord.getCreateTime());
        return vo;
    }
}
