package io.github.apocRogue.inventory.gameinventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.github.apocRogue.weapons.StatKeys;
import io.github.apocRogue.weapons.Weapon;

/**
 * A single inventory slot that displays a Weapon’s icon,
 * and on hover shows a tooltip with its ID and stats.
 */
public class InventorySlot extends Table {
    private final Skin skin;
    private final Image itemImage;
    private Weapon weapon;

    // Tooltip for ID + stats
    private final Tooltip<Label> tooltip;

    public InventorySlot(Skin skin) {
        super(skin);
        this.skin = skin;

        // Slot background
        Drawable bg = skin.newDrawable("white", Color.DARK_GRAY);
        setBackground(bg);

        // Image placeholder
        itemImage = new Image();
        add(itemImage).expand().fill();

        // Tooltip setup
        TooltipManager manager = TooltipManager.getInstance();
        manager.initialTime = 0.3f;

        Label tipLabel = new Label("", skin);
        tooltip = new Tooltip<>(tipLabel, manager);
        addListener(tooltip);
    }

    /** Updates the slot’s weapon, its icon, and tooltip text. */
    public void setItem(Weapon weapon) {
        this.weapon = weapon;

        if (weapon != null) {
            // Show icon
            itemImage.setDrawable(new TextureRegionDrawable(
                new TextureRegion(weapon.getTexture())
            ));

            // Build tooltip
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(weapon.getID()).append("\n");
            for (String key : StatKeys.ALL) {
                sb.append(key)
                    .append(": ")
                    .append(weapon.getStats().get(key))
                    .append("\n");
            }
            ((Label)tooltip.getActor()).setText(sb.toString());

        } else {
            itemImage.setDrawable(null);
            ((Label)tooltip.getActor()).setText("");
        }
    }

    public Weapon getWeapon() { return weapon; }
    public Drawable getItemDrawable() { return itemImage.getDrawable(); }
    public boolean isEmpty() { return weapon == null; }
    public void clearItem() {
        weapon = null;
        itemImage.setDrawable(null);
    }

    /** Optional: highlight selection. */
    public void setHighlighted(boolean highlighted) {
        Color tint = highlighted ? Color.YELLOW : Color.DARK_GRAY;
        setBackground(skin.newDrawable("white", tint));
    }
}
