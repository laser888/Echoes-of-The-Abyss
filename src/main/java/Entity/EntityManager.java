package Entity;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import GameState.BaseLevelState;
import TileMap.TileMap;

import java.awt.Graphics2D;

public class EntityManager {

    private Player player;

    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Effects.DamageNumber> damageNumbers;

    public EntityManager(Player player) {
        this.player = player;

        this.enemies = new CopyOnWriteArrayList<>();
        this.explosions = new CopyOnWriteArrayList<>();
        this.damageNumbers = new CopyOnWriteArrayList<>();
    }

    public void addEnemy(Enemy enemy) {
        if (enemy != null) {
            this.enemies.add(enemy);
        }
    }

    public void addExplosion(Explosion explosion) {
        if (explosion != null) {
            this.explosions.add(explosion);
        }
    }

    public void addDamageNumber(Effects.DamageNumber dn) {
        if (dn != null) {
            this.damageNumbers.add(dn);
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void updateAll(TileMap tileMap, BaseLevelState levelState) {

        for (Enemy e : enemies) {
            e.update();
        }

        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            explosion.update();
            if (explosion.shouldRemove()) {
                explosions.remove(i);
            }
        }

        for (Effects.DamageNumber dn : damageNumbers) {
            dn.update();
        }

        if (player != null) {
            player.checkAttack(new ArrayList<>(enemies));
        }
    }

    public void drawAll(Graphics2D g, TileMap tileMap) {
        for (Enemy e : enemies) {
            e.setMapPosition();
            e.draw(g);
        }

        for (Explosion ex : explosions) {
            ex.setMapPosition((int) tileMap.getx(), (int) tileMap.gety());
            ex.draw(g);
        }

        for (Effects.DamageNumber dn : damageNumbers) {
            dn.draw(g);
        }
    }

    public void clearAll() {
        enemies.clear();
        explosions.clear();
        damageNumbers.clear();
    }

    public int getTotalEnemies() {
        return enemies.size();
    }
}
