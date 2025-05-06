package io.github.apocRogue.actors.mapEntities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.useClasses.ItemActor;
import io.github.apocRogue.inventory.general.ItemManager;
import io.github.apocRogue.weapons.WeaponFactory;
import io.github.apocRogue.weapons.WeaponIDDecoder;
import io.github.apocRogue.weapons.WeaponIDDecoder.Decoded;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;
import io.github.apocRogue.weapons.Weapon;
import java.util.Map;

/**
 * A chest that, when opened (or destroyed), rolls and spawns two weapons
 * using your pseudo-hex ID system.
 */
public class ChestActor extends Image {
    private boolean opened = false;
    private int health = 1;
    private final Array<String> possibleTypeIDs;
    private final ItemManager itemManager;
    private final WeaponTypeRegistry typeRegistry;
    private Label pressRLabel;
    private final Skin uiSkin;
    private final float interactRange = 80f;

    public ChestActor(Texture texture,
                      float x, float y,
                      Array<String> possibleTypeIDs,
                      Skin uiSkin,
                      ItemManager itemManager,
                      WeaponTypeRegistry typeRegistry)
    {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());
        this.possibleTypeIDs = possibleTypeIDs;
        this.uiSkin          = uiSkin;
        this.itemManager     = itemManager;
        this.typeRegistry    = typeRegistry;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null && pressRLabel == null) {
            pressRLabel = new Label("Press R to open", uiSkin);
            pressRLabel.setVisible(false);
            stage.addActor(pressRLabel);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (opened || getStage() == null || pressRLabel == null) {
            if (pressRLabel != null) pressRLabel.setVisible(false);
            return;
        }
        PlayerActor player = findPlayer();
        if (player == null) {
            pressRLabel.setVisible(false);
            return;
        }
        float dx = (getX()+getWidth()/2f)  - (player.getX()+player.getWidth()/2f);
        float dy = (getY()+getHeight()/2f) - (player.getY()+player.getHeight()/2f);
        if (dx*dx + dy*dy < interactRange*interactRange) {
            pressRLabel.setVisible(true);
            pressRLabel.setPosition(
                getX() + getWidth()/2f - pressRLabel.getWidth()/2f,
                getY() + getHeight() + 10f
            );
        } else {
            pressRLabel.setVisible(false);
        }
    }

    /** Called by your “E/R” listener */
    public void openByInteraction() {
        if (!opened) openChest();
    }

    /** Called when the chest is attacked */
    public void takeDamage(int amount) {
        if (!opened && (health -= amount) <= 0) {
            openChest();
        }
    }

    private void openChest() {
        opened = true;
        if (pressRLabel != null) pressRLabel.setVisible(false);
        if (getStage() == null) return;

        // spawn two items
        for (int i = 0; i < 2; i++) spawnRandomItem();

        // show “open” texture
        Texture openTex = new Texture("ui/openChest.jpg");
        setDrawable(new TextureRegionDrawable(new TextureRegion(openTex)));
    }

    private void spawnRandomItem() {
        if (possibleTypeIDs.size == 0) return;

        // 1) pick a random weapon type
        String typeID = possibleTypeIDs.random();

        // 2) roll & encode with skullLevel=1, skullSub=1
        Map<String,Integer> base = itemManager.getBaseStats(typeID);
        String id = WeaponFactory.rollAndEncode(typeID, base, 1, 1);

        // 3) decode back to exact stats
        Decoded d = WeaponIDDecoder.decode(id);

        // 4) lookup static info
        WeaponTypeInfo info = typeRegistry.get(d.typeID);

        // 5) build the Weapon instance
        Weapon w = new Weapon(
            id,
            info.getName(),
            d.stats.get("damage"),
            new Texture(Gdx.files.internal(info.getTexturePath())),
            info.isProjectileType(),
            d.stats.get("projectileValue"),
            info.getAmmoTexture(),
            d.stats.get("animationSpeed"),
            d.stats.get("noiseLevel"),
            d.stats.get("dashSpeed"),
            d.stats.get("dashDuration"),
            d.stats.get("dashCooldown")
        );

        // 6) drop it into the world
        ItemActor drop = new ItemActor(w, getX(), getY());
        drop.setVelocity(
            MathUtils.random(-100f,100f),
            MathUtils.random(100f,200f)
        );
        getStage().addActor(drop);
    }

    private PlayerActor findPlayer() {
        for (Actor a : getStage().getActors()) {
            if (a instanceof PlayerActor) return (PlayerActor)a;
        }
        return null;
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public boolean isOpened() {
        return opened;
    }
}
