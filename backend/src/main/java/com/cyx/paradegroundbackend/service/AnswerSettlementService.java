package com.cyx.paradegroundbackend.service;

import com.cyx.paradegroundbackend.common.ErrorCode;
import com.cyx.paradegroundbackend.exception.BusinessException;
import com.cyx.paradegroundbackend.mapper.AnswerRecordMapper;
import com.cyx.paradegroundbackend.mapper.UserInfoMapper;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 将原有的外部调用分离，事务实现保存一个答案和它的薪水变动。。
 */
@Service
@RequiredArgsConstructor
public class AnswerSettlementService {

    private final AnswerRecordMapper answerRecordMapper;

    private final UserInfoMapper userInfoMapper;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public AnswerRecord settle(AnswerRecord answerRecord, Integer expectedSalary, Integer updatedSalary) {
        AnswerRecord existing = answerRecordMapper.selectByUserIdAndLevelId(
                answerRecord.getUserId(),
                answerRecord.getLevelId()
        );
        if (existing != null) {
            return existing;
        }

        try {
            if (answerRecordMapper.insert(answerRecord) != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to save answer record");
            }
        } catch (DuplicateKeyException e) {
            AnswerRecord concurrentResult = answerRecordMapper.selectByUserIdAndLevelId(
                    answerRecord.getUserId(),
                    answerRecord.getLevelId()
            );
            if (concurrentResult != null) {
                return concurrentResult;
            }
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "Answer was submitted concurrently, please retry", e);
        }

        int updatedRows = userInfoMapper.updateSalaryIfCurrent(
                answerRecord.getUserId(),
                expectedSalary,
                updatedSalary
        );
        if (updatedRows != 1) {
            throw new BusinessException(ErrorCode.CONFLICT_ERROR, "Salary changed concurrently, please retry");
        }
        return answerRecord;
    }
}
