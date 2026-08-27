package com.cyx.paradegroundbackend.service;

import com.cyx.paradegroundbackend.exception.BusinessException;
import com.cyx.paradegroundbackend.mapper.AnswerRecordMapper;
import com.cyx.paradegroundbackend.mapper.UserInfoMapper;
import com.cyx.paradegroundbackend.model.entity.AnswerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerSettlementServiceTest {

    private static final int CONCURRENT_REQUESTS = 20;

    @Mock
    private AnswerRecordMapper answerRecordMapper;

    @Mock
    private UserInfoMapper userInfoMapper;

    @Test
    void shouldReturnExistingRecordWithoutChangingSalary() {
        AnswerRecord existing = answerRecord(99L);
        when(answerRecordMapper.selectByUserIdAndLevelId(1L, "level-1")).thenReturn(existing);
        AnswerSettlementService service = new AnswerSettlementService(answerRecordMapper, userInfoMapper);

        AnswerRecord result = service.settle(answerRecord(null), 10000, 12000);

        assertSame(existing, result);
        verify(answerRecordMapper, never()).insert(any(AnswerRecord.class));
        verify(userInfoMapper, never()).updateSalaryIfCurrent(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldRejectWhenSalaryChangedConcurrently() {
        when(answerRecordMapper.selectByUserIdAndLevelId(1L, "level-1")).thenReturn(null);
        when(answerRecordMapper.insert(any(AnswerRecord.class))).thenReturn(1);
        when(userInfoMapper.updateSalaryIfCurrent(1L, 10000, 12000)).thenReturn(0);
        AnswerSettlementService service = new AnswerSettlementService(answerRecordMapper, userInfoMapper);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.settle(answerRecord(null), 10000, 12000)
        );

        assertEquals(40900, exception.getCode());
    }

    @Test
    void shouldSettleOnlyOnceForTwentyConcurrentRequests() throws Exception {
        AtomicReference<AnswerRecord> persistedRecord = new AtomicReference<>();
        AtomicInteger successfulInserts = new AtomicInteger();
        AtomicInteger salary = new AtomicInteger(10000);
        Object insertLock = new Object();

        when(answerRecordMapper.selectByUserIdAndLevelId(anyLong(), anyString()))
                .thenAnswer(invocation -> persistedRecord.get());
        when(answerRecordMapper.insert(any(AnswerRecord.class))).thenAnswer(invocation -> {
            AnswerRecord candidate = invocation.getArgument(0);
            synchronized (insertLock) {
                if (persistedRecord.get() != null) {
                    throw new DuplicateKeyException("uk_userId_levelId");
                }
                candidate.setId(99L);
                persistedRecord.set(candidate);
                successfulInserts.incrementAndGet();
                return 1;
            }
        });
        when(userInfoMapper.updateSalaryIfCurrent(1L, 10000, 12000)).thenAnswer(invocation ->
                salary.compareAndSet(10000, 12000) ? 1 : 0
        );

        AnswerSettlementService service = new AnswerSettlementService(answerRecordMapper, userInfoMapper);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        List<Future<AnswerRecord>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < CONCURRENT_REQUESTS; index++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return service.settle(answerRecord(null), 10000, 12000);
                }));
            }
            start.countDown();

            for (Future<AnswerRecord> future : futures) {
                assertEquals(99L, future.get().getId());
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(1, successfulInserts.get());
        assertEquals(12000, salary.get());
        verify(userInfoMapper).updateSalaryIfCurrent(1L, 10000, 12000);
    }

    private AnswerRecord answerRecord(Long id) {
        AnswerRecord answerRecord = new AnswerRecord();
        answerRecord.setId(id);
        answerRecord.setUserId(1L);
        answerRecord.setLevelId("level-1");
        return answerRecord;
    }
}
