package it.gov.pagopa.group.utils;

import it.gov.pagopa.group.exception.ParallelExecutionException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static it.gov.pagopa.group.constants.GroupConstants.ExceptionMessage.GROUP_PARALLEL_EXECUTION_NOT_POSSIBLE;
import static it.gov.pagopa.group.constants.GroupConstants.ExceptionCode.GROUP_PARALLEL_EXECUTION_ERROR;

public class ParallelStreamUtils {

    private ParallelStreamUtils() {
    }

    public static <T> T goForParallelExecution(Callable<T> runnable, int parallelPool) {
        ForkJoinPool forkJoinPool = null;
        try {
            forkJoinPool = new ForkJoinPool(parallelPool);
            return forkJoinPool.submit(runnable).get();
        } catch (InterruptedException | ExecutionException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new ParallelExecutionException(GROUP_PARALLEL_EXECUTION_ERROR, GROUP_PARALLEL_EXECUTION_NOT_POSSIBLE);
        } finally {
            if (forkJoinPool != null) {
                forkJoinPool.shutdown();
            }
        }
    }
}