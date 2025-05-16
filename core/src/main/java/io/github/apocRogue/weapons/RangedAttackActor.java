package io.github.apocRogue.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.SoundPhysics;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;


public abstract class RangedAttackActor extends BaseAttackActor {
    protected Vector2 velocity = new Vector2(0, 0);
    protected float gravity = -300f;
    protected int noiseLevel;
    private Weapon weapon;
    private float timeSinceLastSound = 0f;  //for periodic flight sound
    private PlayerActor player;


    public RangedAttackActor(PlayerActor player, Texture texture, int damage, Stage stage, Weapon weapon, Vector2 direction) {
        super(texture, damage, stage);
        this.weapon = weapon;
        this.player = player;
        setPosition(player.getX(), player.getY());
        this.velocity = direction.nor().scl(500f);
        this.noiseLevel = (weapon != null ? weapon.getNoiseLevel() : 0);
    }
    @Override
    public void act(float delta) {
        super.act(delta);

        velocity.y += gravity * delta;

        setPosition(getX() + velocity.x * delta, getY() + velocity.y * delta);

        if (velocity.len2() > 0) {
            setRotation(velocity.angleDeg());
        }
        timeSinceLastSound += delta;
        if (timeSinceLastSound >= 0.05f) {
            timeSinceLastSound -= 0.05f;
            SoundPhysics.emitSound(
                new Vector2(getX(), getY()),
                weapon.getFlightNoiseIntensity(),
                weapon.getFlightNoiseRadius(),
                SoundPhysics.SoundType.PROJECTILE_FLIGHT,
                player.getStage()
            );
        }
        checkCollisionWithTile();
    }

    protected void checkCollisionWithTile() {
        if (stage == null) return;

        Array<Actor> actorsCopy = new Array<>(stage.getActors());

        Rectangle projectileRect = new Rectangle(getX(), getY(), getWidth(), getHeight());

        for (Actor actor : actorsCopy) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (weapon != null) {
                    SoundPhysics.emitSound(
                        new Vector2(getX(), getY()),
                        weapon.getImpactNoiseIntensity(),
                        weapon.getImpactNoiseRadius(),
                        SoundPhysics.SoundType.PROJECTILE_IMPACT,
                        player.getStage()
                            );
                    }
                    remove();
                    break;
                }
            }
        }
    }




