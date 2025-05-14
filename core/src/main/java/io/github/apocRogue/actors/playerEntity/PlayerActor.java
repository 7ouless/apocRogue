package io.github.apocRogue.actors.playerEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.useClasses.ItemActor;
import io.github.apocRogue.globals.getters.ObstacleGetters;
import io.github.apocRogue.globals.movementProcesses.StepUpProcessor;
import io.github.apocRogue.globals.physics.*;
import io.github.apocRogue.globals.stats.StatsComponent;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.map.TileActor;

public class PlayerActor extends PhysicalActor {
    private Texture texture;
    private static final float STEP_HEIGHT = 32f;

    private Texture idleTex, attackTex;
    private boolean  isAttacking = false;
    private float    attackTimer = 0f;
    private static final float ATTACK_DURATION = 0.7f;

    public float jumpPower     = 1500f;
    public float friction      = 0.95f;
    public boolean facingRight = true;

    public float dashSpeed     = 1500f;
    public float dashDuration  = 0.15f;
    public float dashTimer     = 0f;
    public boolean isDashing   = false;

    private boolean isKnockedback   = false;
    private float   knockbackTimer  = 0f;
    private static final float KNOCKBACK_DURATION = 0.3f;  // seconds

    private float   flashTimer      = 0f;
    private static final float FLASH_DURATION      = 0.4f;  // seconds

    private Inventory inventory;
    public float timeCounter   = 0f;

    private float katanaCooldownTimer = 0f;
    private boolean isWeaponDashing    = false;

    private StatsComponent stats;

    public PlayerActor(Texture idle, Texture attack) {

        super(idle);
        this.idleTex   = idle;
        this.attackTex = attack;
        this.texture   = idle;
        float scale = 0.055f;
        setSize(texture.getWidth() * scale, texture.getHeight() * scale);
        stats = new StatsComponent(100, 100, 10, 2, 1200, 2, 2, 10, 0);
    }

    public StatsComponent getStats() {
        return stats;
    }

    public void startAttack() {
        isAttacking = true;
        attackTimer = ATTACK_DURATION;
    }

    @Override
    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        System.out.println("Damage Taken " + amount);
        flashTimer = FLASH_DURATION;
    }

    @Override
    public void act(float delta) {
        stats.regenStamina(delta);
        super.act(delta);

        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) {
                isAttacking = false;
            }
        }

        // tick down katana cooldown (start)
        if (katanaCooldownTimer > 0f) {
            katanaCooldownTimer -= delta;
        }

        if (flashTimer > 0f) {
            flashTimer -= delta;
            if (flashTimer < 0f) flashTimer = 0f;
        }

        KnockbackProcessor.updateKnockback(this, delta);

        // movement & dash
        if (!isWeaponDashing) {
            if (!isDashing) velocityX *= friction;
            MovementProcessor.handleHorizontalMovement(this, delta);
            DashProcessor.handleDash(this, delta);
        }

        // gravity & stepping
        GravitySystem.applyGravityAndPhysics(this, delta, 4.2f);

        // interactions
        handleChestInteraction();
        handleItemPickups();

        // second friction pass
        if (!isDashing && !isWeaponDashing) {
            velocityX *= friction;
        }

        // repeat movement & dash
        MovementProcessor.handleHorizontalMovement(this, delta);
        DashProcessor.handleDash(this, delta);

        // jump input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            JumpProcessor.handleJump(this);
        }

        // apply horizontal movement
        setX(getX() + velocityX * delta);

        // tick down katana cooldown (end)
        if (katanaCooldownTimer > 0f) {
            katanaCooldownTimer -= delta;
        }

        timeCounter += delta;
    }


    public void startKnockback() {
        isKnockedback  = true;
        knockbackTimer = KNOCKBACK_DURATION;
    }

    // preserve existing inventory/weapon-dash API
    public void setWeaponDashing(boolean d)   { this.isWeaponDashing = d; }
    public boolean isWeaponDashing()          { return isWeaponDashing; }

    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public Inventory getInventory()               { return inventory; }

    public boolean isFacingRight()              { return facingRight; }
    public boolean isPlayerDead()               { return stats.isDead(); }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        if (flashTimer > 0f) {
            batch.setColor(1f, 0f, 0f, 1f);
        } else {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        Texture current = isAttacking ? attackTex : idleTex;

        float drawX = getX(), drawY = getY(),
            drawW = getWidth(), drawH = getHeight();

        if (!facingRight) {
            drawX += getWidth();
            drawW = -getWidth();
        }

        batch.draw(current, drawX, drawY, drawW, drawH);
        batch.setColor(1f, 1f, 1f, 1f);
    }


    private void handleItemPickups() {
        if (getStage() == null) return;
        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Array<Actor> toRemove = new Array<>();
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof ItemActor) {
                ItemActor item = (ItemActor) actor;
                if (playerRect.overlaps(item.getBounds())) {
                    boolean success = getInventory().addItem(item.getWeapon());
                    if (success) {
                        toRemove.add(item);
                    } else {
                        float dropX = getX() + getWidth()/2f - item.getWidth()/2f;
                        float dropY = getY() + getHeight()/2f;
                        item.setPosition(dropX, dropY);
                        float horizontalPush = facingRight ? 100f : -100f;
                        item.setVelocity(horizontalPush, 200f);
                    }
                }
            }
        }
        for (Actor a : toRemove) a.remove();
    }

    private void handleChestInteraction() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            float interactRange = 80f;
            for (Actor actor : getStage().getActors()) {
                if (actor instanceof ChestActor) {
                    ChestActor chest = (ChestActor) actor;
                    if (!chest.isOpened()) {
                        float dx = (getX() + getWidth()/2f) - (chest.getX() + chest.getWidth()/2f);
                        float dy = (getY() + getHeight()/2f) - (chest.getY() + chest.getHeight()/2f);
                        if (dx*dx + dy*dy < interactRange*interactRange) {
                            chest.openByInteraction();
                            System.out.println("Chest opened!");
                            break;
                        }
                    }
                }
            }
        }
    }
}
