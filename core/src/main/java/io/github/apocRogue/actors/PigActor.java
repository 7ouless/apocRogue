package io.github.apocRogue.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.map.FloorTile;
import io.github.apocRogue.map.HazardTile;
import io.github.apocRogue.map.PlatformTile;
import io.github.apocRogue.map.TileActor;

public class PigActor extends Actor {

    private Texture texture;

    public int velocityX;
    public int velocityY;

    // Physics
    public float gravity = -600f; // Gravity in pixels/sec^2
    public float jumpPower = 200f;
    public boolean isOnGround = false;

    public PigActor(Texture texture, float x, float y) {
        this.texture = texture;
        setPosition(x, y);
        setSize(texture.getWidth() * 3, texture.getHeight() * 3);
    }

    public void act(float delta) {
        super.act(delta);
        velocityY += gravity * delta;
        handleTileCollisions(delta);

        setX(getX() + velocityX * delta);
        setY(getY() + velocityY * delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    public void handleTileCollisions(float delta) {
        if (getStage() == null) return;

        isOnGround = false; // reset each frame

        // Compute "old" positions based on current velocities
        float oldX = getX() - velocityX * delta;
        float oldY = getY() - velocityY * delta;

        for (Actor actor : getStage().getActors()) {
            if (actor instanceof TileActor) {
                TileActor tile = (TileActor) actor;
                if (overlaps(tile)) {
                    if (tile instanceof FloorTile || tile instanceof PlatformTile) {
                        float tileTop = tile.getY() + tile.getHeight();

                        // Vertical collision: if moving downward and crossing the tile's top
                        if (velocityY <= 0f) {
                            float oldBottom = oldY;
                            float newBottom = getY();
                            if (oldBottom >= tileTop && newBottom < tileTop) {
                                setY(tileTop);
                                velocityY = 0;
                                isOnGround = true;
                            }
                        }

                        // Horizontal collision:
                        float tileLeft = tile.getX();
                        float tileRight = tile.getX() + tile.getWidth();
                        float playerLeft = getX();
                        float playerRight = getX() + getWidth();

                        // If moving left and player's left edge crosses tile's right edge
                        if (velocityX < 0) {
                            if (oldX >= tileRight && playerLeft < tileRight && getY() != tile.getY() + tile.getHeight()) {
                                setX(tileRight);
                                velocityX = 0;
                            }
                        }
                        // If moving right and player's right edge crosses tile's left edge
                        else if (velocityX > 0) {
                            if (oldX + getWidth() <= tileLeft && playerRight > tileLeft && getY() != tile.getY() + tile.getHeight()) {
                                setX(tileLeft - getWidth());
                                velocityX = 0;
                            }
                        }
                    } else if (tile instanceof HazardTile) {
                        System.out.println("Hit a hazard! (Respawn or lose health)");
                    }
                }
            }
        }
    }

    public boolean overlaps(TileActor tile) {
        Rectangle playerRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Rectangle tileRect   = new Rectangle(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
        return playerRect.overlaps(tileRect);
    }
}
