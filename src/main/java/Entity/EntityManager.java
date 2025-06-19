package Entity;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import GameState.BaseLevelState;
import TileMap.TileMap;

import java.awt.Graphics2D;

// Manages all entities except player
public class EntityManager {

    private Player player; // Reference to player

    private List<Enemy> enemies; // Active enemies
    private List<Explosion> explosions; // Active explosions
    private List<Effects.DamageNumber> damageNumbers; // Active damage numbers

    // Initializes EntityManager with player
    public EntityManager(Player player) {
        this.player = player;
        this.enemies = new CopyOnWriteArrayList<>();
        this.explosions = new CopyOnWriteArrayList<>();
        this.damageNumbers = new CopyOnWriteArrayList<>();
    }

    // Adds enemy to list
    public void addEnemy(Enemy enemy) {
        if (enemy != null) {
            this.enemies.add(enemy);
        }
    }

    // Adds explosion to list
    public void addExplosion(Explosion explosion) {
        if (explosion != null) {
            this.explosions.add(explosion);
        }
    }

    // Adds damage number to list
    public void addDamageNumber(Effects.DamageNumber dn) {
        if (dn != null) {
            this.damageNumbers.add(dn);
        }
    }

    // Returns list of enemies
    public List<Enemy> getEnemies() {
        return enemies;
    }

    // Updates all entities
    public void updateAll(TileMap tileMap, BaseLevelState levelState) {

        // Updates all enemies
        for (Enemy e : enemies) {
            e.update();
        }

        // Updates and removes finished explosions
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            explosion.update();
            if (explosion.shouldRemove()) {
                explosions.remove(i);
            }
        }

        // Updates damage numbers
        for (Effects.DamageNumber dn : damageNumbers) {
            dn.update();
        }

        // Checks player attacks against enemies
        if (player != null) {
            player.checkAttack(new ArrayList<>(enemies));
        }
    }

    // Draws all entities
    public void drawAll(Graphics2D g, TileMap tileMap) {

        // Draws enemies
        for (Enemy e : enemies) {
            e.setMapPosition();
            e.draw(g);
        }

        // Draws explosions
        for (Explosion ex : explosions) {
            ex.setMapPosition((int) tileMap.getx(), (int) tileMap.gety());
            ex.draw(g);
        }

        // Draws damage numbers
        for (Effects.DamageNumber dn : damageNumbers) {
            dn.draw(g);
        }
    }

    // Clears all entities and effects
    public void clearAll() {
        enemies.clear();
        explosions.clear();
        damageNumbers.clear();
    }

    // Returns number of enemies
    public int getTotalEnemies() {
        return enemies.size();
    }
}
