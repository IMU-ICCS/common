package de.uniulm.omi.cloudiator.util.stateMachine;

public interface Stateful<S extends State> {

  S state();
  
}
