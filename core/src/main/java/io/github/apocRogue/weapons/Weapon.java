package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.attackEntity.SlashActor;
import io.github.apocRogue.actors.attackEntity.ArrowActor;
import io.github.apocRogue.globals.physics.SoundPhysics;

public class Weapon {
    private String name;
    private int damage;
    private Texture texture;
    private boolean projectileType;
    private int projectileValue; // can be null
    private String ammoTexture;
    private int animationSpeed;
    private int noiseLevel;
    private float dashSpeed;
    private float dashDuration;
    private float dashCooldown;

    public Weapon(String name, int damage, Texture texture,
                  boolean projectileType, int projectileValue,
                  String ammoTexture, int animationSpeed, int noiseLevel,
                  float dashSpeed, float dashDuration, float dashCooldown) {
        this.name = name;
        this.damage = damage;
        this.texture = texture;
        this.projectileType = projectileType;
        this.projectileValue = projectileValue;
        this.ammoTexture = ammoTexture;
        this.animationSpeed = animationSpeed;
        this.noiseLevel = noiseLevel;
        this.dashSpeed       = dashSpeed;
        this.dashDuration    = dashDuration;
        this.dashCooldown    = dashCooldown;

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

    public float getDashSpeed()    { return dashSpeed; }
    public float getDashDuration() { return dashDuration; }
    public float getDashCooldown() { return dashCooldown; }


    public void use(PlayerActor player, Stage stage) {
        int wepDamage = getDamage(); // e.g. 10
        Vector2 target = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        Vector2 direction = new Vector2(target.x - player.getX(), target.y - player.getY()).nor();

        if ("Katana".equals(name)) {
            // 1) spawn the dash-attack actor
            DashAttackActor dash = new DashAttackActor(
                getTexture(),
                player,
                getDamage(),
                stage,
                getDashSpeed(),
                getDashDuration()
            );
            stage.addActor(dash);
            return;
        }


        else if (projectileType) {
            // Ranged: create arrow
            Texture arrowTexture = new Texture(getAmmoTexture());
            target = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            // Pass noise level from the weapon into the ArrowActor.
            ArrowActor arrow = new ArrowActor(
                arrowTexture,
                player.getX(), player.getY() + 20,
                target.x, target.y,
                stage,
                wepDamage,
                this.getProjectileValue(),
                this.noiseLevel,
                this,
                player,
                direction

            );
            SoundPhysics.emitSound(
                new Vector2(player.getX(), player.getY()),
                getMeleeNoiseIntensity(),
                getMeleeNoiseRadius(),
                SoundPhysics.SoundType.PROJECTILE_IMPACT,
                player.getStage()
            );
            stage.addActor(arrow);
        } else {
            // Melee: create slash (no noise triggered in melee by default).
            Texture slashTexture = new Texture(getAmmoTexture());
            SlashActor slash = new SlashActor(slashTexture, player, wepDamage, stage);
            SoundPhysics.emitSound(
                new Vector2(player.getX(), player.getY()),
                getMeleeNoiseIntensity(),
                getMeleeNoiseRadius(),
                SoundPhysics.SoundType.MELEE_NOISE,
                player.getStage()
            );
            stage.addActor(slash);
        }
    }

    public float getMuzzleNoiseIntensity() {
        return noiseLevel * 1.0f;
    }
    public float getMuzzleNoiseRadius() {
        return noiseLevel * 30f;
    }
    public float getFlightNoiseIntensity() {
        return noiseLevel * 0.1f;
    }
    public float getFlightNoiseRadius() {
        return noiseLevel * 20f;
    }
    public float getImpactNoiseIntensity() {
        return noiseLevel * 1000f;
    }
    public float getImpactNoiseRadius() {
        return noiseLevel * 100f;
    }
    public float getMeleeNoiseIntensity() {
        return noiseLevel * 1.0f;
    }
    public float getMeleeNoiseRadius() {
        return noiseLevel * 50f;
    }
    public int getNoiseLevel() {
        return noiseLevel;
    }

}
