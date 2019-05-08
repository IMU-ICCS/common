package de.uniulm.omi.cloudiator.util.stateMachine;

public class Transitions {

  public static <O extends Stateful<S>, S extends State> TransitionBuilder<O, S> transitionBuilder() {
    return new TransitionBuilder<>();
  }

  public static <O extends Stateful<S>, S extends State> ErrorTransitionBuilder<O, S> errorTransitionBuilder() {
    return new ErrorTransitionBuilder<>();
  }

}
