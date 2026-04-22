package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class GameResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer score;

    private Integer salaryChange;

    private Integer updatedSalary;

    private Object standardAnswers;

    private Object resultReport;

    public static GameResultVO fromEntity(AnswerRecord answerRecord) {
        GameResultVO vo = new GameResultVO();
        vo.setScore(answerRecord.getScore());
        vo.setSalaryChange(answerRecord.getSalaryChange());
        vo.setUpdatedSalary(answerRecord.getUpdatedSalary());
        vo.setStandardAnswers(LevelQuestionVO.parseJsonOrRaw(answerRecord.getCorrectOptionIds()));
        vo.setResultReport(LevelQuestionVO.parseJsonOrRaw(answerRecord.getResultReport()));
        return vo;
    }
}
