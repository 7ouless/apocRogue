package io.github.apocRogue.shop;

public class ShopItem {
    private String name;
    private String description;
    private int price;
    private int requiredLevel;

    public ShopItem(String name, String description, int price) {
        // Default requiredLevel to 1 for items that are always unlocked
        this(name, description, price, 1);
    }

    public ShopItem(String name, String description, int price, int requiredLevel) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.requiredLevel = requiredLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
