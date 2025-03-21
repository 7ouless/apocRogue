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

    public StatsComponent(int health, int maxHealth, int strength, int defense, int speed, int dashes, int jumps, int sightSens) {
        this.health = health;
        this.maxHealth = maxHealth;
        this.strength = strength;
        this.defense = defense;
        this.speed = speed;
        this.dashes = dashes;
        this.jumps = jumps;
        this.sightSens = sightSens;
    }

    public void takeDamage(int amount) {
        // Possibly factor in defense
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

    // getters, setters, etc.
}
