package io.github.apocRogue.actorAi.FiniteStateMachine;

public interface State<T> {

    void enter(T actor);


    void update(T actor, float delta);


    void exit(T actor);
}
