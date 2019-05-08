package io.github.cloudiator.persistance;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TransactionRetryer {

  private static final int WAIT_TIME = 20;
  private static final int ATTEMPTS = 5;

  private TransactionRetryer() {
    throw new AssertionError("Do not instantiate");
  }

  public static <T> T retry(Callable<T> callable) {
    Retryer<T> retryer = RetryerBuilder.<T>newBuilder().retryIfRuntimeException()
        .withWaitStrategy(
            WaitStrategies.randomWait(WAIT_TIME, TimeUnit.SECONDS)).withStopStrategy(
            StopStrategies.stopAfterAttempt(ATTEMPTS)).build();

    try {
      return retryer.call(callable);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Execution failed with cause : " + e.getCause().getMessage(),
          e.getCause());
    } catch (RetryException e) {
      throw new IllegalStateException("Retrying finally failed.", e);
    }
  }


}
