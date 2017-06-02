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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.General.Response;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.SubscriptionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 17.03.17.
 */
public class KafkaMessageInterface implements MessageInterface {

  private static final ExecutorService EXECUTOR_SERVICE =
      new LoggingScheduledThreadPoolExecutor(5);
  private final KafkaRequestResponseHandler kafkaRequestResponseHandler = new KafkaRequestResponseHandler();
  private static final Logger LOGGER =
      LoggerFactory.getLogger(KafkaMessageInterface.class);

  KafkaMessageInterface() {

  }

  @Override
  public <T extends Message> Subscription subscribe(String topic, Parser<T> parser,
      MessageCallback<T> callback) {
    KafkaConsumer<String, T> consumer =
        new KafkaConsumer<>(Kafka.properties(), new StringDeserializer(),
            new ProtobufDeserializer<>(parser));
    consumer.subscribe(Collections.singleton(topic));
    final Future<?> future = EXECUTOR_SERVICE.submit(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        final ConsumerRecords<String, T> poll = consumer.poll(1000);
        poll.forEach(
            stringTConsumerRecord -> {
              System.out.println("Receiving message with id " + stringTConsumerRecord.key() + " - "
                  + stringTConsumerRecord.value());
              callback
                  .accept(stringTConsumerRecord.key(), stringTConsumerRecord.value());
            });
      }
    });
    return SubscriptionImpl.of(() -> future.cancel(true));
  }

  @Override
  public <T extends Message> Subscription subscribe(Class<T> messageClass, Parser<T> parser,
      MessageCallback<T> callback) {
    return subscribe(messageClass.getSimpleName(), parser, callback);
  }

  @Override
  public void publish(String topic, Message message) {
    this.publish(topic, UUID.randomUUID().toString(), message);
  }

  @Override
  public void publish(Message message) {
    publish(message.getClass().getSimpleName(), message);
  }

  @Override
  public void publish(String topic, String id, Message message) {
    Kafka.Producers.kafkaProducer()
        .send(new ProducerRecord<>(topic, id, message));
  }

  @Override
  public <T extends Message, S extends Message> void callAsync(String requestTopic, T request,
      String responseTopic, Class<S> responseClass, ResponseCallback<S> responseConsumer) {
    kafkaRequestResponseHandler
        .callAsync(requestTopic, request, responseTopic, responseClass, responseConsumer);
  }

  @Override
  public <T extends Message, S extends Message> void callAsync(T request, Class<S> responseClass,
      ResponseCallback<S> responseConsumer) {
    callAsync(request.getClass().getSimpleName(), request, responseClass.getSimpleName(),
        responseClass,
        responseConsumer);
  }

  @Override
  public <T extends Message, S extends Message> S call(String requestTopic, T request,
      String responseTopic, Class<S> responseClass) throws ResponseException {
    return kafkaRequestResponseHandler
        .call(requestTopic, request, responseTopic, responseClass, 0L);
  }

  @Override
  public <T extends Message, S extends Message> S call(String requestTopic, T request,
      String responseTopic, Class<S> responseClass, long timeout) throws ResponseException {
    return kafkaRequestResponseHandler
        .call(requestTopic, request, responseTopic, responseClass, timeout);
  }

  @Override
  public <T extends Message, S extends Message> S call(T request, Class<S> responseClass)
      throws ResponseException {
    return kafkaRequestResponseHandler
        .call(request.getClass().getSimpleName(), request, responseClass.getSimpleName(),
            responseClass, 0L);
  }

  @Override
  public <T extends Message, S extends Message> S call(T request, Class<S> responseClass,
      long timeout) throws ResponseException {
    return kafkaRequestResponseHandler
        .call(request.getClass().getSimpleName(), request, responseClass.getSimpleName(),
            responseClass,
            timeout);
  }

  @Override
  public void reply(String originId, Message message) {
    reply(message.getClass().getSimpleName(), originId, message);
  }

  @Override
  public <T extends Message> void reply(Class<T> originalMessage, String originId, Error error) {
    reply(originalMessage.getSimpleName(), originId, error);
  }

  @Override
  public void reply(String topic, String originId, Message message) {
    Response response = Response.newBuilder().setCorrelation(originId).setContent(
        Any.pack(message)).build();
    this.publish(topic, response);
  }

  @Override
  public void reply(String topic, String originId, Error error) {
    Response response = Response.newBuilder().setCorrelation(originId).setError(error).build();
    this.publish(topic, response);

  }

  private class KafkaRequestResponseHandler {

    private Map<String, ResponseCallback> waitingCallbacks = new ConcurrentHashMap<>();
    private Map<String, Subscription> activeSubscriptions = new ConcurrentHashMap<>();

    private <T extends Message, S extends Message> void callAsync(String requestTopic, T request,
        String responseTopic,
        Class<S> responseClass, ResponseCallback<S> responseConsumer) {

      //generate message ID
      String messageId = UUID.randomUUID().toString();

      //add waiting callback
      waitingCallbacks.put(messageId, responseConsumer);

      //subscribe to responseTopic
      final Subscription subscription = KafkaMessageInterface.this
          .subscribe(responseTopic, Response.parser(), (id, response) -> {
            //suppressing warning, as we can be sure that the callback is always of the corresponding
            //type. Not using generics on the class, allows us to provide a more versatile implementation
            @SuppressWarnings("unchecked") final ResponseCallback<S> waitingCallback = waitingCallbacks
                .get(response.getCorrelation());
            if (waitingCallback == null) {
              LOGGER.warn(String
                  .format("Could not find callback for correlation id %s. Ignoring response %s",
                      response.getCorrelation(), response));
              return;
            }
            try {
              switch (response.getResponseCase()) {
                case CONTENT:
                  waitingCallback.accept(response.getContent().unpack(responseClass), null);
                  break;
                case ERROR:
                  waitingCallback.accept(null, response.getError());
                  break;
                case RESPONSE_NOT_SET:
                  throw new IllegalStateException(String
                      .format("Neither content or error were set in response message %s.",
                          response));
                default:
                  throw new AssertionError(String
                      .format("Illegal responseCase in message %s: %s", response,
                          response.getResponseCase()));
              }
            } catch (InvalidProtocolBufferException e) {
              throw new IllegalStateException(
                  String.format("Error parsing message %s.", e.getMessage()), e);
            } finally {
              if (activeSubscriptions.containsKey(messageId)) {
                activeSubscriptions.get(messageId).cancel();
              } else {
                LOGGER.warn(
                    String.format("Tried to cancel subscription for message %s, but found none.",
                        messageId));
              }
              waitingCallbacks.remove(response.getCorrelation());
            }
          });
      activeSubscriptions.put(messageId, subscription);

      //send the message
      System.out.println("Sending message with id " + messageId + " - " + request);
      KafkaMessageInterface.this.publish(requestTopic, messageId, request);
    }

    private <T extends Message, S extends Message> S call(String requestTopic, T request,
        String responseTopic,
        Class<S> responseClass, long timeout) throws ResponseException {

      final SyncResponse<S> response = new SyncResponse<>();

      this.callAsync(requestTopic, request, responseTopic, responseClass,
          (content, error) -> {
            synchronized (response) {
              response.set(content, error);
              response.notify();
            }
          });

      synchronized (response) {
        //check if the response is already available
        if (!response.isAvailable()) {
          try {
            response.wait(timeout);
          } catch (InterruptedException e) {
            throw new IllegalStateException(e);
          }
        }
      }
      return response.getContentOrThrowException();

    }

    private class SyncResponse<S> {

      @Nullable
      private volatile S content = null;
      @Nullable
      private volatile Error error = null;

      private boolean isAvailable() {
        if (content != null || error != null) {
          return true;
        }
        return false;
      }

      private S getContentOrThrowException() throws ResponseException {
        if (content != null) {
          return content;
        }
        if (error != null) {
          throw new ResponseException(error.getCode(), error.getMessage());
        }
        throw new IllegalStateException();
      }

      private void set(@Nullable S content, @Nullable Error error) {
        if (this.content != null || this.error != null) {
          throw new IllegalStateException("Values can only be set once");
        }
        if (content != null && error != null) {
          throw new IllegalStateException("Only one value can be set at a time");
        }
        this.content = content;
        this.error = error;
      }

    }


  }
}