package de.uniulm.omi.cloudiator.util.stateMachine;

import java.util.Optional;
import java.util.Set;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransitionGraph<O extends Stateful<S>, S extends State> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TransitionGraph.class);
  private final DefaultDirectedGraph<S, Transition> graph;

  public static <T extends Stateful<U>, U extends State> TransitionGraph<T, U> of(
      Set<Transition<T, U>> transitions) {
    return new TransitionGraph<>(transitions);
  }

  private TransitionGraph(Set<Transition<O, S>> transitions) {

    graph = new DefaultDirectedGraph<>(
        Transition.class);

    for (Transition<O, S> transition : transitions) {
      graph.addVertex(transition.from());
      graph.addVertex(transition.to());
      graph.addEdge(transition.from(), transition.to(), transition);
    }

    LOGGER.debug(String.format("Build transition graph %s.", graph));

  }

  public Optional<GraphPath<S, Transition<O, S>>> shortestPath(S source, S sink) {
    //noinspection unchecked
    return Optional.ofNullable(new DijkstraShortestPath(graph).getPath(source, sink));
  }


}
