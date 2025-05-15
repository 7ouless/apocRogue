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
    private float dashSpeed = 500f;
    private float normalSpeed;
    private boolean facingRight = true;
    private final Texture normalTexture;
    private final Texture attackTexture;
    private Texture activeTexture;
    private static final float SCALE = 0.5f;


    public MiniSamuraiActor(Texture texture, Texture samuraiAttackTexture, float x, float y) {
        super(texture, x, y,
            new StatsComponent(250, 250, 100, 0, 400, 0, 1, 1000, 8)
        );
        this.normalTexture = texture;
        this.attackTexture = new Texture(Gdx.files.internal("ui/samurai-attack.png"));
        this.activeTexture = normalTexture;

        setPosition(x, y);
        setSize(normalTexture.getWidth()* SCALE, normalTexture.getHeight()* SCALE);

        fsm = new FiniteStateMachine<>(this, new RoamingState());
        roamTarget = new Vector2(x, y);
        dashing = false;
        dashDirection = new Vector2(0, 0);
        normalSpeed = getStats().getSpeed();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        GravitySystem.applyGravityAndPhysics(this, delta, 1f);
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

    /**
     * Roams only horizontally.
     */
    public void roam(float delta) {
        Vector2 pos = new Vector2(getX(), getY());
        float distance = pos.dst(roamTarget);
        System.out.println("Roaming: current pos = " + pos + ", roamTarget = " + roamTarget + ", distance = " + distance);
        if (distance < 5f) {
            roamTarget = getRandomRoamTarget();
            System.out.println("New roam target set: " + roamTarget);
        } else {
            Vector2 direction = roamTarget.cpy().sub(pos).nor();
            // Ensure only horizontal movement.
            direction.y = 0;
            System.out.println("Moving in direction: " + direction);
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
        if (player != null) {
            float dx = (player.getX() + player.getWidth()/2) - (getX() + getWidth()/2);
            // Consider detection range (adjust the threshold as needed)
            double distance = Math.abs(dx);
            Vector2 bounds = getCurrentPlatformBounds();
            float playerCenterX = player.getX() + player.getWidth()/2;
            return (distance < 100 && playerCenterX >= bounds.x && playerCenterX <= bounds.y);
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
    public void draw(Batch batch,float delta) {
        super.act(delta);
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

    public void dispose() {
        super.remove();
        normalTexture.dispose();
        attackTexture.dispose();
    }
}
