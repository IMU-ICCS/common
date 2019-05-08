package de.uniulm.omi.cloudiator.util.stateMachine;

import de.uniulm.omi.cloudiator.util.stateMachine.Transition.TransitionAction;

public class TransitionBuilder<O extends Stateful<S>, S extends State> {

  private S from;
  private S to;
  private TransitionAction<O> action;

  TransitionBuilder() {
  }

  public TransitionBuilder<O,S> from(S from) {
    this.from = from;
    return this;
  }

  public TransitionBuilder<O,S> to(S to) {
    this.to = to;
    return this;
  }

  public TransitionBuilder<O,S> action(TransitionAction<O> action) {
    this.action = action;
    return this;
  }

  public Transition<O,S> build() {
    return new Transition<>(from, to, action);
  }

}
