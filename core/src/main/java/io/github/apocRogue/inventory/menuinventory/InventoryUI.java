package io.github.apocRogue.inventory.menuinventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Scaling;

import io.github.apocRogue.inventory.gameinventory.DragData;
import io.github.apocRogue.inventory.gameinventory.InventorySlot;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.weapons.Weapon;
import io.github.apocRogue.weapons.WeaponTypeInfo;
import io.github.apocRogue.weapons.WeaponTypeRegistry;
import io.github.apocRogue.inventory.menuinventory.InventoryService.InventoryItemPayload;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Inventory UI that synchronises with the remote Cloud-Functions back-end.
 * All items and stats are loaded directly from the server;
 * if no saved layout exists, items appear in a random order.
 * UI updates occur on the render thread via postRunnable.
 */
public class InventoryUI {

    private final Stage stage;
    private final Skin skin;
    private final stageBuilder game;

    private Table root;
    private final List<InventorySlot> stashSlots = new ArrayList<>();
    private final List<InventorySlot> equipSlots = new ArrayList<>();
    private final DragAndDrop dragAndDrop;

    // Weapon metadata registry
    private final WeaponTypeRegistry typeRegistry;

    public InventoryUI(Stage stage, Skin skin, stageBuilder game) {
        this.stage = stage;
        this.skin = skin;
        this.game = game;

        dragAndDrop = new DragAndDrop();
        buildLayout();

        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json");

        // Load the authoritative inventory from server
        syncWithServerInventory();

        setupDragAndDrop();
    }

    private void buildLayout() {
        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        TextButton back = new TextButton("Back", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(back).left().pad(10);
        root.row();

        Table content = new Table(skin);
        root.add(content).expand().fill().pad(10);
        root.row();

        Table stashTable = new Table(skin);
        stashTable.defaults().size(64,64).pad(5);
        int cols = 6, rows = 3;
        IntStream.range(0, rows).forEach(r -> {
            IntStream.range(0, cols).forEach(c -> {
                InventorySlot slot = new InventorySlot(skin);
                stashSlots.add(slot);
                stashTable.add(slot);
            });
            stashTable.row();
        });
        ScrollPane stashPane = new ScrollPane(stashTable, skin);
        content.add(stashPane).expand().fill().padRight(10);

        Table rightCol = new Table(skin);
        rightCol.defaults().pad(5);

        Image portrait = new Image(new Texture(Gdx.files.internal("ui/character-portrait.png")));
        portrait.setScaling(Scaling.fit);
        Table portraitTable = new Table(skin);
        portraitTable.setBackground(skin.newDrawable("white", 0.2f,0.2f,0.2f,1f));
        portraitTable.add(portrait).size(200,200);
        rightCol.add(portraitTable).row();

        Table equipTable = new Table(skin);
        equipTable.defaults().size(64,64).pad(5);
        IntStream.range(0, 5).forEach(i -> {
            InventorySlot slot = new InventorySlot(skin);
            equipSlots.add(slot);
            equipTable.add(slot);
        });
        rightCol.add(equipTable);

        content.add(rightCol).width(300).expandY().fillY();
    }

    /**
     * Downloads the authoritative inventory list and populates hotbar and stash.
     * If no saved preferences exist, items appear in random order.
     * Ensures GL operations run on the render thread.
     */
    private void syncWithServerInventory() {
        List<String> saved = InventoryPreferences.load();
        boolean noPrefs = saved.isEmpty() || saved.stream().allMatch(String::isEmpty);

        InventoryService.fetchInventory(new InventoryService.Callback<List<InventoryItemPayload>>() {
            @Override
            public void onSuccess(List<InventoryItemPayload> items) {
                Gdx.app.postRunnable(() -> {
                    // clear all slots
                    equipSlots.forEach(InventorySlot::clearItem);
                    stashSlots.forEach(InventorySlot::clearItem);

                    // flatten payloads by count
                    List<InventoryItemPayload> flat = new ArrayList<>();
                    for (InventoryItemPayload p : items) {
                        for (int i = 0; i < p.count; i++) {
                            flat.add(p);
                        }
                    }
                    if (noPrefs) Collections.shuffle(flat);

                    // populate hotbar (up to equipSlots.size)
                    Iterator<InventoryItemPayload> it = flat.iterator();
                    for (int i = 0; i < equipSlots.size() && it.hasNext(); i++) {
                        InventoryItemPayload p = it.next();
                        WeaponTypeInfo info = typeRegistry.get(p.typeID);
                        if (info == null) continue;
                        Weapon w = new Weapon(
                            p.itemCode,
                            info.getName(),
                            p.stats.getOrDefault("damage", 0),
                            new Texture(Gdx.files.internal(info.getTexturePath())),
                            info.isProjectileType(),
                            p.stats.getOrDefault("projectileValue", 0),
                            info.getAmmoTexture(),
                            p.stats.getOrDefault("animationSpeed", 0),
                            p.stats.getOrDefault("noiseLevel", 0),
                            p.stats.getOrDefault("dashSpeed", 0),
                            p.stats.getOrDefault("dashDuration", 0),
                            p.stats.getOrDefault("dashCooldown", 0)
                        );
                        equipSlots.get(i).setItem(w);
                    }

                    // populate stash from the same flat list
                    for (InventoryItemPayload p : flat) {
                        WeaponTypeInfo info = typeRegistry.get(p.typeID);
                        if (info == null) continue;
                        Weapon w = new Weapon(
                            p.itemCode,
                            info.getName(),
                            p.stats.getOrDefault("damage", 0),
                            new Texture(Gdx.files.internal(info.getTexturePath())),
                            info.isProjectileType(),
                            p.stats.getOrDefault("projectileValue", 0),
                            info.getAmmoTexture(),
                            p.stats.getOrDefault("animationSpeed", 0),
                            p.stats.getOrDefault("noiseLevel", 0),
                            p.stats.getOrDefault("dashSpeed", 0),
                            p.stats.getOrDefault("dashDuration", 0),
                            p.stats.getOrDefault("dashCooldown", 0)
                        );
                        int idx = findFirstEmptyStash();
                        if (idx < 0) break;
                        stashSlots.get(idx).setItem(w);
                    }
                });
            }
            @Override public void onFailure(Throwable t) {
                Gdx.app.error("InventoryUI", "Failed to pull inventory", t);
            }
        });
    }

    private void setupDragAndDrop() {
        iterateAllSlots().forEach(slot -> {
            dragAndDrop.addSource(new Source(slot) {
                @Override public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    if (slot.isEmpty()) return null;
                    DragData dd = new DragData(slot, slot.getWeapon());
                    Payload p = new Payload();
                    p.setObject(dd);
                    p.setDragActor(new Image(slot.getItemDrawable()));
                    slot.clearItem();
                    return p;
                }

                @Override public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
                    if (target == null) {
                        DragData dd = (DragData) payload.getObject();
                        if (dd.sourceSlot != null) dd.sourceSlot.setItem(dd.weapon);
                    }
                    saveHotbarToPrefs();
                }
            });

            dragAndDrop.addTarget(new Target(slot) {
                @Override public boolean drag(Source source, Payload payload, float x, float y, int pointer) { return true; }
                @Override public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    DragData dd = (DragData) payload.getObject();
                    Weapon incoming = dd.weapon;
                    Weapon existing = slot.getWeapon();
                    slot.setItem(incoming);
                    dd.sourceSlot.setItem(existing);
                    saveHotbarToPrefs();
                }
            });
        });
    }

    private Iterable<InventorySlot> iterateAllSlots() {
        List<InventorySlot> all = new ArrayList<>(stashSlots);
        all.addAll(equipSlots);
        return all;
    }

    private int findFirstEmptyStash() {
        for (int i = 0; i < stashSlots.size(); i++) {
            if (stashSlots.get(i).isEmpty()) return i;
        }
        return -1;
    }

    private void saveHotbarToPrefs() {
        List<String> codes = new ArrayList<>();
        equipSlots.forEach(s -> codes.add(s.isEmpty() ? "" : s.getWeapon().getID()));
        InventoryPreferences.save(codes);
    }

    private void saveStashToPrefs() {
        List<String> codes = new ArrayList<>();
        stashSlots.forEach(s -> codes.add(s.isEmpty() ? "" : s.getWeapon().getID()));
        InventoryPreferences.save(codes);
    }
}
