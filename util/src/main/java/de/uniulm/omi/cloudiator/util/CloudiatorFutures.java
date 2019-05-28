package de.uniulm.omi.cloudiator.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudiatorFutures {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CloudiatorFutures.class);

  private CloudiatorFutures() {
    throw new AssertionError("Do not instantiate.");
  }


  public static <T> Collection<T> waitForFutures(Collection<Future<T>> futures)
      throws ExecutionException, InterruptedException {

    final int size = futures.size();
    List<T> results = new ArrayList<>(size);

    LOGGER.debug(String.format("Waiting for a total amount of %s futures", size));

    int i = 1;
    for (Future<T> future : futures) {
      LOGGER.debug(String
          .format("Waiting for future %s of %s. Number of completed futures: %s", i, size,
              results.size()));
      final T t = future.get();
      results.add(t);
    }

    return results;
  }


}
