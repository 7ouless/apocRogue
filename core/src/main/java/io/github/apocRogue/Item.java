package io.github.apocRogue;

import com.badlogic.gdx.graphics.Texture;

public class Item {
    public enum ItemType {
        SWORD,
        BOW
    }

    private ItemType type;
    private Texture texture;

    public Item(ItemType type, Texture texture) {
        this.type = type;
        this.texture = texture;
    }

    public ItemType getType() {
        return type;
    }

    public Texture getTexture() {
        return texture;
    }
}
