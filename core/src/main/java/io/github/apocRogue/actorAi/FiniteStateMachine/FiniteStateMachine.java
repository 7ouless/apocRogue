package io.github.apocRogue.actorAi.FiniteStateMachine;

public class FiniteStateMachine<T> {
    private T actor;
    private State<T> currentState;

    public FiniteStateMachine(T actor, State<T> initialState) {
        this.actor = actor;
        currentState = initialState;
        currentState.enter(actor);
    }

    public void update(float delta) {
        if (currentState != null) {
            currentState.update(actor, delta);
        }
    }

    public void changeState(State<T> newState) {
        if (currentState != null) {
            currentState.exit(actor);
        }
        currentState = newState;
        currentState.enter(actor);
    }

    public State<T> getCurrentState() {
        return currentState;
    }
}
