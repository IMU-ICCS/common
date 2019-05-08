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

package org.cloudiator.messaging.kafka;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import de.uniulm.omi.cloudiator.util.execution.LoggingThreadPoolExecutor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.SubscriptionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class KafkaSubscriptionServiceImpl implements KafkaSubscriptionService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(KafkaSubscriptionServiceImpl.class);
  private static final ExecutorService SUBSCRIBER_EXECUTION = new LoggingThreadPoolExecutor(0,
      2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue());
  private final SubscriberRegistry subscriberRegistry = new SubscriberRegistry();
  private final KafkaConsumerFactory kafkaConsumerFactory;

  @Inject
  public KafkaSubscriptionServiceImpl(
      KafkaConsumerFactory kafkaConsumerFactory) {
    this.kafkaConsumerFactory = kafkaConsumerFactory;
    init();
  }

  private void init() {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        LOGGER.info(
            String.format("Shutting down callback execution: %s", Subscriber.CALLBACK_EXECUTION));
        MoreExecutors
            .shutdownAndAwaitTermination(Subscriber.CALLBACK_EXECUTION, 1, TimeUnit.MINUTES);
        LOGGER.info(String.format("Shutting down subscriber execution: %s", SUBSCRIBER_EXECUTION));
        MoreExecutors.shutdownAndAwaitTermination(SUBSCRIBER_EXECUTION, 1, TimeUnit.MINUTES);
      }
    }));
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T extends Message> Subscription subscribe(String topic, Parser<T> parser,
      MessageCallback<T> messageCallback) {

    Subscriber subscriber = subscriberRegistry.getSubscriberForTopic(topic);
    if (subscriber == null) {

      LOGGER.debug(String.format("Creating new subscriber for topic %s.", topic));

      subscriber = new Subscriber(kafkaConsumerFactory.createKafkaConsumer(parser), topic);
      subscriber.init();
      subscriberRegistry.registerSubscriber(topic, subscriber);

    } else {
      LOGGER.debug(String.format("Reusing old subscriber for topic %s: %s", topic, subscriber));
    }

    return subscriber.addCallback(messageCallback);
  }

  private static class SubscriberRegistry {

    private final Map<String, Subscriber> subscriberMap = Maps.newConcurrentMap();

    @Nullable
    private Subscriber getSubscriberForTopic(String topic) {
      return subscriberMap.get(topic);
    }

    private void registerSubscriber(String topic, Subscriber subscriber) {
      checkState(getSubscriberForTopic(topic) == null,
          String.format("Consumer for topic %s was already registered", topic));
      subscriberMap.put(topic, subscriber);
    }
  }

  private static class Subscriber<T> implements Runnable {

    private static final ExecutorService CALLBACK_EXECUTION = new LoggingThreadPoolExecutor(0,
        2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue());
    private final Consumer<String, T> consumer;
    private final List<MessageCallback<T>> callbacks;
    private final String topic;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private Subscriber(Consumer<String, T> consumer,
        String topic) {
      this.consumer = consumer;
      this.callbacks = Lists.newCopyOnWriteArrayList(Lists.newArrayList());
      this.topic = topic;
    }


    private synchronized Subscription addCallback(MessageCallback<T> callback) {
      callbacks.add(callback);
      return new SubscriptionImpl(() -> removeCallback(callback));

    }

    private void removeCallback(MessageCallback<T> callback) {
      LOGGER.debug(
          String.format("Removing callback %s from subscription of topic %s.", callback, topic));
      callbacks.remove(callback);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("topic", topic).toString();
    }

    void init() {
      checkState(!initialized.get(), String.format("%s was already initialized.", this));
      synchronized (initialized) {
        consumer.subscribe(Collections.singletonList(topic));
        SUBSCRIBER_EXECUTION.execute(this);
        try {
          if (!initialized.get()) {
            //We wait until the consumer has started polling as this is when
            //partitions get assigned and our subscription becomes active
            //https://issues.apache.org/jira/browse/KAFKA-2359
            initialized.wait();
          }
        } catch (InterruptedException e) {
          LOGGER.warn(String.format("Execution of %s got interrupted. Exiting.", this));
          Thread.currentThread().interrupt();
        }
      }
      LOGGER.debug(String.format("Finished initializing subscriber %s.", this));
    }

    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          final ConsumerRecords<String, T> poll = consumer.poll(1000);
          synchronized (initialized) {
            if (!initialized.get()) {
              initialized.set(true);
              initialized.notify();
            }
          }
          for (ConsumerRecord<String, T> record : poll) {
            if (callbacks.isEmpty()) {
              LOGGER.warn(String
                  .format(
                      "Receiving message %s with key %s on topic %s but no callbacks are currently registered to this topic.",
                      record.value(), record.key(), topic));
            }
            synchronized (this) {
              for (MessageCallback<T> callback : callbacks) {
                LOGGER.trace(String.format(
                    "Receiving message with id %s and content %s on topic %s. Scheduling callback %s for execution",
                    record.key(), record.value(), topic, callback));
                Runnable runnable = RunnableMessageCallback
                    .of(callback, record.key(), record.value());
                CALLBACK_EXECUTION.execute(runnable);
              }
            }
          }
        }
      } catch (Throwable t) {
        LOGGER
            .error(String.format("Unexpected error during handling of subscription %s.", this), t);
      } finally {
        LOGGER.debug(
            String.format("%s is stopping execution. Stopping consumer %s.", this, consumer));
        consumer.unsubscribe();
        consumer.close();
      }
    }
  }


}
