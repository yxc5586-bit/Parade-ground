package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GameRecordSummaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private String levelId;

    private String levelName;

    private Integer score;

    private Integer salaryChange;

    private Integer updatedSalary;

    private LocalDateTime createTime;

    public static GameRecordSummaryVO fromEntity(AnswerRecord answerRecord, String levelName) {
        GameRecordSummaryVO vo = new GameRecordSummaryVO();
        vo.setRecordId(answerRecord.getId());
        vo.setLevelId(answerRecord.getLevelId());
        vo.setLevelName(levelName);
        vo.setScore(answerRecord.getScore());
        vo.setSalaryChange(answerRecord.getSalaryChange());
        vo.setUpdatedSalary(answerRecord.getUpdatedSalary());
        vo.setCreateTime(answerRecord.getCreateTime());
        return vo;
    }
}
