package io.github.apocRogue.weapons;

public class WeaponTypeInfo {
    public String name;
    public String texturePath;
    public String ammoTexture;
    public int projectileValue;

    public boolean projectileType;

    public int getProjectileValue() {return projectileValue;}
    public String getName() {
        return name;
    }


    public String getTexturePath() {
        return texturePath;
    }


    public String getAmmoTexture() {
        return ammoTexture;
    }


    public boolean isProjectileType() {
        return projectileType;
    }
}
