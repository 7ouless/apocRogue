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
        // Set a nonzero tap square size to improve drag detection.
        dragAndDrop.setTapSquareSize(0);
        createHotbar();
        createInventory();
        setupDragAndDrop();
        // Example: load a sword and a bow and place them into hotbar slots.
        Texture swordTexture = new Texture(Gdx.files.internal("ui/sword.png"));
        Texture bowTexture = new Texture(Gdx.files.internal("ui/bow.png"));
        Item sword = new Item(Item.ItemType.SWORD, swordTexture);
        Item bow = new Item(Item.ItemType.BOW, bowTexture);
        // Place sword in slot 0 and bow in slot 1.
        hotbarSlots.get(0).setItem(sword);
        hotbarSlots.get(1).setItem(bow);
    }

    // Create a hotbar with 5 slots positioned at the bottom left.
    private void createHotbar() {
        hotbarTable = new Table();
        // Fill the parent so that the table positions itself relative to the entire screen.
        hotbarTable.setFillParent(true);
        // Anchor the table to the bottom left with a padding.
        hotbarTable.bottom().left().pad(10);

        // Add an input listener (or use a global scroll listener) if desired.
        hotbarTable.addListener(new ActorGestureListener() {
            public void scrolled(InputEvent event, float x, float y, float amount) {
                scrollHotbar((int) amount);
            }
        });

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            InventorySlot slot = new InventorySlot(skin);
            if (i == selectedHotbarIndex) {
                slot.setHighlighted(true);
            }
            hotbarSlots.add(slot);
            hotbarTable.add(slot).size(50, 50).pad(5);
        }
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
        // Pack the table so it sizes itself to the children.
        inventoryTable.pack();
        // Center the inventory table on the screen.
        inventoryTable.setPosition(
            (Gdx.graphics.getWidth() - inventoryTable.getWidth()) / 2,
            (Gdx.graphics.getHeight() - inventoryTable.getHeight()) / 2
        );
        // Let only the children handle touch events.
        inventoryTable.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);
    }

    // Setup DragAndDrop for all slots.
    private void setupDragAndDrop() {
        Array<InventorySlot> allSlots = new Array<InventorySlot>();
        allSlots.addAll(hotbarSlots);
        allSlots.addAll(inventorySlots);

        for (final InventorySlot slot : allSlots) {
            // Drag source: when dragging starts, store the item in the payload.
            dragAndDrop.addSource(new Source(slot) {
                @Override
                public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    if (slot.isEmpty()) return null;
                    Gdx.app.log("Drag", "dragStart triggered for slot: " + slot);
                    Payload payload = new Payload();
                    payload.setObject(slot.getItem());
                    Image dragImage = new Image(slot.getItemDrawable());
                    // Set the size of the drag image to be the same as the slot.
                    dragImage.setSize(slot.getWidth(), slot.getHeight());
                    payload.setDragActor(dragImage);
                    slot.clearItem();
                    return payload;
                }
            });

            // Drop target: on drop, if the slot already has an item, swap; otherwise, set the new item.
            dragAndDrop.addTarget(new Target(slot) {
                @Override
                public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                    return true;
                }
                @Override
                public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    Item draggedItem = (Item) payload.getObject();
                    if (!slot.isEmpty()) {
                        // Swap items between target and source.
                        Item temp = slot.getItem();
                        slot.setItem(draggedItem);
                        InventorySlot sourceSlot = (InventorySlot) source.getActor();
                        sourceSlot.setItem(temp);
                    } else {
                        slot.setItem(draggedItem);
                    }
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
