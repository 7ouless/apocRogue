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
import io.github.apocRogue.globals.physics.MovementProcessor;
import io.github.apocRogue.globals.physics.DashProcessor;
import io.github.apocRogue.globals.physics.JumpProcessor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.globals.stats.StatsComponent;
import io.github.apocRogue.inventory.gameinventory.Inventory;
import io.github.apocRogue.map.TileActor;

public class PlayerActor extends PhysicalActor {
    private Texture texture;
    private static final float STEP_HEIGHT = 32f;

    public float jumpPower     = 900f;
    public float friction      = 0.95f;
    public boolean facingRight = true;

    public float dashSpeed     = 1500f;
    public float dashDuration  = 0.15f;
    public float dashTimer     = 0f;
    public boolean isDashing   = false;

    private Inventory inventory;
    public float timeCounter   = 0f;

    private float katanaCooldownTimer = 0f;
    private boolean isWeaponDashing    = false;

    private StatsComponent stats;

    public PlayerActor(Texture texture) {
        super(texture);
        this.texture = texture;
        setSize(texture.getWidth(), texture.getHeight());
        stats = new StatsComponent(100, 100, 10, 2, 1200, 2, 2, 10, 0);
    }

    public StatsComponent getStats() {
        return stats;
    }

    @Override
    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        System.out.println("Damage Taken " + amount);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // tick down katana cooldown (start)
        if (katanaCooldownTimer > 0f) {
            katanaCooldownTimer -= delta;
        }

        // movement & dash
        if (!isWeaponDashing) {
            if (!isDashing) velocityX *= friction;
            MovementProcessor.handleHorizontalMovement(this, delta);
            DashProcessor.handleDash(this, delta);
        }

        // gravity & stepping
        GravitySystem.applyGravityAndPhysics(this, delta, 1f);
        StepUpProcessor.attemptStepUp(this);

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

    // preserve existing inventory/weapon-dash API
    public void setWeaponDashing(boolean d)   { this.isWeaponDashing = d; }
    public boolean isWeaponDashing()          { return isWeaponDashing; }

    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public Inventory getInventory()               { return inventory; }

    public boolean isFacingRight()              { return facingRight; }
    public boolean isPlayerDead()               { return stats.isDead(); }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
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
