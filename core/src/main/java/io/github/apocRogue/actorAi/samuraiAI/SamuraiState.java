package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public interface SamuraiState {
    void update(MiniSamuraiActor samurai, float delta);

    void enter(MiniSamuraiActor samurai);

    void exit(MiniSamuraiActor samurai);
}
