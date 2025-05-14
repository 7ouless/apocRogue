package io.github.apocRogue.items;

public class ItemTypeInfo {
    public String name;
    public String texturePath;
    public boolean stackable;
    public int     maxStack;

    public String getName()        { return name; }
    public String getTexturePath() { return texturePath; }
    public boolean isStackable()   { return stackable; }
    public int  getMaxStack()      { return maxStack; }
}
