package org.cloudiator.messaging.services;

import com.google.inject.Inject;
import javax.annotation.Nullable;
import javax.inject.Named;
import org.cloudiator.messages.Process;
import org.cloudiator.messages.Process.CreateFaasProcessRequest;
import org.cloudiator.messages.Process.CreateLanceProcessRequest;
import org.cloudiator.messages.Process.CreateProcessRequest;
import org.cloudiator.messages.Process.CreateScheduleRequest;
import org.cloudiator.messages.Process.CreateSparkClusterRequest;
import org.cloudiator.messages.Process.CreateSparkProcessRequest;
import org.cloudiator.messages.Process.DeleteLanceProcessRequest;
import org.cloudiator.messages.Process.DeleteProcessRequest;
import org.cloudiator.messages.Process.DeleteScheduleRequest;
import org.cloudiator.messages.Process.FaasProcessCreatedResponse;
import org.cloudiator.messages.Process.LanceProcessCreatedResponse;
import org.cloudiator.messages.Process.LanceProcessDeletedResponse;
import org.cloudiator.messages.Process.ProcessCreatedResponse;
import org.cloudiator.messages.Process.ProcessDeletedResponse;
import org.cloudiator.messages.Process.ProcessEvent;
import org.cloudiator.messages.Process.ProcessQueryRequest;
import org.cloudiator.messages.Process.ProcessQueryResponse;
import org.cloudiator.messages.Process.ProcessStatusQuery;
import org.cloudiator.messages.Process.ProcessStatusResponse;
import org.cloudiator.messages.Process.ScaleRequest;
import org.cloudiator.messages.Process.ScaleResponse;
import org.cloudiator.messages.Process.ScheduleCreatedResponse;
import org.cloudiator.messages.Process.ScheduleDeleteResponse;
import org.cloudiator.messages.Process.ScheduleEvent;
import org.cloudiator.messages.Process.ScheduleGraphRequest;
import org.cloudiator.messages.Process.ScheduleGraphResponse;
import org.cloudiator.messages.Process.ScheduleQueryRequest;
import org.cloudiator.messages.Process.ScheduleQueryResponse;
import org.cloudiator.messages.Process.SparkClusterCreatedResponse;
import org.cloudiator.messages.Process.SparkProcessCreatedResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.ResponseException;

public class ProcessServiceImpl implements ProcessService {

  private final MessageInterface messageInterface;

  @Inject(optional = true)
  @Named("responseTimeout")
  private long timeout = 20000;

  @Inject
  public ProcessServiceImpl(MessageInterface messageInterface) {
    this.messageInterface = messageInterface;
  }

  @Override
  public ScheduleQueryResponse querySchedules(ScheduleQueryRequest scheduleQueryRequest)
      throws ResponseException {
    return this.messageInterface
        .call(scheduleQueryRequest, ScheduleQueryResponse.class, timeout);
  }

  @Override
  public ProcessQueryResponse queryProcesses(ProcessQueryRequest processQueryRequest)
      throws ResponseException {
    return this.messageInterface.call(processQueryRequest, ProcessQueryResponse.class, timeout);
  }

  @Override
  public void subscribeScheduleQueryRequest(MessageCallback<ScheduleQueryRequest> callback) {
    this.messageInterface
        .subscribe(ScheduleQueryRequest.class, ScheduleQueryRequest.parser(), callback);
  }

  @Override
  public void subscribeScheduleDeleteRequest(MessageCallback<DeleteScheduleRequest> callback) {
    this.messageInterface
        .subscribe(DeleteScheduleRequest.class, DeleteScheduleRequest.parser(), callback);
  }

  @Override
  public void subscribeProcessQueryRequest(MessageCallback<ProcessQueryRequest> callback) {
    messageInterface.subscribe(ProcessQueryRequest.class, ProcessQueryRequest.parser(), callback);
  }

  @Override
  public ScheduleCreatedResponse createSchedule(CreateScheduleRequest createScheduleRequest)
      throws ResponseException {
    return messageInterface.call(createScheduleRequest, ScheduleCreatedResponse.class, timeout);
  }

  @Override
  public void createScheduleAsync(CreateScheduleRequest createScheduleRequest,
      ResponseCallback<ScheduleCreatedResponse> callback) {
    messageInterface.callAsync(createScheduleRequest, ScheduleCreatedResponse.class, callback);
  }

  @Override
  public void deleteScheduleAsync(DeleteScheduleRequest deleteScheduleRequest,
      ResponseCallback<ScheduleDeleteResponse> callback) {
    messageInterface.callAsync(deleteScheduleRequest, ScheduleDeleteResponse.class, callback);
  }

  @Override
  public ProcessCreatedResponse createProcess(CreateProcessRequest createProcessRequest)
      throws ResponseException {
    return messageInterface.call(createProcessRequest, ProcessCreatedResponse.class, timeout);
  }

  @Override
  public ProcessDeletedResponse deleteProcess(DeleteProcessRequest deleteProcessRequest)
      throws ResponseException {
    return messageInterface.call(deleteProcessRequest, ProcessDeletedResponse.class, timeout);
  }

  @Override
  public void createProcessAsync(CreateProcessRequest createProcessRequest,
      ResponseCallback<ProcessCreatedResponse> callback) {
    messageInterface.callAsync(createProcessRequest, ProcessCreatedResponse.class, callback);
  }

  @Override
  public void deleteProcessAsync(DeleteProcessRequest deleteProcessRequest,
      ResponseCallback<ProcessDeletedResponse> callback) {
    messageInterface.callAsync(deleteProcessRequest, ProcessDeletedResponse.class, callback);
  }

  @Override
  public void subscribeCreateProcessRequest(MessageCallback<CreateProcessRequest> callback) {
    messageInterface.subscribe(CreateProcessRequest.class, CreateProcessRequest.parser(), callback);
  }

  @Override
  public void subscribeDeleteProcessRequest(MessageCallback<DeleteProcessRequest> callback) {
    messageInterface.subscribe(DeleteProcessRequest.class, DeleteProcessRequest.parser(), callback);
  }

  @Override
  public LanceProcessCreatedResponse createLanceProcess(
      CreateLanceProcessRequest createLanceProcessRequest) throws ResponseException {
    return messageInterface
        .call(createLanceProcessRequest, LanceProcessCreatedResponse.class, timeout);
  }

  @Override
  public void createLanceProcessAsync(CreateLanceProcessRequest createLanceProcessRequest,
      ResponseCallback<LanceProcessCreatedResponse> callback) {
    messageInterface
        .callAsync(createLanceProcessRequest, LanceProcessCreatedResponse.class, callback);
  }

  @Override
  public void subscribeCreateLanceProcessRequest(
      MessageCallback<CreateLanceProcessRequest> callback) {
    messageInterface
        .subscribe(CreateLanceProcessRequest.class, CreateLanceProcessRequest.parser(), callback);
  }

  @Override
  public void deleteLanceProcessAsync(DeleteLanceProcessRequest deleteLanceProcessRequest,
      ResponseCallback<LanceProcessDeletedResponse> callback) {
    messageInterface
        .callAsync(deleteLanceProcessRequest, LanceProcessDeletedResponse.class, callback);
  }

  @Override
  public void subscribeDeleteLanceProcessRequest(
      MessageCallback<DeleteLanceProcessRequest> callback) {
    messageInterface
        .subscribe(DeleteLanceProcessRequest.class, DeleteLanceProcessRequest.parser(), callback);
  }

  @Override
  public SparkProcessCreatedResponse createSparkProcess(
      CreateSparkProcessRequest createSparkProcessRequest) throws ResponseException {
    return messageInterface
        .call(createSparkProcessRequest, SparkProcessCreatedResponse.class, timeout);
  }

  @Override
  public void createSparkProcessAsync(CreateSparkProcessRequest createSparkProcessRequest,
      ResponseCallback<SparkProcessCreatedResponse> callback) {
    messageInterface
        .callAsync(createSparkProcessRequest, SparkProcessCreatedResponse.class, callback);

  }

  @Override
  public void subscribeCreateSparkProcessRequest(
      MessageCallback<CreateSparkProcessRequest> callback) {
    messageInterface
        .subscribe(CreateSparkProcessRequest.class, CreateSparkProcessRequest.parser(), callback);
  }

  @Override
  public SparkClusterCreatedResponse createSparkCluster(
      CreateSparkClusterRequest createSparkClusterRequest) throws ResponseException {
    return messageInterface
        .call(createSparkClusterRequest, SparkClusterCreatedResponse.class, timeout);
  }

  @Override
  public void createSparkClusterAsync(
      CreateSparkClusterRequest createSparkClusterRequest,
      ResponseCallback<SparkClusterCreatedResponse> callback) {

    messageInterface
        .callAsync(createSparkClusterRequest, SparkClusterCreatedResponse.class, callback);

  }

  @Override
  public void subscribeCreateSparkClusterRequest(
      MessageCallback<CreateSparkClusterRequest> callback) {

    messageInterface
        .subscribe(CreateSparkClusterRequest.class, CreateSparkClusterRequest.parser(), callback);

  }

  @Override
  public FaasProcessCreatedResponse createFaasProcess(
      Process.CreateFaasProcessRequest createFaasProcessRequest) throws ResponseException {
    return messageInterface
        .call(createFaasProcessRequest, FaasProcessCreatedResponse.class, timeout);
  }

  @Override
  public void createFaasProcessAsync(Process.CreateFaasProcessRequest createFaasProcessRequest,
      ResponseCallback<Process.FaasProcessCreatedResponse> callback) {
    messageInterface
        .callAsync(createFaasProcessRequest, FaasProcessCreatedResponse.class, callback);
  }

  @Override
  public void subscribeCreateFaasProcessRequest(
      MessageCallback<Process.CreateFaasProcessRequest> callback) {
    messageInterface
        .subscribe(CreateFaasProcessRequest.class, CreateFaasProcessRequest.parser(), callback);
  }

  @Override
  public void subscribeSchedule(MessageCallback<CreateScheduleRequest> callback) {
    messageInterface
        .subscribe(CreateScheduleRequest.class, CreateScheduleRequest.parser(), callback);
  }

  @Override
  public void announceProcessEvent(ProcessEvent processEvent) {
    messageInterface.publish(processEvent);
  }

  @Override
  public void subscribeProcessEvent(MessageCallback<ProcessEvent> callback) {
    messageInterface.subscribe(ProcessEvent.class, ProcessEvent.parser(), callback);
  }

  @Override
  public ScheduleGraphResponse graph(ScheduleGraphRequest scheduleGraphRequest)
      throws ResponseException {
    return messageInterface.call(scheduleGraphRequest, ScheduleGraphResponse.class, timeout);
  }

  @Override
  public ProcessStatusResponse queryProcessStatus(ProcessStatusQuery processStatusQuery,
      @Nullable Long timeout)
      throws ResponseException {

    if (timeout == null) {
      timeout = this.timeout;
    }

    return messageInterface.call(processStatusQuery, ProcessStatusResponse.class, timeout);
  }

  @Override
  public void announceScheduleEvent(ScheduleEvent scheduleEvent) {
    messageInterface.publish(scheduleEvent);
  }

  @Override
  public ScaleResponse createScale(ScaleRequest scaleRequest) throws ResponseException {
    return messageInterface.call(scaleRequest, ScaleResponse.class, timeout);
  }

  @Override
  public void createScaleRequestAsync(ScaleRequest scaleRequest,
      ResponseCallback<ScaleResponse> callback) {

    messageInterface.callAsync(scaleRequest, ScaleResponse.class, callback);

  }

  @Override
  public void subscribeScaleRequest(MessageCallback<ScaleRequest> callback) {
    messageInterface.subscribe(ScaleRequest.class, ScaleRequest.parser(), callback);
  }


}
