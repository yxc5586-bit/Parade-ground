package com.cyx.paradegroundbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyx.paradegroundbackend.model.entity.LevelInfo;
import org.apache.ibatis.annotations.Param;

public interface LevelInfoMapper extends BaseMapper<LevelInfo> {

    LevelInfo selectByLevelId(@Param("levelId") String levelId);
}
