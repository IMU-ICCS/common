package de.uniulm.omi.cloudiator.util.stateMachine;

public interface StateMachineHook<O extends Stateful<S>, S extends State> {

  void pre(O object, S to);

  void post(S from, O object);

}
