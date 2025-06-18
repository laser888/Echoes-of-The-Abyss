package Entity.Enemies.Bosses;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Entity.Projectiles.CardProjectile;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Livid extends Enemy {
    private Player player;
    private ArrayList<CardProjectile> cards;
    private ArrayList<Livid> cloneGroup;
    private String suitType;
    private boolean isRealBoss;
    private Random rand = new Random();
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int ATTACKING = 2;
    private long lastAttackTime;
    private static final long ATTACK_COOLDOWN = 2000;
    private static final int ATTACK_RANGE = 200;
    private long lastBlindTime;
    private static final long BLIND_COOLDOWN = 10000;
    private boolean shouldBlindPlayer = false;
    private static HashMap<String, BufferedImage> spriteSheets = new HashMap<>();

    static {
        String[] suits = {"Diamond", "Heart", "Spade"};
        String[] paths = {
                "/Sprites/Enemies/Bosses/DiamondBoss.gif",
                "/Sprites/Enemies/Bosses/HeartBoss.gif",
                "/Sprites/Enemies/Bosses/SpadeBoss.gif"
        };
        for (int i = 0; i < suits.length; i++) {
            BufferedImage sheet = null;
            try {
                sheet = ImageIO.read(Livid.class.getResourceAsStream(paths[i]));
                if (sheet == null) {
                    throw new IOException("Resource stream is null for " + paths[i]);
                }

            } catch (Exception e) {
                sheet = new BufferedImage(105, 135, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = sheet.createGraphics();
                g.setColor(Color.RED);
                g.fillRect(0, 0, 105, 135);
                g.dispose();
            }
            spriteSheets.put(suits[i], sheet);
        }
    }

    public Livid(TileMap tm, Player player, String suitType, boolean isRealBoss, ArrayList<Livid> cloneGroup) {
        super(tm);
        this.player = player;
        this.suitType = suitType;
        this.isRealBoss = isRealBoss;
        this.cloneGroup = cloneGroup;
        this.cards = new ArrayList<>();
        this.name = "Livid";
        moveSpeed = 0.5;
        maxSpeed = 0.5;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;
        falling = true;
        width = 35;
        height = 45;
        cwidth = 20;
        cheight = 20;
        health = maxHealth = 3000;
        damage = 45;
        loadSprites(spriteSheets.get(suitType));
        right = false;
        left = false;
    }

    private void loadSprites(BufferedImage spritesheet) {
        sprites = new ArrayList<>();
        if (spritesheet == null) {
            spritesheet = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = spritesheet.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, width, height);
            g.dispose();
        }

        BufferedImage[] idleFrames = new BufferedImage[1];
        BufferedImage[] walkingFrames = new BufferedImage[6];
        BufferedImage[] attackingFrames = new BufferedImage[2];

        try {
            if (spritesheet.getWidth() < width * 6 || spritesheet.getHeight() < height * 3) {
                throw new Exception("Invalid dimensions");
            }
            for (int i = 0; i < 1; i++) {
                idleFrames[i] = spritesheet.getSubimage(i * width, 0, width, height);
            }
            for (int i = 0; i < 6; i++) {
                walkingFrames[i] = spritesheet.getSubimage(i * width, height, width, height);
            }
            for (int i = 0; i < 2; i++) {
                attackingFrames[i] = spritesheet.getSubimage(i * width, height * 2, width, height);
            }
        } catch (Exception e) {
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, width, height);
            g.dispose();
            for (int i = 0; i < 1; i++) idleFrames[i] = placeholder;
            for (int i = 0; i < 6; i++) walkingFrames[i] = placeholder;
            for (int i = 0; i < 2; i++) attackingFrames[i] = placeholder;
        }

        for (int i = 0; i < idleFrames.length; i++) {
            if (idleFrames[i] == null) {
                idleFrames[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
        }
        for (int i = 0; i < walkingFrames.length; i++) {
            if (walkingFrames[i] == null) {
                walkingFrames[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
        }
        for (int i = 0; i < attackingFrames.length; i++) {
            if (attackingFrames[i] == null) {
                attackingFrames[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
        }

        sprites.add(idleFrames);
        sprites.add(walkingFrames);
        sprites.add(attackingFrames);

        animation = new Animation();
        setAnimation(IDLE);
    }

    private void setAnimation(int anim) {
        if (currentAction != anim) {
            currentAction = anim;
            animation.setFrames(sprites.get(anim));
            animation.setDelay(anim == WALKING ? 50 : (anim == ATTACKING ? 100 : 400));
        }
    }

    public void triggerBlind() {
        if (isRealBoss) {
            shouldBlindPlayer = true;
        }
    }

    public boolean shouldBlindPlayer() {
        if (shouldBlindPlayer) {
            shouldBlindPlayer = false;
            return true;
        }
        return false;
    }

    private void fireCard() {
        if (cards.size() >= 3) {
            return;
        }
        boolean fireRight = (player.getx() > x);
        CardProjectile card = new CardProjectile(tileMap, fireRight, damage);
        card.setPosition(x, y);
        cards.add(card);
        lastAttackTime = System.currentTimeMillis();
        setAnimation(ATTACKING);
    }

    @Override
    public void hit(int damage) {
        if (dead || flinching) return;
        health -= damage;
        if (health <= 0) {
            dead = true;
            if (!isRealBoss) {
                for (Livid l : cloneGroup) {
                    if (l.isRealBoss && !l.dead) {
                        l.health = l.maxHealth;
                        break;
                    }
                }
            }
        }
        flinching = true;
        flinchTimer = System.nanoTime();
    }

    private void getNextPosition() {
        double minDistance = 50.0;
        double distToPlayer = player.getx() - x;
        double absDistToPlayer = Math.abs(distToPlayer);

        if (absDistToPlayer < minDistance) {
            dx = 0;
        } else if (absDistToPlayer < ATTACK_RANGE) {

            double targetDx = (distToPlayer > 0 ? moveSpeed : -moveSpeed);
            dx = (dx + targetDx) / 2;
            facingRight = (distToPlayer > 0);
            left = !facingRight;
            right = facingRight;
        } else {
            dx = 0;
            left = false;
            right = false;
        }

        for (Livid other : cloneGroup) {
            if (other != this) {
                double dxDiff = x - other.x;
                double dyDiff = y - other.y;
                double distance = Math.sqrt(dxDiff * dxDiff + dyDiff * dyDiff);
                if (distance < cwidth && distance > 0) {
                    double repelStrength = (cwidth - distance) / cwidth * moveSpeed;
                    double repelDx = (dxDiff / distance) * repelStrength;
                    dx += repelDx;
                }
            }
        }

        if (dx > maxSpeed) dx = maxSpeed;
        else if (dx < -maxSpeed) dx = -maxSpeed;

        if (falling) {
            dy += fallSpeed;
            if (dy > maxFallSpeed) dy = maxFallSpeed;
        } else {
            dy = 0;
        }
    }

    @Override
    public void update() {
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) {
                flinching = false;
            }
        }

        long elapsed = System.currentTimeMillis() - lastBlindTime;
        if (elapsed > BLIND_COOLDOWN && isRealBoss) {
            triggerBlind();
            lastBlindTime = System.currentTimeMillis();
        }

        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < ATTACK_RANGE && System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN) {
            fireCard();
            lastAttackTime = System.currentTimeMillis();
        }

        if (currentAction == ATTACKING && animation.hasPlayedOnce()) {
            setAnimation(IDLE);
        } else if (dx != 0 && currentAction != ATTACKING) {
            setAnimation(WALKING);
        } else if (dx == 0 && currentAction != ATTACKING) {
            setAnimation(IDLE);
        }

        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).update();
            if (cards.get(i).shouldRemove()) {
                cards.remove(i);
                i--;
            }
        }

        animation.update();
    }

    @Override
    public void draw(Graphics2D g) {
        setMapPosition();
        for (CardProjectile card : cards) {
            card.draw(g);
        }
        super.draw(g);
    }

    @Override
    public boolean isBoss() {
        return true; // Mark SluggerBoss as a boss
    }

    public ArrayList<CardProjectile> getCards() { return cards; }
    public String getSuitType() { return suitType; }
    public boolean isReal() { return isRealBoss; }
}