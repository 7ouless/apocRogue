package io.github.apocRogue.globals.stats;

public class StatsComponent {
    private int health;
    private int maxHealth;
    private int strength;
    private int defense;
    private int speed;
    private int dashes;
    private int jumps;
    private int sightSens;
    private float aggression;

    private float stamina;
    private float maxStamina = 100f;
    private float staminaRegenPerSec = 20f;
    private float jumpStaminaCost = 25f;
    private float dashStaminaCost = 40f;

    public StatsComponent(int health, int maxHealth, int strength, int defense, int speed, int dashes, int jumps, int sightSens, float aggression) {
        this.health = health;
        this.maxHealth = maxHealth;
        this.strength = strength;
        this.defense = defense;
        this.speed = speed;
        this.dashes = dashes;
        this.jumps = jumps;
        this.sightSens = sightSens;
        this.aggression = aggression;
        this.stamina = maxStamina;
    }

    public void takeDamage(int amount) {
        //future implementation - defense?
        health -= amount;
        if (health < 0) health = 0;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }
    public int getMaxHealth() {
        return maxHealth;
    }
    public int getStrength() {
        return strength;
    }
    public int getDefense() {
        return defense;
    }
    public int getSpeed() {
        return speed;
    }
    public int getDashes() {
        return dashes;
    }
    public int getJumps() {
        return jumps;
    }
    public int sightSens() {
        return sightSens;
    }
    public boolean isPlayerDead() {
        return isDead();
    }
    public float getAggression() {
        return aggression;
    }

    public boolean spendStamina(float cost) {
        if (stamina < cost) return false;
        stamina -= cost;
        return true;
    }

    public float getJumpStaminaCost() {
        return jumpStaminaCost;
    }


    public float getDashStaminaCost() {
        return dashStaminaCost;
    }

    public void regenStamina(float delta) {
        stamina = Math.min(maxStamina, stamina + staminaRegenPerSec * delta);
    }

    public float getStamina() { return stamina; }
    public float getMaxStamina() { return maxStamina; }
}
