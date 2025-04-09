
package io.github.apocRogue.actors.playerEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Actor;

import io.github.apocRogue.actors.mapEntities.ChestActor;
import io.github.apocRogue.actors.useClasses.ItemActor;
import io.github.apocRogue.globals.getters.ObstacleGetters;
import io.github.apocRogue.globals.movementProcesses.StepUpProcessor;
import io.github.apocRogue.globals.physics.PhysicalActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;
import io.github.apocRogue.inventory.Inventory;

public class PlayerActor extends PhysicalActor {

    private static final float STEP_HEIGHT = 32f;
    private static final float PLAYER_WIDTH = 120;
    private static final float PLAYER_HEIGHT = 180f;

    private float jumpPower = 900f;
    private float friction = 0.95f;
    private boolean facingRight = true;

    private float dashSpeed = 1500f;
    private float dashDuration = 0.15f;
    private float dashTimer = 0f;
    private boolean isDashing = false;

    private Inventory inventory;
    private float timeCounter = 0f;
    private StatsComponent stats;

    private Animation<TextureRegion> runRightAnim, runLeftAnim;
    private Animation<TextureRegion> jumpRightAnim, jumpLeftAnim;
    private Animation<TextureRegion> idleRightAnim, idleLeftAnim;
    private Animation<TextureRegion> currentAnimation;
    private TextureRegion currentFrame;
    private float animationTimer = 0f;

    private final Texture runSheet = new Texture(Gdx.files.internal("ui/Run.png"));
    private final Texture jumpSheet = new Texture(Gdx.files.internal("ui/Jump.png"));
    private final Texture idleSheet = new Texture(Gdx.files.internal("ui/Idle.png"));

    public PlayerActor() {
        super(createTransparentTexture());
        System.out.println("✅ PlayerActor created");
        stats = new StatsComponent(100, 100, 10, 2, 1200, 2, 2, 10);

        runRightAnim = createAnimation(runSheet, 6, false);
        runLeftAnim = createAnimation(runSheet, 6, true);
        jumpRightAnim = createAnimation(jumpSheet, 8, false);  // 8 frames
        jumpLeftAnim = createAnimation(jumpSheet, 8, true);    // 8 frames
        idleRightAnim = createAnimation(idleSheet, 4, false);
        idleLeftAnim = createAnimation(idleSheet, 4, true);

        currentAnimation = idleRightAnim;
        currentFrame = currentAnimation.getKeyFrame(0);
        setSize(PLAYER_WIDTH, PLAYER_HEIGHT);
    }

    private static Texture createTransparentTexture() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        return new Texture(pixmap);
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, boolean flipX) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        int frameWidth = sheet.getWidth() / frameCount;
        for (int i = 0; i < frameCount; i++) {
            TextureRegion frame = new TextureRegion(sheet, i * frameWidth, 0, frameWidth, sheet.getHeight());
            if (flipX) frame.flip(true, false);
            frames[i] = frame;
        }
        Animation<TextureRegion> anim = new Animation<>(0.09f, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        return anim;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        GravitySystem.applyGravityAndPhysics(this, delta, 1f);
        timeCounter += delta;

        if (stepCooldown > 0f) stepCooldown -= delta;
        if (!isSteppingUp && isOnGround && stepCooldown <= 0f) StepUpProcessor.attemptStepUp(this);

        if (isSteppingUp) {
            float currentY = getY();
            float distance = Math.abs(stepTargetY - currentY);
            setY(MathUtils.lerp(currentY, stepTargetY, 0.09f));
            velocityY = 0f;
            isOnGround = true;
            if (distance < 1f) {
                setY(stepTargetY);
                isSteppingUp = false;
                stepCooldown = 0.1f;
            }
        }

        handleChestInteraction();
        handleItemPickups();

        if (!isDashing) velocityX *= friction;

        handleHorizontalMovement(delta);
        handleDash(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            jump();
        }

        setX(getX() + velocityX * delta);
        updateAnimation(delta);
    }

    private void updateAnimation(float delta) {
        Animation<TextureRegion> newAnim;
        if (!isOnGround) {
            newAnim = facingRight ? jumpRightAnim : jumpLeftAnim;
        } else if (Math.abs(velocityX) > 5f) {
            newAnim = facingRight ? runRightAnim : runLeftAnim;
        } else {
            newAnim = facingRight ? idleRightAnim : idleLeftAnim;
        }

        if (newAnim != currentAnimation) {
            currentAnimation = newAnim;
            animationTimer = 0f;
        } else {
            animationTimer += delta;
        }

        currentFrame = currentAnimation.getKeyFrame(animationTimer);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (currentFrame == null) return;
        batch.setColor(1, 1, 1, parentAlpha);
        batch.draw(currentFrame, getX(), getY(), PLAYER_WIDTH, PLAYER_HEIGHT);
    }

    @Override
    public void takeDamage(int amount) {
        stats.takeDamage(amount);
        System.out.println("Damage Taken: " + amount);
    }

    public void jump() {
        if (isOnGround) {
            velocityY = jumpPower;
            isOnGround = false;
        }
    }

    private void handleItemPickups() {
        if (getStage() == null) return;

        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Array<Actor> toRemove = new Array<>();
        Array<Actor> actorsCopy = new Array<>(getStage().getActors());

        for (Actor actor : actorsCopy) {
            if (actor instanceof ItemActor) {
                ItemActor item = (ItemActor) actor;
                if (playerRect.overlaps(item.getBounds())) {
                    boolean success = getInventory().addItem(item.getWeapon());
                    if (success) {
                        toRemove.add(item);
                    } else {
                        float dropX = getX() + getWidth() / 2f - item.getWidth() / 2f;
                        float dropY = getY() + getHeight() / 2f;
                        item.setPosition(dropX, dropY);
                        float horizontalPush = isFacingRight() ? 100f : -100f;
                        item.setVelocity(horizontalPush, 200f);
                    }
                }
            }
        }

        for (Actor a : toRemove) {
            a.remove();
        }
    }

    private void handleChestInteraction() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            float interactRange = 80f;
            for (Actor actor : new Array<>(getStage().getActors())) {
                if (actor instanceof ChestActor) {
                    ChestActor chest = (ChestActor) actor;
                    if (!chest.isOpened()) {
                        float dx = (getX() + getWidth() / 2f) - (chest.getX() + chest.getWidth() / 2f);
                        float dy = (getY() + getHeight() / 2f) - (chest.getY() + chest.getHeight() / 2f);
                        float distSquared = dx * dx + dy * dy;
                        if (distSquared < interactRange * interactRange) {
                            chest.openByInteraction();
                            System.out.println("Chest opened!");
                            break;
                        }
                    }
                }
            }
        }
    }

    private void handleHorizontalMovement(float delta) {
        boolean movingLeft = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean movingRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        float speed = stats.getSpeed();

        if (movingLeft) {
            velocityX -= speed * delta;
            facingRight = false;
        }
        if (movingRight) {
            velocityX += speed * delta;
            facingRight = true;
        }

        velocityX = MathUtils.clamp(velocityX, -speed, speed);
    }

    private void handleDash(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            startDash(-dashSpeed);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            startDash(dashSpeed);
        }

        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0f) endDash();
        }
    }

    private void startDash(float dashVel) {
        isDashing = true;
        dashTimer = dashDuration;
        velocityX = dashVel;
    }

    private void endDash() {
        isDashing = false;
        dashTimer = 0f;
    }

    public StatsComponent getStats() { return stats; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isPlayerDead() { return stats.isDead(); }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public Inventory getInventory() { return inventory; }

    public boolean isOnGround() {
        return isOnGround;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void forceEndJumpAnimation() {
        currentAnimation = facingRight ? idleRightAnim : idleLeftAnim;
        animationTimer = 0f;
    }


}
