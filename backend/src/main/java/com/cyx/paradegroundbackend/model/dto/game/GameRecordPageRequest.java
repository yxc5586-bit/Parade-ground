package com.cyx.paradegroundbackend.model.dto.game;

import com.cyx.paradegroundbackend.model.dto.common.PageRequest;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameRecordPageRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    private String levelId;
}
