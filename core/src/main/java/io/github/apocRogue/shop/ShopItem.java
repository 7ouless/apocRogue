package io.github.apocRogue.shop;

public class ShopItem {
    private String name;
    private String description;
    private int price;
    private int requiredLevel;

    private int stock;
    private int maxStock;

    public ShopItem(String name, String description, int price) {
        this(name, description, price, 1);
    }

    public ShopItem(String name, String description, int price, int requiredLevel) {
        // Let’s default maxStock to 5 for all items
        this(name, description, price, requiredLevel, 5);
    }

    // New constructor letting you set stock
    public ShopItem(String name, String description, int price, int requiredLevel, int maxStock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.maxStock = maxStock;
        this.stock = maxStock; // item starts fully stocked
    }

    // Called by ShopKeeper buy logic
    public void decrementStock() {
        if (stock > 0) {
            stock--;
        }
    }

    // Called by ShopKeeper restock logic
    public void restock() {
        stock = maxStock;
    }

    // Accessors
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

    public int getStock() {
        return stock;
    }

    public int getMaxStock() {
        return maxStock;
    }
}
