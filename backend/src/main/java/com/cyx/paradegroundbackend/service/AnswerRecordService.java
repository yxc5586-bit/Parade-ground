package com.cyx.paradegroundbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyx.paradegroundbackend.model.dto.answerrecord.AnswerRecordAddRequest;
import com.cyx.paradegroundbackend.model.dto.answerrecord.AnswerRecordUpdateRequest;
import com.cyx.paradegroundbackend.model.dto.game.GameRecordPageRequest;
import com.cyx.paradegroundbackend.model.dto.game.GameSubmitRequest;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;

public interface AnswerRecordService extends IService<AnswerRecord> {

    Long addAnswerRecord(AnswerRecordAddRequest answerRecordAddRequest);

    boolean updateAnswerRecord(AnswerRecordUpdateRequest answerRecordUpdateRequest);

    IPage<AnswerRecord> listAnswerRecordByPage(Long userId, GameRecordPageRequest gameRecordPageRequest);

    AnswerRecord submitAnswer(Long userId, GameSubmitRequest gameSubmitRequest);
}
