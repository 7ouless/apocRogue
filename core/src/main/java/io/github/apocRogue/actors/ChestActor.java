package io.github.apocRogue.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.apocRogue.weapons.Weapon;

public class ChestActor extends Image {
    private boolean opened = false;
    private int health = 1; // how many hits before it opens
    private Array<Weapon> possibleDrops;
    private Label pressELabel;     // The floating text
    private Skin uiSkin;           // We need a Skin to create the label
    private float interactRange = 80f; // distance within which we show "Press E to open"
    private Texture chestTexture;

    public ChestActor(Texture texture, float x, float y, Array<Weapon> possibleDrops, Skin uiSkin) {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());

        this.possibleDrops = possibleDrops;
        this.uiSkin = uiSkin;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        // Optionally draw a different sprite if opened = true
    }

    // Called when the chest is "hit" by a slash or arrow
    public void takeDamage(int amount) {
        if (!opened) {
            health -= amount;
            if (health <= 0) {
                openChest();
            }
        }
    }

    // Called when the player interacts with E while near
    public void openByInteraction() {
        if (!opened) {
            openChest();
        }
    }

    private void openChest() {
        opened = true;
        // Hide the label
        if (pressELabel != null) {
            pressELabel.setVisible(false);
        }
        // Spawn random items on the floor
        if (getStage() == null) return;

        // For example, spawn 2 items
        int itemsToSpawn = 2;
        for (int i = 0; i < itemsToSpawn; i++) {
            spawnRandomItem();
        }
        // Optionally remove the chest or switch to an "open chest" texture
        // remove()
        Texture openChest = new Texture("ui/openChest.jpg");
        // Update this Image actor to use the new texture
        setDrawable(new TextureRegionDrawable(new TextureRegion(openChest)));
    }

    private void spawnRandomItem() {
        if (possibleDrops.size == 0) return;
        int index = MathUtils.random(possibleDrops.size - 1);
        Weapon randomWeapon = possibleDrops.get(index);

        ItemActor item = new ItemActor(randomWeapon, getX(), getY());
        float vx = MathUtils.random(-100f, 100f);
        float vy = MathUtils.random(100f, 200f);
        item.setVelocity(vx, vy);

        getStage().addActor(item);
    }

    // For collision checks with slashes/arrows
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public boolean isOpened() {
        return opened;
    }

    // This method is called automatically when the actor is added to a stage.
    // We can create and add the label here so it's in the same stage as the chest.
    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null && pressELabel == null) {
            // Create the label with the provided skin
            pressELabel = new Label("Press R to open", uiSkin);
            pressELabel.setVisible(false);
            // Add it to the stage
            stage.addActor(pressELabel);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // If the chest is already opened or we have no stage, hide the label
        if (opened || getStage() == null || pressELabel == null) {
            if (pressELabel != null) pressELabel.setVisible(false);
            return;
        }

        // Find the player
        PlayerActor player = findPlayer();
        if (player == null) {
            pressELabel.setVisible(false);
            return;
        }

        // Distance check: if close, show the label; otherwise hide it
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float playerCenterY = player.getY() + player.getHeight() / 2f;

        float dx = centerX - playerCenterX;
        float dy = centerY - playerCenterY;
        float dist2 = dx * dx + dy * dy;

        if (dist2 < interactRange * interactRange) {
            // Show the label
            pressELabel.setVisible(true);
            // Position it slightly above the chest
            pressELabel.setPosition(
                centerX - pressELabel.getWidth() / 2f,
                getY() + getHeight() + 10f
            );
        } else {
            pressELabel.setVisible(false);
        }
    }

    private PlayerActor findPlayer() {
        // Loop through stage actors to find a PlayerActor
        for (Actor a : getStage().getActors()) {
            if (a instanceof PlayerActor) {
                return (PlayerActor) a;
            }
        }
        return null;
    }
}
