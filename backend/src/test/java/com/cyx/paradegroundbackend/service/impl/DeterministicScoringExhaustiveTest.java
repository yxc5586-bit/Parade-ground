package com.cyx.paradegroundbackend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对 12 个候选项的全部 4096 种选择组合进行回归验证。
 *
 * <p>当前评分方法仍是服务类私有实现，因此测试通过反射调用真实代码。
 * 后续可将算法提取为独立组件，进一步降低测试耦合。</p>
 */
class DeterministicScoringExhaustiveTest {

    private static final List<String> ALL_OPTIONS = List.of(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"
    );
    private static final List<String> CORRECT_OPTIONS = List.of("A", "B", "C", "D", "E", "F");
    private static final int TOTAL_COMBINATIONS = 1 << ALL_OPTIONS.size();

    private AnswerRecordServiceImpl service;
    private Method scoringMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        service = new AnswerRecordServiceImpl();
        scoringMethod = AnswerRecordServiceImpl.class.getDeclaredMethod(
                "calculateFallbackScore",
                List.class,
                List.class
        );
        scoringMethod.setAccessible(true);
    }

    @Test
    void shouldRemainDeterministicAndWithinBoundsForAll4096Combinations() {
        for (int mask = 0; mask < TOTAL_COMBINATIONS; mask++) {
            List<String> selected = selectedOptions(mask);
            int firstScore = calculateScore(selected, CORRECT_OPTIONS);
            int secondScore = calculateScore(selected, CORRECT_OPTIONS);

            assertEquals(firstScore, secondScore, "Score changed for mask=" + mask);
            assertTrue(firstScore >= 0 && firstScore <= 100, "Score out of range for mask=" + mask);
        }
    }

    @Test
    void shouldReturnExpectedBoundaryScores() {
        assertEquals(0, calculateScore(List.of(), CORRECT_OPTIONS));
        assertEquals(100, calculateScore(CORRECT_OPTIONS, CORRECT_OPTIONS));
        assertEquals(50, calculateScore(ALL_OPTIONS, CORRECT_OPTIONS));
        assertEquals(0, calculateScore(List.of("G", "H", "I"), CORRECT_OPTIONS));
    }

    @Test
    void shouldIgnoreDuplicateSelections() {
        assertEquals(
                calculateScore(List.of("A", "B"), CORRECT_OPTIONS),
                calculateScore(List.of("A", "A", "B", "B"), CORRECT_OPTIONS)
        );
    }

    private List<String> selectedOptions(int mask) {
        List<String> selected = new ArrayList<>();
        for (int index = 0; index < ALL_OPTIONS.size(); index++) {
            if ((mask & (1 << index)) != 0) {
                selected.add(ALL_OPTIONS.get(index));
            }
        }
        return selected;
    }

    private int calculateScore(List<String> selected, List<String> correct) {
        try {
            return (int) scoringMethod.invoke(service, selected, correct);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to invoke deterministic scoring method", exception);
        }
    }
}
