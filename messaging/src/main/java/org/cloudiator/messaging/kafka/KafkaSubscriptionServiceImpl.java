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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
  private static final ExecutorService SUBSCRIBER_EXECUTION = Executors.newCachedThreadPool();
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

    private static final ExecutorService CALLBACK_EXECUTION = Executors.newCachedThreadPool();
    private final Consumer<String, T> consumer;
    private final List<MessageCallback<T>> callbacks;
    private final String topic;

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

    private synchronized void removeCallback(MessageCallback<T> callback) {
      callbacks.remove(callback);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("topic", topic).toString();
    }

    @Override
    public void run() {
      try {
        consumer.subscribe(Collections.singletonList(topic));
        while (!Thread.currentThread().isInterrupted()) {
          final ConsumerRecords<String, T> poll = consumer.poll(1000);
          for (ConsumerRecord<String, T> record : poll) {
            for (MessageCallback<T> callback : callbacks) {
              LOGGER.debug(String.format(
                  "Receiving message with id %s and content %s on topic %s. Scheduling callback %s for Execution",
                  record.key(), record.value(), topic, callback));
              Runnable runnable = RunnableMessageCallback
                  .of(callback, record.key(), record.value());
              CALLBACK_EXECUTION.execute(runnable);
            }
          }
        }
      } finally {
        LOGGER.debug(
            String.format("%s is stopping execution. Stopping consumer %s.", this, consumer));
        consumer.unsubscribe();
        consumer.close();
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T extends Message> Subscription subscribe(String topic, Parser<T> parser,
      MessageCallback<T> messageCallback) {

    Subscriber subscriber = subscriberRegistry.getSubscriberForTopic(topic);
    if (subscriber == null) {
      subscriber = new Subscriber(kafkaConsumerFactory.createKafkaConsumer(parser), topic);
      subscriberRegistry.registerSubscriber(topic, subscriber);
      SUBSCRIBER_EXECUTION.execute(subscriber);
    }

    return subscriber.addCallback(messageCallback);
  }


}