package com.cyx.paradegroundbackend.model.vo;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelGenerateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String levelId;

    private String status;

    private Boolean isNew;
}
