// Weapon.java
package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.attackEntity.*;
import io.github.apocRogue.globals.physics.SoundPhysics;
import java.util.*;

/**
 * A fully-realized in-game weapon, constructed from
 *   1) its encoded ID
 *   2) static WeaponTypeInfo (name/textures/projectile-flag)
 *   3) a backend-provided stats map
 */
public class Weapon {
    private final String itemCode;
    private final WeaponTypeInfo typeInfo;
    private final Map<String,Integer> stats;
    private final Texture weaponTexture;
    private final Texture ammoTexture;

    public Weapon(
        String itemCode,
        WeaponTypeInfo typeInfo,
        Map<String,Integer> stats,
        Texture weaponTexture,
        Texture ammoTexture
    ) {
        this.itemCode      = itemCode;
        this.typeInfo      = typeInfo;
        this.weaponTexture = weaponTexture;
        this.ammoTexture   = ammoTexture;

        // Populate in fixed order using your StatKeys.ALL
        this.stats = new LinkedHashMap<>();
        for (String key : StatKeys.ALL) {
            this.stats.put(key, stats.getOrDefault(key, 0));
        }
    }

    // Basic getters
    public String getItemCode()             { return itemCode; }
    public String getName()                 { return typeInfo.getName(); }
    public Texture getTexture()             { return weaponTexture; }
    public boolean isProjectileType()       { return typeInfo.isProjectileType(); }
    public int    getStat(String key)       { return stats.getOrDefault(key, 0); }
    public int    getDamage()               { return getStat("damage"); }
    public int    getProjectileValue()      { return getStat("projectileValue"); }
    public int    getAnimationSpeed()       { return getStat("animationSpeed"); }
    public int    getNoiseLevel()           { return getStat("noiseLevel"); }
    public float  getDashSpeed()            { return getStat("dashSpeed"); }
    public float  getDashDuration()         { return getStat("dashDuration")  / 100f; }
    public float  getDashCooldown()         { return getStat("dashCooldown")  / 10f; }

    // Noise multipliers
    private float noise(int level, float factor) {
        return level * factor;
    }
    public float getMuzzleNoiseRadius()     { return noise(getNoiseLevel(), 30f); }
    public float getMuzzleNoiseIntensity()  { return noise(getNoiseLevel(), 1f); }
    public float getFlightNoiseRadius()     { return noise(getNoiseLevel(), 20f); }
    public float getFlightNoiseIntensity()  { return noise(getNoiseLevel(), 0.1f); }
    public float getImpactNoiseRadius()     { return noise(getNoiseLevel(), 100f); }
    public float getImpactNoiseIntensity()  { return noise(getNoiseLevel(), 1000f); }
    public float getMeleeNoiseRadius()      { return noise(getNoiseLevel(), 50f); }
    public float getMeleeNoiseIntensity()   { return noise(getNoiseLevel(), 1f); }

    /** Fires or swings this weapon in-world. */
    public void use(PlayerActor player, Stage stage) {
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 aim   = stage.screenToStageCoordinates(mouse);
        Vector2 dir   = aim.sub(player.getX(), player.getY()).nor();

        if (isProjectileType()) {
            ArrowActor arrow = new ArrowActor(
                ammoTexture,
                player.getX(), player.getY() + 20,
                aim.x, aim.y,
                stage,
                getDamage(),
                getProjectileValue(),
                getNoiseLevel(),
                this,
                player,
                dir
            );
            SoundPhysics.emitSound(
                new Vector2(player.getX(), player.getY()),
                getMeleeNoiseIntensity(),
                getMeleeNoiseRadius(),
                SoundPhysics.SoundType.PROJECTILE_IMPACT,
                stage
            );
            stage.addActor(arrow);
        } else {
            boolean isKatana = getName().equalsIgnoreCase("Katana")
                || itemCode.startsWith("04");
            if (isKatana && !player.isWeaponDashing()) {
                DashAttackActor dash = new DashAttackActor(
                    ammoTexture,
                    player,
                    getDamage(),
                    stage,
                    getDashSpeed(),
                    getDashDuration()
                );
                stage.addActor(dash);
            } else {
                SlashActor slash = new SlashActor(
                    ammoTexture, player, getDamage(), stage
                );
                SoundPhysics.emitSound(
                    new Vector2(player.getX(), player.getY()),
                    getMeleeNoiseIntensity(),
                    getMeleeNoiseRadius(),
                    SoundPhysics.SoundType.MELEE_NOISE,
                    stage
                );
                stage.addActor(slash);
            }
        }
    }
}
