package de.uniulm.omi.cloudiator.util.stateMachine;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

public class ErrorTransition<O extends Stateful<S>, S extends State> {

  public S errorState() {
    return errorState;
  }

  public interface ErrorTransitionAction<O> {

    O apply(O o, Object[] arguments, @Nullable Throwable t);
  }

  private final S errorState;
  private final ErrorTransitionAction<O> errorTransitionAction;


  ErrorTransition(S errorState,
      ErrorTransitionAction<O> errorTransitionAction) {

    checkNotNull(errorState, "error state is null");
    checkNotNull(errorTransitionAction, "errorTransitionAction is null");

    this.errorState = errorState;
    this.errorTransitionAction = errorTransitionAction;
  }

  O apply(O object, Object[] arguments, @Nullable Throwable t) {
    return errorTransitionAction.apply(object, arguments, t);
  }
}
