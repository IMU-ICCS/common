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

import org.cloudiator.messages.Monitor.CreateMonitorRequest;
import org.cloudiator.messages.Monitor.CreateMonitorResponse;
import org.cloudiator.messages.Monitor.DeleteMonitorRequest;
import org.cloudiator.messages.Monitor.DeleteMonitorResponse;
import org.cloudiator.messages.Monitor.GetMonitorRequest;
import org.cloudiator.messages.Monitor.GetMonitorResponse;
import org.cloudiator.messages.Monitor.MonitorQueryRequest;
import org.cloudiator.messages.Monitor.MonitorQueryResponse;
import org.cloudiator.messages.Monitor.UpdateMonitorRequest;
import org.cloudiator.messages.Monitor.UpdateMonitorResponse;
import org.cloudiator.messaging.ResponseException;

/**
 * Created by daniel on 23.06.17.
 */
public interface MonitorService {

  MonitorQueryResponse findMonitors(MonitorQueryRequest MonitorQueryRequest)
      throws ResponseException;

  CreateMonitorResponse addMonitor(CreateMonitorRequest createMonitorRequest)
      throws ResponseException;

  UpdateMonitorResponse updateMonitor(UpdateMonitorRequest updateMonitorRequest)
      throws ResponseException;

  DeleteMonitorResponse deleteMonitor(DeleteMonitorRequest deleteMonitorRequest)
      throws ResponseException;

  GetMonitorResponse getMonitor(GetMonitorRequest getMonitorRequest) throws ResponseException;

}
