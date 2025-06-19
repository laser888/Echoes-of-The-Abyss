package Entity.Enemies.Bosses;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Entity.Projectiles.Card;
import Main.GamePanel;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

// Controls Clone Boss and its clones
public class CloneBoss extends Enemy {
    private Player player; // Player reference
    private ArrayList<Card> cards; // Card projectile list
    private ArrayList<CloneBoss> cloneGroup; // Clone group reference
    private String suitType; // Boss suit (Diamond, Heart, Spade)
    private boolean isRealBoss; // Real boss flag
    private Random rand; // Random number generator
    // Animation states
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int ATTACKING = 2;
    private long lastAttackTime; // Last attack time
    private static final long ATTACK_COOLDOWN = 2000; // Attack cooldown (ms)
    private long lastBlindTime; // Last blind effect time
    private static final long BLIND_COOLDOWN = 10000; // Blind cooldown (ms)
    private static final int ATTACK_RANGE = 300; // Attack range (pixels)
    private boolean shouldBlindPlayer; // Blind effect flag
    private static HashMap<String, BufferedImage> spriteSheets = new HashMap<>(); // Cached sprite sheets

    // Loads sprite sheets for all suits
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
                sheet = ImageIO.read(CloneBoss.class.getResourceAsStream(paths[i]));
                if (sheet == null) throw new IOException("Sprite sheet not found: " + paths[i]);
            } catch (IOException e) {
                sheet = new BufferedImage(105, 135, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = sheet.createGraphics();
                g.setColor(Color.RED);
                g.fillRect(0, 0, 105, 135);
                g.dispose();
            }
            spriteSheets.put(suits[i], sheet);
        }
    }

    // Initializes Livid boss
    public CloneBoss(TileMap tm, Player player, String suitType, boolean isRealBoss, ArrayList<CloneBoss> cloneGroup) {
        super(tm);
        this.player = player;
        this.suitType = (suitType != null) ? suitType : "Diamond"; // Defaults to Diamond
        this.isRealBoss = isRealBoss;
        this.cloneGroup = (cloneGroup != null) ? cloneGroup : new ArrayList<>(); // Defaults to empty list
        this.cards = new ArrayList<>();
        this.rand = new Random();
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
        loadSprites(spriteSheets.get(this.suitType));
        right = false;
        left = false;
    }

    // Loads boss sprites
    private void loadSprites(BufferedImage spritesheet) {
        sprites = new ArrayList<>();
        BufferedImage[] idleFrames = new BufferedImage[1];
        BufferedImage[] walkingFrames = new BufferedImage[6];
        BufferedImage[] attackingFrames = new BufferedImage[2];

        try {
            if (spritesheet == null || spritesheet.getWidth() < width * 6 || spritesheet.getHeight() < height * 3) {
                throw new IOException("Invalid sprite sheet");
            }
            idleFrames[0] = spritesheet.getSubimage(0, 0, width, height);
            for (int i = 0; i < 6; i++) {
                walkingFrames[i] = spritesheet.getSubimage(i * width, height, width, height);
            }
            for (int i = 0; i < 2; i++) {
                attackingFrames[i] = spritesheet.getSubimage(i * width, height * 2, width, height);
            }
        } catch (IOException e) {
            // Creates placeholder sprites
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, width, height);
            g.dispose();
            idleFrames[0] = placeholder;
            for (int i = 0; i < 6; i++) walkingFrames[i] = placeholder;
            for (int i = 0; i < 2; i++) attackingFrames[i] = placeholder;
        }

        sprites.add(idleFrames);
        sprites.add(walkingFrames);
        sprites.add(attackingFrames);

        animation = new Animation();
        setAnimation(IDLE);
    }

    // Sets animation state
    private void setAnimation(int anim) {
        if (currentAction != anim && animation != null) {
            currentAction = anim;
            animation.setFrames(sprites.get(Math.min(anim, sprites.size() - 1)));
            animation.setDelay(anim == WALKING ? 50 : (anim == ATTACKING ? 100 : 400));
        }
    }

    // Triggers blind effect
    public void triggerBlind() {
        if (isRealBoss) shouldBlindPlayer = true;
    }

    // Checks if blind effect should apply
    public boolean shouldBlindPlayer() {
        if (shouldBlindPlayer) {
            shouldBlindPlayer = false;
            return true;
        }
        return false;
    }

    // Fires card projectile
    private void fireCard() {
        if (cards.size() >= 3 || player == null) return;
        boolean fireRight = player.getx() > x;
        Card card = new Card(tileMap, fireRight, damage);
        card.setPosition(x, y);
        cards.add(card);
        lastAttackTime = System.currentTimeMillis();
        setAnimation(ATTACKING);
    }

    // Handles damage taken
    @Override
    public void hit(int damage) {
        if (dead || flinching) return;
        health = Math.max(0, health - Math.max(0, damage));
        if (health <= 0) {
            dead = true;
            if (!isRealBoss && cloneGroup != null) {
                // Resets real boss health
                for (CloneBoss cb : cloneGroup) {
                    if (cb.isRealBoss && !cb.dead) {
                        cb.health = cb.maxHealth;
                        break;
                    }
                }
            }
        }
        flinching = true;
        flinchTimer = System.nanoTime();
    }

    // Calculates next position
    private void getNextPosition() {
        if (player == null) return;
        double minDistance = 50.0;
        double distToPlayer = player.getx() - x;
        double absDistToPlayer = Math.abs(distToPlayer);

        if (absDistToPlayer < minDistance) {
            dx = 0;
        } else if (absDistToPlayer < ATTACK_RANGE) {
            // Moves toward player
            double targetDx = (distToPlayer > 0 ? moveSpeed : -moveSpeed);
            dx = (dx + targetDx) / 2;
            facingRight = distToPlayer > 0;
            left = !facingRight;
            right = facingRight;
        } else {
            dx = 0;
            left = false;
            right = false;
        }

        // Repels from other clones
        if (cloneGroup != null) {
            for (CloneBoss other : cloneGroup) {
                if (other != this) {
                    double dxDiff = x - other.x;
                    double dyDiff = y - other.y;
                    double distance = Math.sqrt(dxDiff * dxDiff + dyDiff * dyDiff);
                    if (distance < cwidth && distance > 0) {
                        double repelStrength = (cwidth - distance) / cwidth * moveSpeed;
                        dx += (dxDiff / distance) * repelStrength;
                    }
                }
            }
        }

        dx = Math.max(-maxSpeed, Math.min(dx, maxSpeed));
        if (falling) {
            dy += fallSpeed;
            dy = Math.min(dy, maxFallSpeed);
        } else {
            dy = 0;
        }
    }

    // Updates boss state
    @Override
    public void update() {
        if (player == null || tileMap == null) return;
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) flinching = false;
        }

        // Triggers blind effect
        if (isRealBoss && System.currentTimeMillis() - lastBlindTime > BLIND_COOLDOWN) {
            triggerBlind();
            lastBlindTime = System.currentTimeMillis();
        }

        // Triggers attack
        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < ATTACK_RANGE && System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN) {
            fireCard();
        }

        // Updates animation
        if (currentAction == ATTACKING && animation != null && animation.hasPlayedOnce()) {
            setAnimation(IDLE);
        } else if (dx != 0 && currentAction != ATTACKING) {
            setAnimation(WALKING);
        } else if (dx == 0 && currentAction != ATTACKING) {
            setAnimation(IDLE);
        }

        // Updates projectiles
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            card.update();
            if (card.shouldRemove()) cards.remove(i--);
        }

        if (animation != null) animation.update();
    }

    // Draws boss and cards
    @Override
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) return;
        setMapPosition();
        for (Card card : cards) {
            card.draw(g);
        }
        super.draw(g);
    }

    // Identifies as boss
    @Override
    public boolean isBoss() {
        return true;
    }

    // Returns card projectiles
    public ArrayList<Card> getCards() {
        return cards;
    }

    // Returns suit type
    public String getSuitType() {
        return suitType;
    }

    // Checks if real boss
    public boolean isReal() {
        return isRealBoss;
    }
}