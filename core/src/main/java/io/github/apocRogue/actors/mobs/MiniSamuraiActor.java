package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.apocRogue.actorAi.FiniteStateMachine.FiniteStateMachine;
import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actorAi.samuraiAI.RoamingState;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.getters.ObstacleGetters;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.Gdx;

public class MiniSamuraiActor extends EnemyActor {
    private FiniteStateMachine<MiniSamuraiActor> fsm;
    // Roam target is now only an X target (Y remains constant).
    private Vector2 roamTarget;
    private boolean dashing;
    private Vector2 dashDirection;
    private float dashSpeed = 20f;
    private float normalSpeed;
    private boolean facingRight = true;
    private final Texture normalTexture;
    private final Texture attackTexture;
    private Texture activeTexture;
    private static final float SCALE = 0.12f;
    private float previousX;
    private static final float DASH_COOLDOWN = 3f;
    private float timeSinceLastDash = DASH_COOLDOWN;

    public MiniSamuraiActor(Texture normalTexture,
                            Texture attackTexture,
                            float x, float y) {
        super(normalTexture, x, y,
            new StatsComponent(250, 250, 100, 0, 400, 0, 1, 1000, 8)
        );
        this.normalTexture = normalTexture;
        this.attackTexture = attackTexture;
        this.activeTexture = normalTexture;
        this.previousX = x;

        setPosition(x, y);
        setSize(normalTexture.getWidth()* SCALE, normalTexture.getHeight()* SCALE);

        fsm = new FiniteStateMachine<>(this, new RoamingState());
        roamTarget = new Vector2(x, y);
        dashing = false;
        dashDirection = new Vector2(0, 0);
        normalSpeed = getStats().getSpeed();
    }

    public void act(float delta) {
        super.act(delta);

        float dx = getX() - previousX;
        if (dx < 0)      facingRight = false;
        else if (dx > 0) facingRight = true;
        previousX = getX();

        GravitySystem.applyGravityAndPhysics(this, delta, 1f);
        timeSinceLastDash = Math.min(DASH_COOLDOWN, timeSinceLastDash + delta);
        fsm.update(delta);
    }

    public void setAttackMode(boolean attacking) {
        activeTexture = attacking ? attackTexture : normalTexture;
    }


    public void changeState(State<MiniSamuraiActor> newState) {
        fsm.changeState(newState);
    }
    public FiniteStateMachine<MiniSamuraiActor> getStateMachine() {
        return fsm;
    }


    public void roam(float delta) {
        Vector2 pos = new Vector2(getX(), getY());
        float distance = pos.dst(roamTarget);
        if (distance < 5f) {
            roamTarget = getRandomRoamTarget();

        } else {
            Vector2 direction = roamTarget.cpy().sub(pos).nor();
            // Ensure only horizontal movement.
            direction.y = 0;

            moveBy(direction.x * normalSpeed * delta, 0);
        }
    }

    private Vector2 getRandomRoamTarget() {
        Vector2 bounds = getCurrentPlatformBounds();
        float newX = bounds.x + (float)Math.random() * (bounds.y - bounds.x);
        return new Vector2(newX, getY());
    }


    public Vector2 getCurrentPlatformBounds() {
        float tileSize = ObstacleGetters.getStandardTileSize();
        // Get the tile in which the center lies.
        float centerX = getX() + getWidth() / 2;
        int tileColumn = (int)(centerX / tileSize);
        // Assume the platform spans, say, 3 tiles for a wider roaming area.
        float minX = (tileColumn - 1) * tileSize;
        float maxX = (tileColumn + 2) * tileSize;
        return new Vector2(minX, maxX);
    }

    public boolean detectPlayer() {
        PlayerActor player = findPlayer();
        if (player == null) return false;

        float myCenterX = getX() + getWidth()/2f;
        float playerCenterX = player.getX() + player.getWidth()/2f;
        float dx = playerCenterX - myCenterX;
        float distance = Math.abs(dx);

        // just use a simple vision radius (e.g. 200 units)
        if (distance < 200f) {
            // face the player
            facingRight = dx > 0;
            return true;
        }
        return false;
    }

    public void startRoaming() {
        roamTarget = getRandomRoamTarget();
    }
    public void stopRoaming() {
        roamTarget.set(getX(), getY());
    }
    public void stopMovement() {
        velocityX = 0;
        velocityY = 0;
    }
    public void playReadyAnimation() {
        System.out.println("Samurai: Ready Up!");
    }
    public void playSlashAnimation() {
        System.out.println("Samurai: SLASH!");
    }

    public void startDash() {
        PlayerActor player = findPlayer();
        if (player != null) {
            Vector2 pos = new Vector2(getX(), getY());
            Vector2 target = new Vector2(player.getX(), player.getY());
            dashDirection = target.sub(pos).nor();
        } else {
            dashDirection.set(1, 0);
        }
        dashDirection.y = 0;
        dashDirection.nor();
        dashing = true;
    }

    public void dashTowardsTarget(float delta) {
        if (dashing) {
            float newX = getX() + dashDirection.x * dashSpeed * delta;
            Vector2 bounds = getCurrentPlatformBounds();
            newX = Math.max(bounds.x, Math.min(newX, bounds.y));
            setX(newX);
        }
    }
    public void endDash() {
        dashing = false;
    }

    public boolean canDash() {
        return timeSinceLastDash >= DASH_COOLDOWN;
    }

    public void resetDashCooldown() {
        timeSinceLastDash = 0f;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isPlayerInCameraView() {
        PlayerActor player = findPlayer();
        if (player == null) return false;

        Stage stage = getStage();
        if (stage == null) return false;

        Camera cam = stage.getCamera();
        if (!(cam instanceof OrthographicCamera)) {
            // if you ever use a different camera type, handle it here
            return false;
        }
        OrthographicCamera ocam = (OrthographicCamera)cam;

        // world-units half-width/height of what the camera sees
        float halfW = (ocam.viewportWidth * ocam.zoom) / 2f;
        float halfH = (ocam.viewportHeight * ocam.zoom) / 2f;

        // camera center in world-coords
        float camX = ocam.position.x;
        float camY = ocam.position.y;

        // player center in world-coords
        float pX = player.getX() + player.getWidth()  / 2f;
        float pY = player.getY() + player.getHeight() / 2f;

        return (pX >= camX - halfW && pX <= camX + halfW)
            && (pY >= camY - halfH && pY <= camY + halfH);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(
            activeTexture,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            facingRight ? 1f : -1f, 1f,
            getRotation(),
            0, 0,
            activeTexture.getWidth(), activeTexture.getHeight(),
            false, false
        );
    }

}
