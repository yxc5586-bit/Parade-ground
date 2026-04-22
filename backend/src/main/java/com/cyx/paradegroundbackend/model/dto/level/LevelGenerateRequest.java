package com.cyx.paradegroundbackend.model.dto.level;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class LevelGenerateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String preferredDirection;
}
