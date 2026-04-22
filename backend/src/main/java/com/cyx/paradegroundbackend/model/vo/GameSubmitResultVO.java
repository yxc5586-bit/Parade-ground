package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class GameSubmitResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private String levelId;

    private Integer score;

    private Integer salaryChange;

    private Integer updatedSalary;

    private Object standardAnswers;

    private Object resultReport;

    public static GameSubmitResultVO fromEntity(AnswerRecord answerRecord) {
        GameSubmitResultVO vo = new GameSubmitResultVO();
        vo.setRecordId(answerRecord.getId());
        vo.setLevelId(answerRecord.getLevelId());
        vo.setScore(answerRecord.getScore());
        vo.setSalaryChange(answerRecord.getSalaryChange());
        vo.setUpdatedSalary(answerRecord.getUpdatedSalary());
        vo.setStandardAnswers(LevelQuestionVO.parseJsonOrRaw(answerRecord.getCorrectOptionIds()));
        vo.setResultReport(LevelQuestionVO.parseJsonOrRaw(answerRecord.getResultReport()));
        return vo;
    }
}
