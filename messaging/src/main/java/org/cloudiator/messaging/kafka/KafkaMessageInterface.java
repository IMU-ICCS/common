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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.General.Response;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 17.03.17.
 */
@Singleton
class KafkaMessageInterface implements MessageInterface {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageInterface.class);
  private final KafkaRequestResponseHandler kafkaRequestResponseHandler =
      new KafkaRequestResponseHandler();
  private final KafkaProducerFactory kafkaProducerFactory;
  private final KafkaSubscriptionService kafkaSubscriptionService;


  @Inject
  KafkaMessageInterface(KafkaProducerFactory kafkaProducerFactory,
      KafkaSubscriptionService kafkaSubscriptionService) {
    this.kafkaProducerFactory = kafkaProducerFactory;
    this.kafkaSubscriptionService = kafkaSubscriptionService;
  }

  @Override
  public <T extends Message> Subscription subscribe(String topic, Parser<T> parser,
      MessageCallback<T> callback) {

    LOGGER.debug(String
        .format("Registering new subscription for topic %s, with parser %s and callback %s.",
            topic, parser, callback));

    return kafkaSubscriptionService.subscribe(topic, parser, callback);
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
    LOGGER.trace(
        String.format("Publishing new message %s on topic %s with id %s.", message, topic, id));
    kafkaProducerFactory.createKafkaProducer()
        .send(new ProducerRecord<>(topic, id, message), new Callback() {
          @Override
          public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
              LOGGER.error(String.format("Error sending message with ID %s on topic %s", id, topic),
                  exception);
              throw new IllegalStateException(
                  String.format("Error sending message with ID %s on topic %s", id, topic),
                  exception);
            }
            LOGGER.trace(
                String.format("Successfully send message with id %s in topic %s.", id, topic));
          }
        });

  }

  private void publishSync(String topic, Message message) {
    this.publishSync(topic, UUID.randomUUID().toString(), message);
  }

  private void publishSync(Message message) {
    publishSync(message.getClass().getSimpleName(), message);
  }

  private void publishSync(String topic, String id, Message message) {
    LOGGER.trace(String
        .format("Publishing (sync) new message %s on topic %s with id %s.", message, topic,
            id));
    Producer<String, Message> producer = kafkaProducerFactory.createKafkaProducer();
    producer.send(new ProducerRecord<>(topic, id, message), new Callback() {
      @Override
      public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
          LOGGER.error(String.format("Error sending message with ID %s on topic %s", id, topic),
              exception);
          throw new IllegalStateException(
              String.format("Error sending message with ID %s on topic %s", id, topic),
              exception);
        }
        LOGGER.trace(
            String.format("Successfully send message with id %s in topic %s.", id, topic));
      }
    });
    producer.flush();
  }

  @Override
  public <T extends Message, S extends Message> void callAsync(String requestTopic, T request,
      String responseTopic, Class<S> responseClass, ResponseCallback<S> responseConsumer) {

    LOGGER.trace(String.format(
        "Async call to requestTopic %s with request %s. Response Topic is %s, using class %s and consumer %s",
        requestTopic, request, responseTopic, responseClass, responseConsumer));

    kafkaRequestResponseHandler
        .callAsync(requestTopic, request, responseTopic, responseClass, responseConsumer);
  }

  @Override
  public <T extends Message, S extends Message> void callAsync(T request, Class<S> responseClass,
      ResponseCallback<S> responseConsumer) {
    callAsync(request.getClass().getSimpleName(), request, responseClass.getSimpleName(),
        responseClass, responseConsumer);
  }

  @Override
  public <T extends Message, S extends Message> S call(String requestTopic, T request,
      String responseTopic, Class<S> responseClass) throws ResponseException {

    LOGGER.trace(String.format(
        "Call (sync) to requestTopic %s with request %s. Response Topic is %s, using class %s",
        requestTopic, request, responseTopic, responseClass));

    return kafkaRequestResponseHandler
        .call(requestTopic, request, responseTopic, responseClass, 0L);
  }

  @Override
  public <T extends Message, S extends Message> S call(String requestTopic, T request,
      String responseTopic, Class<S> responseClass, long timeout) throws ResponseException {

    LOGGER.trace(String.format(
        "Call (sync) to requestTopic %s with request %s. Response Topic is %s, using class %s and timeout %s",
        requestTopic, request, responseTopic, responseClass, timeout));

    return kafkaRequestResponseHandler
        .call(requestTopic, request, responseTopic, responseClass, timeout);
  }

  @Override
  public <T extends Message, S extends Message> S call(T request, Class<S> responseClass)
      throws ResponseException {

    LOGGER.trace(String
        .format("Call (sync) with request %s. Response Class is %s.", request, responseClass));

    return kafkaRequestResponseHandler
        .call(request.getClass().getSimpleName(), request, responseClass.getSimpleName(),
            responseClass, 0L);
  }

  @Override
  public <T extends Message, S extends Message> S call(T request, Class<S> responseClass,
      long timeout) throws ResponseException {

    LOGGER.trace(String
        .format("Call (sync) with request %s. Response Class is %s. Timeout is %s.", request,
            responseClass, timeout));

    return kafkaRequestResponseHandler
        .call(request.getClass().getSimpleName(), request, responseClass.getSimpleName(),
            responseClass, timeout);
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

    LOGGER.trace(String
        .format("Replying on topic %s for origin ID %s with message %s", topic, originId,
            message));

    Response response =
        Response.newBuilder().setCorrelation(originId).setContent(Any.pack(message)).build();
    this.publishSync(topic, response);
  }

  @Override
  public void reply(String topic, String originId, Error error) {

    LOGGER.trace(String
        .format("Replying on topic %s for origin ID %s with error %s", topic, originId, error));

    Response response = Response.newBuilder().setCorrelation(originId).setError(error).build();
    this.publishSync(topic, response);

  }

  private class KafkaRequestResponseHandler {

    private Map<String, ResponseCallback> waitingCallbacks = new ConcurrentHashMap<>();
    private Map<String, Subscription> pendingSubscriptions = new HashMap<>();
    private List<String> markedForDeletion = Lists.newArrayList();

    private void cleanup() {

      //todo remove very long waiting callbacks

      synchronized (KafkaRequestResponseHandler.this) {
        ArrayList<String> deleted = new ArrayList<>();
        for (String toDelete : markedForDeletion) {
          final Subscription subscription = pendingSubscriptions.get(toDelete);
          if (subscription != null) {
            subscription.cancel();
            deleted.add(toDelete);
          }
          pendingSubscriptions.remove(toDelete);
        }
        markedForDeletion.removeAll(deleted);
      }
    }

    private <T extends Message, S extends Message> void callAsync(String requestTopic,
        T request, String responseTopic, Class<S> responseClass,
        ResponseCallback<S> responseConsumer) {

      //generate message ID
      String messageId = UUID.randomUUID().toString();

      synchronized (this) {
        //add waiting callback
        waitingCallbacks.put(messageId, responseConsumer);
      }

      //subscribeEncryption to responseTopic
      final Subscription subscription = KafkaMessageInterface.this
          .subscribe(responseTopic, Response.parser(), (id, response) -> {
            //suppressing warning, as we can be sure that the callback is always of the corresponding
            //type. Not using generics on the class, allows us to provide a more versatile implementation
            ResponseCallback<S> waitingCallback;
            synchronized (KafkaRequestResponseHandler.this) {
              //noinspection unchecked
              waitingCallback =
                  waitingCallbacks.get(response.getCorrelation());
              if (waitingCallback == null) {
                LOGGER.trace(String.format(
                    "Could not find callback for correlation id %s for a message on requestTopic %s and responseTopic %s. Ignoring",
                    response.getCorrelation(), requestTopic, responseTopic));
                return;
              }
              //we remove the callback before we start execution, to ensure
              //that this is only executed once
              waitingCallbacks.remove(response.getCorrelation());
            }

            try {
              switch (response.getResponseCase()) {
                case CONTENT:
                  waitingCallback
                      .accept(response.getContent().unpack(responseClass), null);
                  break;
                case ERROR:
                  waitingCallback.accept(null, response.getError());
                  break;
                case RESPONSE_NOT_SET:
                  throw new IllegalStateException(String.format(
                      "Neither content or error were set in response message %s.",
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
              synchronized (KafkaRequestResponseHandler.this) {
                markedForDeletion.add(messageId);
              }
              cleanup();
            }
          });
      synchronized (KafkaRequestResponseHandler.this) {
        pendingSubscriptions.put(messageId, subscription);
      }

      //send the message
      KafkaMessageInterface.this.publish(requestTopic, messageId, request);
    }

    private <T extends Message, S extends Message> S call(String requestTopic, T request,
        String responseTopic, Class<S> responseClass, long timeout) throws ResponseException {

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
        if (!response.isAvailable()) {
          throw new TimeoutException(timeout);
        }
      }
      return response.getContentOrThrowException();

    }

    private class SyncResponse<S> {

      @Nullable
      private S content = null;
      @Nullable
      private Error error = null;

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
