package io.github.apocRogue;

import com.badlogic.gdx.graphics.Color;
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
    private Weapon weapon; // Store the weapon in this slot

    public InventorySlot(Skin skin) {
        super(skin);
        this.skin = skin;
        // Use the "white" drawable tinted to dark gray as the default background.
        Drawable bg = skin.newDrawable(skin.getDrawable("white"), Color.DARK_GRAY);
        setBackground(bg);
        itemImage = new Image();
        add(itemImage).expand().fill();
    }

    // Set a weapon in the slot.
    public void setItem(Weapon weapon) {
        this.weapon = weapon;
        if (weapon != null) {
            itemImage.setDrawable(new TextureRegionDrawable(new TextureRegion(weapon.getTexture())));
        } else {
            itemImage.setDrawable(null);
        }
    }

    // Retrieve the weapon in this slot.
    public Weapon getWeapon() {
        return weapon;
    }

    // Get the current item's drawable (for drag-and-drop).
    public Drawable getItemDrawable() {
        return itemImage.getDrawable();
    }

    public boolean isEmpty() {
        return weapon == null;
    }

    // Clear the weapon from this slot.
    public void clearItem() {
        weapon = null;
        itemImage.setDrawable(null);
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
