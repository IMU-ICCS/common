package de.uniulm.omi.cloudiator.util.stateMachine;

public interface StateMachine<O extends Stateful<S>, S extends State> {

  O apply(O object, S to, Object[] arguments);
}
