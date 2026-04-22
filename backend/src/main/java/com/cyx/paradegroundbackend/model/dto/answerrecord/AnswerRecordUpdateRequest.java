package com.cyx.paradegroundbackend.model.dto.answerrecord;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class AnswerRecordUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String levelId;

    private String selectedOptionIds;

    private String correctOptionIds;

    private Integer clientSpendSeconds;

    private Integer score;

    private Integer salaryChange;

    private Integer updatedSalary;

    private String resultReport;
}
