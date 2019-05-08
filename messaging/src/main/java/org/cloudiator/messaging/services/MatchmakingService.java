package org.cloudiator.messaging.services;

import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestMessage;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestResponse;
import org.cloudiator.messages.entities.Matchmaking.SolutionRequest;
import org.cloudiator.messaging.ResponseCallback;
import org.cloudiator.messaging.ResponseException;

public interface MatchmakingService {

  NodeCandidateRequestResponse requestNodes(NodeCandidateRequestMessage nodeCandidateRequestMessage)
      throws ResponseException;

  void requestNodesAsync(NodeCandidateRequestMessage nodeCandidateRequestMessage,
      ResponseCallback<NodeCandidateRequestResponse> callback);

  MatchmakingResponse requestMatch(MatchmakingRequest matchmakingRequest) throws ResponseException;

  void requestMatchAsync(MatchmakingRequest matchmakingRequest,
      ResponseCallback<MatchmakingResponse> response);

  MatchmakingResponse retrieveSolution(SolutionRequest solutionRequest) throws ResponseException;

}
