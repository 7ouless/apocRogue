package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class InventorySlot extends Table {
    private Skin skin;
    private boolean highlighted = false;
    private Image itemImage;
    private Item item; // the item stored in this slot (null if empty)

    public InventorySlot(Skin skin) {
        super(skin);
        this.skin = skin;
        setTouchable(Touchable.enabled);
        Drawable bg = skin.newDrawable(skin.getDrawable("white"), Color.DARK_GRAY);
        setBackground(bg);
        itemImage = new Image();
        itemImage.setTouchable(Touchable.disabled);
        add(itemImage).expand().fill();

        // Debug listener to log touch events.
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("Debug", "touchDown in InventorySlot: " + InventorySlot.this + " | Item: " + getItem());
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                Gdx.app.log("Debug", "touchDragged in InventorySlot: " + InventorySlot.this);
            }
        });
    }

    public boolean isEmpty() {
        return item == null;
    }

    // Set an item into the slot.
    public void setItem(Item item) {
        this.item = item;
        if (item != null) {
            itemImage.setDrawable(new TextureRegionDrawable(new TextureRegion(item.getTexture())));
            Gdx.app.log("Debug", "Item set in slot: " + this + " | Item: " + item.getType());
        } else {
            itemImage.setDrawable(null);
        }
    }

    public Item getItem() {
        return item;
    }

    // Get the drawable for drag-and-drop.
    public Drawable getItemDrawable() {
        return itemImage.getDrawable();
    }

    // Remove the item from the slot.
    public void clearItem() {
        item = null;
        itemImage.setDrawable(null);
    }

    // Highlight this slot.
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        if (highlighted)
            setBackground(skin.newDrawable(skin.getDrawable("white"), Color.YELLOW));
        else
            setBackground(skin.newDrawable(skin.getDrawable("white"), Color.DARK_GRAY));
    }
}
