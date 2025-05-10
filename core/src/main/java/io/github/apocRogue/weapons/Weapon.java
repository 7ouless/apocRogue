package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.attackEntity.SlashActor;
import io.github.apocRogue.actors.attackEntity.ArrowActor;
import io.github.apocRogue.globals.physics.SoundPhysics;
import java.util.LinkedHashMap;
import java.util.Map;
import io.github.apocRogue.weapons.DashAttackActor;

public class Weapon {
    private final String id;
    private final Map<String,Integer> stats = new LinkedHashMap<>();

    private String name;
    private int damage;
    private Texture texture;
    private boolean projectileType;
    private int projectileValue;
    private String ammoTexture;
    private int animationSpeed;
    private int noiseLevel;
    private float dashSpeed;
    private float dashDuration;
    private float dashCooldown;

    public Weapon(
        String id,
        String name,
        int damage,
        Texture texture,
        boolean projectileType,
        int projectileValue,
        String ammoTexture,
        int animationSpeed,
        int noiseLevel,
        float dashSpeed,
        float dashDuration,
        float dashCooldown
    ) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.texture = texture;
        this.projectileType = projectileType;
        this.projectileValue = projectileValue;
        this.ammoTexture = ammoTexture;
        this.animationSpeed = animationSpeed;
        this.noiseLevel = noiseLevel;
        this.dashSpeed = dashSpeed;
        this.dashDuration = dashDuration;
        this.dashCooldown = dashCooldown;


        stats.put("damage", damage);
        stats.put("projectileValue", projectileValue);
        stats.put("animationSpeed", animationSpeed);
        stats.put("noiseLevel", noiseLevel);
        stats.put("dashSpeed",    Math.round(dashSpeed));
        stats.put("dashDuration", Math.round(dashDuration * 100));
        stats.put("dashCooldown", Math.round(dashCooldown * 10));
    }


    public String getID() {
        return id;
    }


    public Map<String,Integer> getStats() {
        return stats;
    }

    //your original getters

    public String getName()              { return name; }
    public int    getDamage()            { return damage; }
    public Texture getTexture()          { return texture; }
    public boolean isProjectileType()    { return projectileType; }
    public int    getProjectileValue()   { return projectileValue; }
    public String getAmmoTexture()       { return ammoTexture; }
    public int    getAnimationSpeed()    { return animationSpeed; }
    public int    getNoiseLevel()        { return noiseLevel; }
    public float  getDashSpeed()         { return dashSpeed; }
    public float  getDashDuration()      { return dashDuration; }
    public float  getDashCooldown()      { return dashCooldown; }

    // noise‐helper methods

    public float getMuzzleNoiseIntensity()  { return noiseLevel * 1.0f; }
    public float getMuzzleNoiseRadius()     { return noiseLevel * 30f; }
    public float getFlightNoiseIntensity()  { return noiseLevel * 0.1f; }
    public float getFlightNoiseRadius()     { return noiseLevel * 20f; }
    public float getImpactNoiseIntensity()  { return noiseLevel * 1000f; }
    public float getImpactNoiseRadius()     { return noiseLevel * 100f; }
    public float getMeleeNoiseIntensity()   { return noiseLevel * 1.0f; }
    public float getMeleeNoiseRadius()      { return noiseLevel * 50f; }


    public void use(PlayerActor player, Stage stage) {
        int wepDamage = getDamage();
        Vector2 target = stage.screenToStageCoordinates(
            new Vector2(Gdx.input.getX(), Gdx.input.getY())
        );
        Vector2 direction = new Vector2(
            target.x - player.getX(),
            target.y - player.getY()
        ).nor();

        if (projectileType) {
            Texture arrowTex = new Texture(getAmmoTexture());
            ArrowActor arrow = new ArrowActor(
                arrowTex,
                player.getX(), player.getY() + 20,
                target.x, target.y,
                stage,
                wepDamage,
                getProjectileValue(),
                noiseLevel,
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
        } else {                              // MELEE branch
            boolean isKatana =
                name.equalsIgnoreCase("Katana")   // by name
                    || id.startsWith("04");              // by ID prefix


            if (isKatana) {
                if (player.isWeaponDashing())
                    return;

                DashAttackActor dash = new DashAttackActor(
                    new Texture(getAmmoTexture()),
                    player,
                    getDamage(),
                    stage,
                    getDashSpeed(),
                    getDashDuration()
                );
                stage.addActor(dash); 
                return;
            }
            else {
                Texture slashTex = new Texture(getAmmoTexture());
                SlashActor slash = new SlashActor(slashTex, player, getDamage(), stage);
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
    }
}
