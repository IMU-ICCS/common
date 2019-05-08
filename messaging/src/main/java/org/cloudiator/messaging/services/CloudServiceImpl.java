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

package org.cloudiator.messaging.services;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import javax.inject.Named;
import org.cloudiator.messages.Cloud.CloudCreatedResponse;
import org.cloudiator.messages.Cloud.CloudDeletedResponse;
import org.cloudiator.messages.Cloud.CloudEvent;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messages.Cloud.CloudQueryResponse;
import org.cloudiator.messages.Cloud.CloudUpdatedResponse;
import org.cloudiator.messages.Cloud.CreateCloudRequest;
import org.cloudiator.messages.Cloud.DeleteCloudRequest;
import org.cloudiator.messages.Cloud.UpdateCloudRequest;
import org.cloudiator.messages.Discovery.DiscoverStatusResponse;
import org.cloudiator.messages.Discovery.DiscoveryEvent;
import org.cloudiator.messages.Discovery.DiscoveryStatusRequest;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;

/**
 * Created by daniel on 29.05.17.
 */
public class CloudServiceImpl implements CloudService {

  private final MessageInterface messageInterface;

  @Inject(optional = true)
  @Named("responseTimeout")
  private long timeout = 20000;

  @Inject
  CloudServiceImpl(MessageInterface messageInterface) {
    checkNotNull(messageInterface, "messageInterface is null");
    this.messageInterface = messageInterface;
  }

  @Override
  public CloudQueryResponse getClouds(CloudQueryRequest cloudQueryRequest)
      throws ResponseException {
    return messageInterface.call(cloudQueryRequest, CloudQueryResponse.class, timeout);
  }

  @Override
  public CloudCreatedResponse createCloud(CreateCloudRequest createCloudRequest)
      throws ResponseException {
    return messageInterface.call(createCloudRequest,
        CloudCreatedResponse.class, timeout);
  }

  @Override
  public CloudUpdatedResponse updateCloud(UpdateCloudRequest updateCloudRequest)
      throws ResponseException {
    return messageInterface.call(updateCloudRequest,
        CloudUpdatedResponse.class, timeout);
  }

  @Override
  public CloudDeletedResponse deleteCloud(DeleteCloudRequest deleteCloudRequest)
      throws ResponseException {
    return messageInterface.call(deleteCloudRequest,
        CloudDeletedResponse.class, timeout);
  }

  @Override
  public void announceEvent(CloudEvent cloudEvent) {
    messageInterface.publish(cloudEvent);
  }

  @Override
  public void announceEvent(DiscoveryEvent discoveryEvent) {
    messageInterface.publish(discoveryEvent);
  }

  @Override
  public DiscoverStatusResponse discoveryStatus() throws ResponseException {
    return messageInterface
        .call(DiscoveryStatusRequest.newBuilder().build(), DiscoverStatusResponse.class, timeout);
  }
}
