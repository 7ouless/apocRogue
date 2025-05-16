package io.github.apocRogue.OMarket;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

/**
 * Data transfer object representing a fully enriched market listing.
 */
public class MarketListing {
    @SerializedName("listingID")
    private long listingID;

    @SerializedName("sellerID")
    private int sellerID;

    private String itemCode;
    private long price;
    private Instant postTime;
    private int itemSkull;
    private String name;
    private int damage;
    private int speed;
    private int dashSpeed;
    private int noiseLevel;
    private int dashDuration;
    private int dashCoolDown;

    public long getListingID() {
        return listingID;
    }

    public int getSellerID() {
        return sellerID;
    }

    public String getItemCode() {
        return itemCode;
    }

    public long getPrice() {
        return price;
    }

    public Instant getPostTime() {
        return postTime;
    }

    public int getItemSkull() {
        return itemSkull;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDashSpeed() {
        return dashSpeed;
    }

    public int getNoiseLevel() {
        return noiseLevel;
    }

    public int getDashDuration() {
        return dashDuration;
    }

    public int getDashCoolDown() {
        return dashCoolDown;
    }
}
