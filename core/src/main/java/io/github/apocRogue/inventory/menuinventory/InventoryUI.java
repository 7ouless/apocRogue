package io.github.apocRogue.inventory.menuinventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.JsonValue;
import io.github.apocRogue.inventory.general.InventoryItem;
import io.github.apocRogue.inventory.general.InventoryPreferences;
import io.github.apocRogue.database.DBManager;
import io.github.apocRogue.database.JsonCallback;
import io.github.apocRogue.inventory.gameinventory.DragData;
import io.github.apocRogue.inventory.gameinventory.InventorySlot;
import io.github.apocRogue.stages.MainScreen;
import io.github.apocRogue.stages.stageBuilder;
import io.github.apocRogue.weapons.*;

import java.util.*;
import java.util.List;

/**
 * UI screen for viewing and arranging player inventory and hotbar.
 * Loads all items from backend, and binds them to stash & equip slots.
 */
public class InventoryUI {
    private final Stage stage;
    private final Skin skin;
    private final stageBuilder game;

    private Table root;
    private final List<InventorySlot> stashSlots = new ArrayList<>();
    private final List<InventorySlot> equipSlots = new ArrayList<>();
    private final DragAndDrop dragAndDrop;

    // Managers
    private final WeaponTypeRegistry typeRegistry;
    private final DBManager dbManager;

    // Runtime item cache
    private final List<Weapon> stashWeapons = new ArrayList<>();
    private final Map<String, Weapon> codeToWeapon = new HashMap<>();

    public InventoryUI(Stage stage, Skin skin, stageBuilder game) {
        this.stage = stage;
        this.skin  = skin;
        this.game  = game;

        dragAndDrop = new DragAndDrop();
        buildLayout();

        // Load type metadata
        typeRegistry = new WeaponTypeRegistry();
        typeRegistry.load("ui/weapon_types.json");

        // Fetch inventory from backend
        dbManager = DBManager.get();
        dbManager.get().fetchInventory(new JsonCallback() {
            @Override
            public void onSuccess(String json) {

            }

            @Override
            public void onSuccess(JsonValue dataArray) {
                populateStash(dataArray);
                populateEquipSlots();
                setupDragAndDrop();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onFailure(String error) {
                Gdx.app.error("InventoryUI", "Failed to load inventory: " + error);
            }
        });
    }

    private void buildLayout() {
        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Back button
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainScreen(game));
            }
        });
        root.add(back).left().pad(10);
        root.row();

        // Content
        Table content = new Table(skin);
        root.add(content).expand().fill().pad(10);
        root.row();

        // Stash pane
        Table stashTable = new Table(skin);
        stashTable.defaults().size(64,64).pad(5);
        for (int i = 0; i < 18; i++) {
            InventorySlot slot = new InventorySlot(skin);
            stashSlots.add(slot);
            stashTable.add(slot);
            if ((i + 1) % 6 == 0) stashTable.row();
        }
        ScrollPane stashPane = new ScrollPane(stashTable, skin);
        content.add(stashPane).expand().fill().padRight(10);

        // Equip/hotbar panel
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
        for (int i = 0; i < 5; i++) {
            InventorySlot slot = new InventorySlot(skin);
            equipSlots.add(slot);
            equipTable.add(slot);
        }
        rightCol.add(equipTable);
        content.add(rightCol).width(300).expandY().fillY();
    }
    public void clearStash() {
        for (InventorySlot slot : stashSlots) {
            slot.clearItem();
        }
    }

    public void populateStashFromPrefs() {
        List<String> saved = InventoryPreferences.loadHotbar();
        for (int i = 0; i < stashSlots.size(); i++) {
            InventorySlot slot = stashSlots.get(i);
            if (i < saved.size()) {
                String code = saved.get(i);
                // find matching Weapon by code in loadedWeapons
                for (Weapon w : stashWeapons) {
                    if (w.getItemCode().equals(code)) {
                        slot.setItem(w);
                        break;
                    }
                }
            } else {
                slot.clearItem();
            }
        }
    }
    public void setStashItems(List<InventoryItem> items) {
        // clear out any existing stash slots
        for (InventorySlot slot : stashSlots) slot.clearItem();
        stashWeapons.clear();
        codeToWeapon.clear();

        // convert InventoryItem → Weapon
        for (int i = 0; i < items.size() && i < stashSlots.size(); i++) {
            InventoryItem bean = items.get(i);
            // decode, lookup type, build Weapon (as you do in populateStash)
            WeaponIDDecoder.Decoded dec = WeaponIDDecoder.decode(bean.getItemCode());
            WeaponTypeInfo info = typeRegistry.get(dec.typeID);
            Weapon w = new Weapon(info, dec.skullLevel, dec.skullSub, dec.stats);

            stashWeapons.add(w);
            codeToWeapon.put(bean.getItemCode(), w);
            stashSlots.get(i).setItem(w);
        }
    }

    public void refreshStashGrid() {
        // If you need to re-layout after setting items:
        stashSlots.forEach(Actor::invalidate);
        stage.act(0);
        stage.draw();
    }

    private void populateStash(JsonValue dataArray) {
        stashWeapons.clear();
        codeToWeapon.clear();

        // 1) Decode each JSON entry into a Weapon, cache it
        for (JsonValue item : dataArray) {
            String code = item.getString("itemCode");
            // your server‐side decoder
            WeaponIDDecoder.Decoded dec = WeaponIDDecoder.decode(code);

            // look up the WeaponType from your registry
            WeaponTypeRegistry type = typeRegistry.get(dec.typeID);
            // build a Weapon instance (adjust constructor as you have it)
            Weapon w = new Weapon(type, dec.skullLevel, dec.skullSub, dec.stats);

            stashWeapons.add(w);
            codeToWeapon.put(code, w);
        }

        // 2) Now actually fill your stash slots left to right
        for (int i = 0; i < stashSlots.size(); i++) {
            InventorySlot slot = stashSlots.get(i);
            if (i < stashWeapons.size()) {
                slot.setItem(stashWeapons.get(i));
            } else {
                slot.clearItem();
            }
        }
    }
    private void populateEquipSlots() {
        List<String> savedOrder = InventoryPreferences.loadHotbar();
        for (int i = 0; i < equipSlots.size(); i++) {
            InventorySlot slot = equipSlots.get(i);
            if (i < savedOrder.size()) {
                Weapon w = codeToWeapon.get(savedOrder.get(i));
                if (w != null) slot.setItem(w);
                else slot.clearItem();
            } else {
                slot.clearItem();
            }
        }
    }

    private void setupDragAndDrop() {
        for (InventorySlot slot : stashSlots) {
            addDragSourcesAndTargets(slot);
        }
        for (InventorySlot slot : equipSlots) {
            addDragSourcesAndTargets(slot);
        }
    }

    private void addDragSourcesAndTargets(final InventorySlot slot) {
        dragAndDrop.addSource(new DragAndDrop.Source(slot) {
            @Override public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                if (slot.isEmpty()) return null;
                DragData dd = new DragData(slot, slot.getWeapon());
                DragAndDrop.Payload p = new DragAndDrop.Payload();
                p.setObject(dd);
                p.setDragActor(new Image(slot.getItemDrawable()));
                slot.clearItem();
                return p;
            }
            @Override public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null) {
                    DragData dd = (DragData)payload.getObject();
                    if (dd.sourceSlot != null) dd.sourceSlot.setItem(dd.weapon);
                }
            }
        });

        dragAndDrop.addTarget(new DragAndDrop.Target(slot) {
            @Override public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }
            @Override public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                DragData dd = (DragData)payload.getObject();
                Weapon incoming = dd.weapon;
                Weapon existing = slot.getWeapon();
                slot.setItem(incoming);
                dd.sourceSlot.setItem(existing);
                // persist hotbar on equip slot changes
                saveCurrentHotbarOrder();
            }
        });
    }

    private void saveCurrentHotbarOrder() {
        List<String> order = new ArrayList<>();
        for (InventorySlot slot : equipSlots) {
            if (!slot.isEmpty()) order.add(slot.getWeapon().getItemCode());
        }
        InventoryPreferences.saveHotbar(order);
    }
    public void bindToHotbar(int slotIndex, String itemCode) {
        Preferences prefs = Gdx.app.getPreferences("hotbar");
        prefs.putString("slot" + slotIndex, itemCode);
        prefs.flush();
        // update the UI highlight, etc.
    }
}
