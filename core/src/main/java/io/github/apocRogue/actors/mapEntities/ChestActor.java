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
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;
import io.github.apocRogue.weapons.Weapon;
import java.util.Map;
import io.github.apocRogue.shop.ShopWeaponPayload;
import io.github.apocRogue.weapons.WeaponGenerateService;

/**
 * A chest that, when opened (or destroyed), rolls and spawns two weapons
 * using your pseudo-hex ID system.
 */
public class ChestActor extends Image {
    private boolean opened = false;
    private int health = 1;
    private final Array<String> possibleTypeIDs;
    private final WeaponTypeRegistry typeRegistry;
    private Label pressRLabel;
    private final Skin uiSkin;
    private final float interactRange = 80f;

    public ChestActor(Texture texture,
                      float x, float y,
                      Array<String> possibleTypeIDs,
                      Skin uiSkin,
                      WeaponTypeRegistry typeRegistry)
    {
        super(texture);
        setPosition(x, y);
        setSize(texture.getWidth(), texture.getHeight());
        this.possibleTypeIDs = possibleTypeIDs;
        this.uiSkin          = uiSkin;
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

        String typeID = possibleTypeIDs.random();

        WeaponGenerateService svc = new WeaponGenerateService();
        svc.generate(typeID, 1, 1, new WeaponGenerateService.Callback() {
            @Override public void onSuccess(ShopWeaponPayload p) {

                // lookup static cosmetics
                WeaponTypeInfo info = typeRegistry.get(p.typeID);

                Weapon w = new Weapon(
                    p.itemCode,
                    info.getName(),
                    p.stats.get("damage"),
                    new Texture(Gdx.files.internal(info.getTexturePath())),
                    info.isProjectileType(),
                    p.stats.get("projectileValue"),
                    info.getAmmoTexture(),
                    p.stats.get("animationSpeed"),
                    p.stats.get("noiseLevel"),
                    p.stats.get("dashSpeed"),
                    p.stats.get("dashDuration"),
                    p.stats.get("dashCooldown")
                );

                ItemActor drop = new ItemActor(w, getX(), getY());
                drop.setVelocity(
                    MathUtils.random(-100f, 100f),
                    MathUtils.random(100f, 200f)
                );
                getStage().addActor(drop);
            }

            @Override public void onFailure(Throwable t) {
                Gdx.app.error("CHEST", "Loot generation failed", t);
            }
        });
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
