package com.cyx.paradegroundbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyx.paradegroundbackend.model.dto.level.LevelAddRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelGenerateRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelQueryRequest;
import com.cyx.paradegroundbackend.model.dto.level.LevelUpdateRequest;
import com.cyx.paradegroundbackend.model.entity.LevelInfo;

public interface LevelInfoService extends IService<LevelInfo> {

    Long addLevel(LevelAddRequest levelAddRequest);

    boolean updateLevel(LevelUpdateRequest levelUpdateRequest);

    LevelInfo getLevelByLevelId(String levelId);

    IPage<LevelInfo> listLevelByPage(LevelQueryRequest levelQueryRequest);

    LevelInfo generateLevel(Long userId, Integer currentSalary, LevelGenerateRequest levelGenerateRequest);
}
