package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public interface SamuraiState {
    // Called every frame for the state behavior.
    void update(MiniSamuraiActor samurai, float delta);

    // Optionally, a method to initialize the state.
    void enter(MiniSamuraiActor samurai);

    // And to clean up when exiting the state.
    void exit(MiniSamuraiActor samurai);
}
