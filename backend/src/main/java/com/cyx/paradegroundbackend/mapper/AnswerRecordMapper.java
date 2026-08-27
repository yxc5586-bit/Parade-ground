package com.cyx.paradegroundbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {

    List<AnswerRecord> selectByUserId(@Param("userId") Long userId);

    AnswerRecord selectByUserIdAndLevelId(@Param("userId") Long userId, @Param("levelId") String levelId);
}
