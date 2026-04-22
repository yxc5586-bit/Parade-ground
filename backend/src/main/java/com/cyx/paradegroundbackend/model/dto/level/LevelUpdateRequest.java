package com.cyx.paradegroundbackend.model.dto.level;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class LevelUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String levelId;

    private String levelName;

    private String difficulty;

    private String salaryRange;

    private String tags;

    private String requirement;

    private String options;

    private String correctOptionIds;

    private String analysisDirection;
}
