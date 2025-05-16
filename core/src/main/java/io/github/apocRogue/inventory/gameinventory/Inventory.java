package io.github.apocRogue.inventory.gameinventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.github.apocRogue.weapons.Weapon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        dragAndDrop.setTapSquareSize(20);
        createHotbar();
        createInventory();
        setupDragAndDrop();

    }

    // Create a hotbar with 5 slots positioned at the bottom left.
    private void createHotbar() {
        hotbarTable = new Table();
        hotbarTable.setFillParent(true);
        hotbarTable.bottom().left().pad(10);

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            InventorySlot slot = new InventorySlot(skin);
            // Enable touchable explicitly
            slot.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            if (i == selectedHotbarIndex) {
                slot.setHighlighted(true);
            }
            hotbarSlots.add(slot);
            hotbarTable.add(slot).size(50, 50).pad(5);
        }
    }
    public List<InventorySlot> getAllSlots() {
        List<InventorySlot> all = new ArrayList<>();
        // Add hotbar slots
        for (InventorySlot slot : hotbarSlots) {
            all.add(slot);
        }
        // Add main inventory slots
        for (InventorySlot slot : inventorySlots) {
            all.add(slot);
        }
        return all;
    }

    // Create an inventory grid
    private void createInventory() {
        inventoryTable = new Table(skin);
        inventoryTable.setVisible(false);
        int columns = 6;
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            InventorySlot slot = new InventorySlot(skin);
            slot.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            inventorySlots.add(slot);
            inventoryTable.add(slot).size(50, 50).pad(5);
            if ((i + 1) % columns == 0)
                inventoryTable.row();
        }
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
            dragAndDrop.addSource(new Source(slot) {
                @Override
                public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    if (slot.isEmpty()) {
                        Gdx.app.log("DragAndDrop", "Slot empty, not starting drag");
                        return null;
                    }
                    Gdx.app.log("DragAndDrop", "Drag started from slot");

                    // Create the Payload
                    Payload payload = new Payload();

                    // Pack the source slot and the weapon together
                    DragData dragData = new DragData(slot, slot.getWeapon());
                    payload.setObject(dragData);

                    // Prepare the drag image
                    Drawable drawable = slot.getItemDrawable();
                    if (drawable == null) {
                        Gdx.app.log("DragAndDrop", "No drawable found, aborting drag");
                        return null;
                    }
                    Image dragImage = new Image(drawable);
                    payload.setDragActor(dragImage);

                    // Clear the slot while dragging
                    slot.clearItem();

                    return payload;
                }

                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
                    // If target == null, the item was dropped on "nothing"
                    if (target == null) {
                        DragData dragData = (DragData) payload.getObject();
                        if (dragData != null && dragData.sourceSlot != null) {
                            // Snap back to original slot
                            dragData.sourceSlot.setItem(dragData.weapon);
                        }
                    }
                    // If target != null, the drop(...) method of the Target handles placing or swapping
                }
            });


            // Create a drop target for each slot.
            dragAndDrop.addTarget(new Target(slot) {
                @Override
                public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                    // Log dragging over a slot
                    Gdx.app.log("DragAndDrop", "Dragging over slot");
                    return true;
                }
                @Override
                public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    Gdx.app.log("DragAndDrop", "Dropped on slot");

                    // Retrieve the dragged data
                    DragData dragData = (DragData) payload.getObject();
                    if (dragData == null) return;

                    // The weapon the user was dragging
                    Weapon draggedWeapon = dragData.weapon;
                    // The target slot where we are dropping
                    Weapon targetWeapon = slot.getWeapon();

                    // Place the dragged weapon in the new slot
                    slot.setItem(draggedWeapon);

                    // If the target slot had a weapon, swap
                    if (targetWeapon != null) {
                        // The Source's actor is the original slot
                        InventorySlot sourceSlot = (InventorySlot) source.getActor();
                        sourceSlot.setItem(targetWeapon);
                    }
                }
            });
        }
    }


    public void draw(Stage stage) {
        stage.addActor(hotbarTable);
        stage.addActor(hotbarTable);
        stage.act(0f);
        for (int i = 0; i < hotbarSlots.size; i++) {
            InventorySlot s = hotbarSlots.get(i);
        }
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
    public Weapon getSelectedWeapon(){
        return hotbarSlots.get(selectedHotbarIndex).getWeapon();
    }

    public boolean addItem(Weapon weapon) {
        // First try hotbar
        for (InventorySlot slot : hotbarSlots) {
            if (slot.isEmpty()) {
                slot.setItem(weapon);
                return true;
            }
        }
        // If hotbar is full, try main inventory
        for (InventorySlot slot : inventorySlots) {
            if (slot.isEmpty()) {
                slot.setItem(weapon);
                return true;
            }
        }
        // If everything is full, return false
        return false;
    }
}
