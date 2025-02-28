package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class Inventory {
    private Table hotbarTable;
    private Table inventoryTable;
    private Skin skin;
    private DragAndDrop dragAndDrop;
    private int selectedHotbarIndex = 0;
    private final int HOTBAR_SIZE = 5;
    private final int INVENTORY_SIZE = 18;
    private Array<InventorySlot> hotbarSlots;
    private Array<InventorySlot> inventorySlots;

    public Inventory(Skin skin) {
        this.skin = skin;
        hotbarSlots = new Array<InventorySlot>();
        inventorySlots = new Array<InventorySlot>();
        dragAndDrop = new DragAndDrop();
        createHotbar();
        createInventory();
        setupDragAndDrop();
    }

    // Create a hotbar with 5 slots positioned at the bottom left.
    private void createHotbar() {
        hotbarTable = new Table();
        // Fill the parent so that the table positions itself relative to the entire screen.
        hotbarTable.setFillParent(true);
        // Anchor the table to the bottom left with a padding.
        hotbarTable.bottom().left().pad(10);

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            InventorySlot slot = new InventorySlot(skin);
            if (i == selectedHotbarIndex) {
                slot.setHighlighted(true);
            }
            hotbarSlots.add(slot);
            hotbarTable.add(slot).size(50, 50).pad(5);
        }
        // Ensure the table layout is recalculated.
        hotbarTable.pack();
    }

    // Create an inventory grid with 18 slots (6 columns x 3 rows).
    private void createInventory() {
        inventoryTable = new Table(skin);
        inventoryTable.setVisible(false);
        int columns = 6;
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            InventorySlot slot = new InventorySlot(skin);
            inventorySlots.add(slot);
            inventoryTable.add(slot).size(50, 50).pad(5);
            if ((i + 1) % columns == 0)
                inventoryTable.row();
        }
        // Center the inventory table on the screen.
        inventoryTable.setPosition(
            Gdx.graphics.getWidth() / 2 - inventoryTable.getWidth() / 2,
            Gdx.graphics.getHeight() / 2 - inventoryTable.getHeight() / 2
        );
    }

    // Setup DragAndDrop for all slots.
    private void setupDragAndDrop() {
        Array<InventorySlot> allSlots = new Array<InventorySlot>();
        allSlots.addAll(hotbarSlots);
        allSlots.addAll(inventorySlots);

        for (final InventorySlot slot : allSlots) {
            // Create a drag source for each slot.
            dragAndDrop.addSource(new Source(slot) {
                @Override
                public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    if (slot.isEmpty()) return null;
                    Payload payload = new Payload();
                    // Create a copy of the current item image.
                    Image dragImage = new Image(slot.getItemDrawable());
                    payload.setDragActor(dragImage);
                    // Clear the slot temporarily while dragging.
                    slot.clearItem();
                    return payload;
                }
            });
            // Create a drop target for each slot.
            dragAndDrop.addTarget(new Target(slot) {
                @Override
                public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                    return true;
                }
                @Override
                public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    // For demonstration, drop a dummy item.
                    // In a full implementation you would swap or transfer actual item data.
                    slot.setItem(new Texture(Gdx.files.internal("ui/dummy.png")));
                }
            });
        }
    }

    // Add the hotbar and inventory tables to the provided stage.
    public void draw(Stage stage) {
        stage.addActor(hotbarTable);
        stage.addActor(inventoryTable);
    }

    // Toggle the visibility of the full inventory.
    public void toggleInventory() {
        inventoryTable.setVisible(!inventoryTable.isVisible());
    }

    // Set the currently selected hotbar slot and update highlighting.
    public void setSelectedHotbarIndex(int index) {
        if (index < 0 || index >= HOTBAR_SIZE) return;
        hotbarSlots.get(selectedHotbarIndex).setHighlighted(false);
        selectedHotbarIndex = index;
        hotbarSlots.get(selectedHotbarIndex).setHighlighted(true);
    }

    // Scroll the hotbar selection (direction can be positive or negative).
    public void scrollHotbar(int direction) {
        int newIndex = (selectedHotbarIndex + direction + HOTBAR_SIZE) % HOTBAR_SIZE;
        setSelectedHotbarIndex(newIndex);
    }
}
