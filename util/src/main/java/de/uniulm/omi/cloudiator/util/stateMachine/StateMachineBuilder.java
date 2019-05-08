package de.uniulm.omi.cloudiator.util.stateMachine;

import java.util.HashSet;

public class StateMachineBuilder<O extends Stateful<S>, S extends State> {

  private HashSet<Transition<O, S>> transitions;
  private HashSet<StateMachineHook<O, S>> hooks;
  private ErrorTransition<O, S> errorTransition;

  public static <T extends Stateful<U>, U extends State> StateMachineBuilder builder() {
    return new StateMachineBuilder<T, U>();
  }

  private StateMachineBuilder() {
    transitions = new HashSet<>();
    hooks = new HashSet<>();
  }

  public StateMachineBuilder<O, S> addTransition(Transition<O, S> transition) {
    transitions.add(transition);
    return this;
  }

  public StateMachineBuilder<O, S> addHook(StateMachineHook<O, S> hook) {
    hooks.add(hook);
    return this;
  }

  public StateMachineBuilder<O, S> errorTransition(ErrorTransition<O, S> errorTransition) {
    this.errorTransition = errorTransition;
    return this;
  }

  public ErrorAwareStateMachine<O, S> build() {
    return new StateMachineImpl<>(transitions, hooks, errorTransition);
  }


}
