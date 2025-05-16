package io.github.apocRogue.actors.attackEntity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.weapons.MeleeAttackActor;

public class SlashActor extends MeleeAttackActor {

    public SlashActor(Texture texture, PlayerActor player, int damage, Stage stage) {
        super(texture, player, damage, stage);
    }
}
