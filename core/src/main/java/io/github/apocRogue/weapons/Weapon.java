package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.attackEntity.SlashActor;
import io.github.apocRogue.actors.attackEntity.ArrowActor;

public class Weapon {
    private String name;
    private int damage;
    private Texture texture;
    private boolean projectileType;
    private int projectileValue; // can be null
    private String ammoTexture;

    public Weapon(String name, int damage, Texture texture, boolean projectileType, int projectileValue, String ammoTexture) {
        this.name = name;
        this.damage = damage;
        this.texture = texture;
        this.projectileType = projectileType;
        this.projectileValue = projectileValue;
        this.ammoTexture = ammoTexture;
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
    public int getProjectileValue() {
        return projectileValue;
    }
    public String getAmmoTexture() {
        return ammoTexture;
    }

    public void use(PlayerActor player, Stage stage) {
        int wepDamage = getDamage(); // e.g. 10
        if (projectileType) {
            // Ranged: create arrow
            Texture arrowTexture = new Texture(getAmmoTexture());
            Vector2 target = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            ArrowActor arrow = new ArrowActor(
                arrowTexture,
                player.getX(), player.getY() + 20,
                target.x, target.y,
                stage,
                wepDamage,
                this.getProjectileValue()
            );
            stage.addActor(arrow);
        } else {
            // Melee: create slash
            Texture slashTexture = new Texture(getAmmoTexture());
            SlashActor slash = new SlashActor(slashTexture, player, wepDamage, stage);
            stage.addActor(slash);
        }
    }

}
