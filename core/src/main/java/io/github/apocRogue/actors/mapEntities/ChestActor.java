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
import io.github.apocRogue.globals.difficulty.CurrentDificulty;
import io.github.apocRogue.globals.difficulty.RunManager;
import io.github.apocRogue.stages.LootGenerateService;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;
import io.github.apocRogue.weapons.Weapon;

import java.util.List;

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
    private final RunManager runMgr = RunManager.getInstance();

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
        int diff = runMgr.getSkullLevel();
        int sub  = runMgr.getWorldLevel();
        int rad  = CurrentDificulty.getRadiation();

        // get chest coords as floats
        float x = getX();
        float y = getY();

        // now pass x and y before the callback
        new LootGenerateService().generate(
            diff, sub, rad,   // world params
            1,                // count
            x, y,             // ◀── chest position floats
            new LootGenerateService.Callback<LootGenerateService.Res[]>() {
                @Override
                public void onSuccess(LootGenerateService.Res[] loot) {
                    for (LootGenerateService.Res r : loot) {
                        Gdx.app.postRunnable(() -> spawnDrop(r));
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    Gdx.app.error("CHEST", "Loot gen failed", t);
                }
            }
        );
    }

    private void spawnDrop(LootGenerateService.Res r) {
        String typeID = r.itemCode.substring(2,4);
        WeaponTypeInfo info = typeRegistry.get(typeID);
        if (info == null) return;

        Weapon w = new Weapon(
            r.itemCode,
            info.getName(),
            r.stats.get("damage"),
            new Texture(Gdx.files.internal(info.getTexturePath())),
            info.isProjectileType(),
            r.stats.get("projectileValue"),
            info.getAmmoTexture(),
            r.stats.get("animationSpeed"),
            r.stats.get("noiseLevel"),
            r.stats.get("dashSpeed"),
            r.stats.get("dashDuration"),
            r.stats.get("dashCooldown")
        );

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
