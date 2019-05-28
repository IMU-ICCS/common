/*
 * Copyright 2017 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.uniulm.omi.cloudiator.util.execution;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 17.04.15.
 */
public class ScheduledThreadPoolExecutorExecutionService implements ExecutionService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduledThreadPoolExecutorExecutionService.class);
  private final ScheduledExecutorService scheduledExecutorService;

  public ScheduledThreadPoolExecutorExecutionService(
      ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = scheduledExecutorService;
  }

  @Override
  public ScheduledFuture<?> schedule(Schedulable schedulable) {
    LOGGER.info(String
        .format("%s is scheduling %s with initial delay of %s and period of %s %s", this,
            schedulable, schedulable.delay(), schedulable.period(), schedulable.timeUnit()));
    return this.scheduledExecutorService
        .scheduleAtFixedRate(schedulable, schedulable.delay(), schedulable.period(),
            schedulable.timeUnit());
  }

  @Override
  public void execute(Runnable runnable) {
    LOGGER.info(String.format("%s is executing %s", this, runnable));
    this.scheduledExecutorService.execute(runnable);
  }

  @Override
  public void shutdown() {
    this.scheduledExecutorService.shutdown();
  }

  @Override
  public void delayShutdownHook(long terminationTimeout, TimeUnit timeUnit) {
    MoreExecutors.addDelayedShutdownHook(scheduledExecutorService, terminationTimeout, timeUnit);
  }
}
