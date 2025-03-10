package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
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
        boolean type = projectileType;
        if (projectileType) {
            Texture arrowTexture = new Texture("ui/arrow.png"); // or 1x1 white pixel
            float direction = player.isFacingRight() ? 1f : -1f;
            ArrowActor arrow = new ArrowActor(arrowTexture, player.getX(), player.getY() + 20, direction, gameStage);
            Gdx.app.log("Position ", arrow.getX() + " " + arrow.getY());
            Gdx.app.log("Player Position ", player.getX() + " " + player.getY());
            gameStage.addActor(arrow);
        } else {
           Texture slashTexture = new Texture(Gdx.files.internal("ui/slash.png"));
           float direction = player.isFacingRight() ? 1f : -1f;
           SlashActor slash = new SlashActor(slashTexture, player.getX() + 60, player.getY() + 10, direction, gameStage);
           if (!player.isFacingRight()) {
               slash.setPosition(player.getX() - 60, player.getY() + 10);
           }
           Gdx.app.log("Position ", slash.getX() + " " + slash.getY());
           Gdx.app.log("Player Position ", player.getX() + " " + player.getY());
           gameStage.addActor(slash);
        }
    }
}
