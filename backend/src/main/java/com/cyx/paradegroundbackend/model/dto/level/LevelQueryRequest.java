package com.cyx.paradegroundbackend.model.dto.level;

import com.cyx.paradegroundbackend.model.dto.common.PageRequest;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LevelQueryRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    private String levelId;

    private String levelName;

    private String difficulty;

    private String salaryRange;

    private Integer priority;
}
