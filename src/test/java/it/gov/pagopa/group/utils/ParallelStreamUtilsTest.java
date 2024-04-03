package it.gov.pagopa.group.utils;

import it.gov.pagopa.group.exception.ParallelExecutionException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class ParallelStreamUtilsTest {
    @Test
    void testParallelExecutionWithException() {
        Callable<Integer> task = () -> {
            throw new RuntimeException("Test exception");
        };

        try {
            ParallelStreamUtils.goForParallelExecution(task, 4);
        } catch (ParallelExecutionException e) {
            assertEquals("GROUP_PARALLEL_EXECUTION_ERROR", e.getCode());
            assertEquals("Error occurred during parallel execution", e.getMessage());
        }
    }

}