package io.github.apocRogue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class InventorySlot extends Table {
    private Skin skin;
    private boolean highlighted = false;
    private Image itemImage;
    private boolean empty = true;

    public InventorySlot(Skin skin) {
        super(skin);
        this.skin = skin;
        // Use the "white" drawable tinted to dark gray as the default background.
        Drawable bg = skin.newDrawable(skin.getDrawable("white"), Color.DARK_GRAY);
        setBackground(bg);
        itemImage = new Image();
        add(itemImage).expand().fill();
    }

    // Set an item in the slot using its texture.
    public void setItem(Texture texture) {
        if (texture != null) {
            itemImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
            empty = false;
        }
    }

    // Get the current item's drawable (for drag-and-drop).
    public Drawable getItemDrawable() {
        return itemImage.getDrawable();
    }

    public boolean isEmpty() {
        return empty;
    }

    // Remove the item from this slot.
    public void clearItem() {
        itemImage.setDrawable(null);
        empty = true;
    }

    // Highlight the slot (change its background) to indicate selection.
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        if (highlighted)
            setBackground(skin.newDrawable(skin.getDrawable("white"), Color.YELLOW));
        else
            setBackground(skin.newDrawable(skin.getDrawable("white"), Color.DARK_GRAY));
    }
}
