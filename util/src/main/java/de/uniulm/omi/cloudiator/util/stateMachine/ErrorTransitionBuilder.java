package de.uniulm.omi.cloudiator.util.stateMachine;

import de.uniulm.omi.cloudiator.util.stateMachine.ErrorTransition.ErrorTransitionAction;

public class ErrorTransitionBuilder<O extends Stateful<S>, S extends State> {

  private S errorState;
  private ErrorTransitionAction<O> action;

  ErrorTransitionBuilder() {
  }

  public ErrorTransitionBuilder<O, S> errorState(S errorState) {
    this.errorState = errorState;
    return this;
  }

  public ErrorTransitionBuilder<O, S> action(ErrorTransitionAction<O> action) {
    this.action = action;
    return this;
  }

  public ErrorTransition<O, S> build() {
    return new ErrorTransition<>(errorState, action);
  }

}
