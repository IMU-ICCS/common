package de.uniulm.omi.cloudiator.util.stateMachine;

public interface ErrorAwareStateMachine<O extends Stateful<T>, T extends State> extends
    StateMachine<O, T> {

  O fail(O object, Object[] arguments, Throwable t);
}
