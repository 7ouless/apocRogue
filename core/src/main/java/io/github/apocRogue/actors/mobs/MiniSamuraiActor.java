package io.github.apocRogue.actors.mobs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.FiniteStateMachine.FiniteStateMachine;
import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actorAi.samuraiAI.RoamingState;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.globals.physics.GravitySystem;
import io.github.apocRogue.globals.stats.StatsComponent;

public class MiniSamuraiActor extends EnemyActor {
    // The FSM that manages our states
    private FiniteStateMachine<MiniSamuraiActor> fsm;
    // A target for roaming movement
    private Vector2 roamTarget;
    // Variables used for dash (attack) movement.
    private boolean dashing;
    private Vector2 dashDirection;
    private float dashSpeed = 500f; // units per second during dash
    private float normalSpeed;     // from stats (set in constructor)

    public MiniSamuraiActor(Texture texture, float x, float y) {
        // Note: Here we raise the speed stat to make movement visible.
        super(texture, x, y, new StatsComponent(250, 250, 100, 0, 400, 0, 1, 300, 8));
        // Initialize the FSM with RoamingState as the starting state.
        fsm = new FiniteStateMachine<>(this, new RoamingState());
        // For roaming, we set an initial roam target.
        roamTarget = new Vector2(x, y);
        dashing = false;
        dashDirection = new Vector2(0, 0);
        normalSpeed = getStats().getSpeed();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Apply gravity, etc.
        GravitySystem.applyGravityAndPhysics(this, delta, 1f);
        // Update the FSM.
        fsm.update(delta);
    }

    public void changeState(State<MiniSamuraiActor> newState) {
        fsm.changeState(newState);
    }

    public FiniteStateMachine<MiniSamuraiActor> getStateMachine() {
        return fsm;
    }

    // --- Reusable movement methods ---

    /**
     * Roams toward a roam target. If within a small threshold,
     * picks a new random target nearby.
     */
    public void roam(float delta) {
        Vector2 pos = new Vector2(getX(), getY());
        float distance = pos.dst(roamTarget);
        if (distance < 5f) { // Target reached; pick a new roam target.
            roamTarget = getRandomRoamTarget();
        } else {
            Vector2 direction = roamTarget.cpy().sub(pos).nor();
            // Move using the normal speed stat.
            moveBy(direction.x * normalSpeed * delta, direction.y * normalSpeed * delta);
        }
    }

    private Vector2 getRandomRoamTarget() {
        // For example, choose a random point within a 200-unit radius around the current position.
        float rx = getX() + (float)(Math.random() * 400 - 200);
        float ry = getY() + (float)(Math.random() * 400 - 200);
        return new Vector2(rx, ry);
    }

    /**
     * Detects if the player is close enough to trigger a state change.
     * (Replace this with your actual line-of-sight or proximity test.)
     */
    public boolean detectPlayer() {
        PlayerActor player = findPlayer();
        if (player != null) {
            float dx = (player.getX() + player.getWidth() / 2) - (getX() + getWidth() / 2);
            float dy = (player.getY() + player.getHeight() / 2) - (getY() + getHeight() / 2);
            return Math.sqrt(dx * dx + dy * dy) < 100;
        }
        return false;
    }

    private PlayerActor findPlayer() {
        if (getStage() == null)
            return null;
        for (Actor actor : getStage().getActors()) {
            if (actor instanceof PlayerActor)
                return (PlayerActor) actor;
        }
        return null;
    }

    public void startRoaming() {
        // When entering roaming, pick a random target.
        roamTarget = getRandomRoamTarget();
    }

    public void stopRoaming() {
        // Set roam target to current position so no roaming occurs.
        roamTarget.set(getX(), getY());
    }

    /**
     * Immediately stops any movement.
     */
    public void stopMovement() {
        velocityX = 0;
        velocityY = 0;
    }

    public void playReadyAnimation() {
        // This is where you’d trigger an animation. For now, we print to console.
        System.out.println("Samurai: Ready Up!");
    }

    public void playSlashAnimation() {
        System.out.println("Samurai: SLASH!");
    }

    /**
     * Called when beginning the dash/slash attack. Determines the dash direction.
     */
    public void startDash() {
        // Use the player's current position as the target if available.
        PlayerActor player = findPlayer();
        if (player != null) {
            Vector2 pos = new Vector2(getX(), getY());
            Vector2 target = new Vector2(player.getX(), player.getY());
            dashDirection = target.sub(pos).nor();
        } else {
            // Default dash direction (to the right).
            dashDirection.set(1, 0);
        }
        dashing = true;
    }

    /**
     * Moves the samurai quickly in the determined dash direction.
     */
    public void dashTowardsTarget(float delta) {
        if (dashing) {
            moveBy(dashDirection.x * dashSpeed * delta, dashDirection.y * dashSpeed * delta);
        }
    }

    /**
     * Ends the dash, resetting the dashing flag.
     */
    public void endDash() {
        dashing = false;
    }
}
