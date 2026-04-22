package com.cyx.paradegroundbackend.model.dto.common;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long current = 1;

    private long pageSize = 10;
}
