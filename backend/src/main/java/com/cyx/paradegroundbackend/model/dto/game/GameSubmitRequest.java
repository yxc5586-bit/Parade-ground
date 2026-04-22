package com.cyx.paradegroundbackend.model.dto.game;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class GameSubmitRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String levelId;

    private List<String> selectedOptionIds;

    private Integer clientSpendSeconds;
}
