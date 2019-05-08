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
import org.cloudiator.messaging.ResponseException;

/**
 * Created by daniel on 24.05.17.
 */
public interface CloudService {

  CloudQueryResponse getClouds(CloudQueryRequest cloudQueryRequest) throws ResponseException;

  CloudCreatedResponse createCloud(CreateCloudRequest createCloudRequest) throws ResponseException;

  CloudUpdatedResponse updateCloud(UpdateCloudRequest updateCloudRequest) throws ResponseException;

  CloudDeletedResponse deleteCloud(DeleteCloudRequest deleteCloudRequest) throws ResponseException;

  void announceEvent(CloudEvent cloudEvent);

  void announceEvent(DiscoveryEvent discoveryEvent);

  DiscoverStatusResponse discoveryStatus() throws ResponseException;

}
