package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.PlayerActor;
import io.github.apocRogue.actors.SlashActor;
import io.github.apocRogue.actors.ArrowActor;

public class Weapon {
    private String name;
    private int damage;
    private Texture texture;
    private boolean projectileType;

    public Weapon(String name, int damage, Texture texture, boolean projectileType) {
        this.name = name;
        this.damage = damage;
        this.texture = texture;
        this.projectileType = projectileType;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isProjectileType() {
        return projectileType;
    }

    public void use(PlayerActor player, Stage gameStage) {
        if (projectileType) {
            // For projectiles, shoot in the direction of the cursor.
            Texture arrowTexture = new Texture("ui/arrow.png");
            // Get target from cursor, convert screen coordinates to stage coordinates.
            Vector2 target = gameStage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            // Spawn the arrow starting at the player's position (or an offset)
            ArrowActor arrow = new ArrowActor(arrowTexture, player.getX(), player.getY() + 20, target.x, target.y, gameStage);
            gameStage.addActor(arrow);
        } else {
            // For melee (slash) type, use the player's facing direction.
            Texture slashTexture = new Texture(Gdx.files.internal("ui/slash.png"));
            // Pass the player actor so the SlashActor can follow the player's current facing direction.
            SlashActor slash = new SlashActor(slashTexture, player, gameStage);
            gameStage.addActor(slash);
        }
    }
}
