package org.cloudiator.messaging.services;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import javax.inject.Named;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestMessage;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestResponse;
import org.cloudiator.messages.entities.Matchmaking.SolutionRequest;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.ResponseException;

public class MatchmakingServiceImpl implements MatchmakingService {

  private final MessageInterface messageInterface;

  @Inject(optional = true)
  @Named("responseTimeout")
  private long timeout = 20000;

  @Inject
  public MatchmakingServiceImpl(MessageInterface messageInterface) {
    checkNotNull(messageInterface, "messageInterface is null");
    this.messageInterface = messageInterface;
  }


  @Override
  public NodeCandidateRequestResponse requestNodes(
      NodeCandidateRequestMessage nodeCandidateRequestMessage) throws ResponseException {

    checkNotNull(nodeCandidateRequestMessage, "nodeCandidateRequestMessage is null");

    return messageInterface
        .call(nodeCandidateRequestMessage, NodeCandidateRequestResponse.class, timeout);
  }

  @Override
  public void requestNodesAsync(NodeCandidateRequestMessage nodeCandidateRequestMessage,
      ResponseCallback<NodeCandidateRequestResponse> callback) {
    messageInterface
        .callAsync(nodeCandidateRequestMessage, NodeCandidateRequestResponse.class, callback);
  }

  @Override
  public MatchmakingResponse requestMatch(MatchmakingRequest matchmakingRequest)
      throws ResponseException {
    return messageInterface.call(matchmakingRequest, MatchmakingResponse.class);
  }

  @Override
  public void requestMatchAsync(MatchmakingRequest matchmakingRequest,
      ResponseCallback<MatchmakingResponse> response) {
    messageInterface.callAsync(matchmakingRequest, MatchmakingResponse.class, response);
  }

  @Override
  public MatchmakingResponse retrieveSolution(SolutionRequest solutionRequest)
      throws ResponseException {
    return messageInterface.call(solutionRequest, MatchmakingResponse.class, timeout);
  }

}
