package Entity;

import TileMap.TileMap;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

// Manages enemy behavior
public class Enemy extends MapObject {
    protected int health; // Current health
    protected int maxHealth; // Maximum health
    protected boolean dead; // Death state
    protected int damage; // Damage dealt
    protected String name; // Enemy name
    protected boolean flinching; // Flinch state
    protected long flinchTimer; // Flinch timer (ns)
    protected ArrayList<BufferedImage[]> sprites; // Animation sprites

    // Initializes Enemy
    public Enemy(TileMap tm) {
        super(tm); // Passes tileMap to MapObject
    }

    // Checks if enemy is dead
    public boolean isDead() {
        return dead;
    }

    // Returns damage value
    public int getDamage() {
        return damage;
    }

    // Applies damage
    public void hit(int damage) {
        if (dead || flinching) return;
        health = Math.max(0, health - damage);
        if (health == 0) dead = true;
        flinching = true;
        flinchTimer = System.nanoTime();
    }

    // Updates enemy state
    public void update() {}

    // Returns current health
    public int getHealth() {
        return health;
    }

    // Returns maximum health
    public int getMaxHealth() {
        return maxHealth;
    }

    // Checks if enemy is a boss
    public boolean isBoss() {
        return false;
    }

    // Marks enemy as dead
    public void setDead() {
        health = 0;
        dead = true;
    }

    // Returns enemy name
    public String getName() {
        return name;
    }
}