package io.github.apocRogue.actorAi.FiniteStateMachine;

public interface State<T> {
    /**
     * Called when the actor enters this state.
     */
    void enter(T actor);

    /**
     * Called each frame the state is active.
     */
    void update(T actor, float delta);

    /**
     * Called when the state is exited.
     */
    void exit(T actor);
}
