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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import java.util.Properties;
import javax.inject.Named;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

class BaseKafkaConsumerFactory implements KafkaConsumerFactory {

  private final String bootstrapServers;
  private final String groupId;

  @Inject
  BaseKafkaConsumerFactory(@Named("bootstrap.servers") String bootstrapServers,
      @Named("group.id") String groupId) {
    checkNotNull(bootstrapServers, "bootstrapServers is null");
    checkNotNull(groupId, "groupId is null");
    this.bootstrapServers = bootstrapServers;
    this.groupId = groupId;
  }

  @Override
  public <T extends Message> Consumer<String, T> createKafkaConsumer(Parser<T> parser) {
    Properties properties = new Properties();
    properties.put("bootstrap.servers", bootstrapServers);
    properties.put("group.id", groupId);
    properties.put("zookeeper.session.timeout.ms", "400");
    properties.put("zookeeper.sync.time.ms", "200");
    properties.put("auto.commit.interval.ms", "1000");
    return new KafkaConsumer<>(properties, new StringDeserializer(),
        new ProtobufDeserializer<>(parser));
  }

}