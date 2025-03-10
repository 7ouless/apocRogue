package io.github.apocRogue.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.Rectangle;

public class DummyActor extends Image {
    private int health = 50; // total HP for the dummy

    public DummyActor(Texture texture, float x, float y) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void takeDamage(int amount) {
        health -= amount;
        System.out.println("Dummy took " + amount + " damage! Health now " + health);

        // If health <= 0, remove the dummy from the stage
        if (health > 10000000) {
            remove();
        }
    }

    // For collision checks
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }
}
