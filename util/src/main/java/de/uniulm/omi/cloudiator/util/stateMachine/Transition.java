package de.uniulm.omi.cloudiator.util.stateMachine;

import com.google.common.base.MoreObjects;
import java.util.concurrent.ExecutionException;

public class Transition<O extends Stateful<S>, S extends State> {

  public interface TransitionAction<O> {

    O apply(O o, Object[] arguments) throws ExecutionException;

  }

  private final S from;
  private final S to;
  private final TransitionAction<O> action;

  Transition(S from, S to,
      TransitionAction<O> action) {
    this.from = from;
    this.to = to;
    this.action = action;
  }

  public S from() {
    return from;
  }

  public S to() {
    return to;
  }

  O apply(O o, Object[] arguments) throws ExecutionException {
    try {
      return action.apply(o, arguments);
    } catch (ExecutionException e) {
      throw new ExecutionException(e.getCause());
    } catch (Exception e) {
      throw new ExecutionException(e);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("from", from).add("to", to).toString();
  }
}
