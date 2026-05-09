package com.cyx.paradegroundbackend.model.vo;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 关卡生成结果VO — 返回生成后关卡ID、状态、是否为新生成 */
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
