package Entity.Enemies.Bosses;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Entity.Projectiles.Fire;
import Entity.Projectiles.Lightning;
import TileMap.TileMap;
import TileMap.TerminalTile;
import Main.GamePanel;
import GameState.BaseLevelState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

// Controls the final boss behavior and attacks
public class FinalBoss extends Enemy {
    private Player player; // Player reference
    private ArrayList<Lightning> lightningStrikes; // Lightning attack list
    private ArrayList<Fire> fireWaves; // Fire wave attack list
    private TerminalTile terminal; // Terminal for dormant phase
    private GamePanel gamePanel; // Game panel for rendering
    // Animation states
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int LIGHTNING = 2;
    private static final int FIRE = 3;
    private static final int CRUMBLING = 4;
    private static final int DORMANT = 5;
    private static final int REVERSE_CRUMBLING = 6;
    private long lastLightningTime; // Last lightning attack time
    private long lastFireTime; // Last fire attack time
    private long lastMoveTime; // Last movement time
    private static final long LIGHTNING_COOLDOWN = 5000; // Lightning cooldown (ms)
    private static final long FIRE_COOLDOWN = 3000; // Fire cooldown (ms)
    private static final int ATTACK_RANGE = 300; // Attack range (pixels)
    private static final int CHASE_RANGE = 500; // Chase range (pixels)
    private static final int TOLERANCE = 50; // Movement tolerance (pixels)
    private int phase; // Current phase (0-2)
    private boolean isDormant; // Dormant state flag
    private long crumblingStartTime; // Crumbling animation start
    private static final long CRUMBLING_DURATION = 2500; // Crumbling duration (ms)
    private boolean reverseCrumbling; // Reverse crumbling flag
    private long reverseCrumblingStartTime; // Reverse crumbling start
    private double initialDamage; // Base damage
    private double initialMoveSpeed; // Base move speed
    private double initialMaxSpeed; // Base max speed
    private boolean phase1Triggered; // Phase 1 trigger flag
    private boolean phase2Triggered; // Phase 2 trigger flag
    private static final double GROUND_Y = 170.0; // Ground Y position
    private static final double MIN_X = 100.0; // Min X position
    private static final double MAX_X = 4900.0; // Max X position

    // Initializes the final boss
    public FinalBoss(TileMap tm, Player player, GamePanel gamePanel) {
        super(tm);
        this.player = player;
        this.gamePanel = gamePanel != null ? gamePanel : new GamePanel(); // Defaults to empty panel
        this.lightningStrikes = new ArrayList<>();
        this.fireWaves = new ArrayList<>();
        this.name = "Final Boss";
        this.phase = 0;
        this.isDormant = false;
        this.moveSpeed = 1.0;
        this.maxSpeed = 1.5;
        this.initialMoveSpeed = moveSpeed;
        this.initialMaxSpeed = maxSpeed;
        this.fallSpeed = 0.0;
        this.maxFallSpeed = 0.0;
        this.falling = false;
        this.width = 40;
        this.height = 42;
        this.cwidth = 40;
        this.cheight = 45;
        this.health = this.maxHealth = 6000;
        this.damage = 40;
        this.initialDamage = damage;
        loadSprites();
        facingRight = true;
        y = GROUND_Y;
    }

    // Loads boss sprites from sprite sheet
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Bosses/GolemBoss.gif"));
            if (spritesheet == null) throw new IOException("Sprite sheet not found");

            sprites = new ArrayList<>();
            BufferedImage[] idleFrames = new BufferedImage[1];
            BufferedImage[] walkingFrames = new BufferedImage[2];
            BufferedImage[] lightningFrames = new BufferedImage[2];
            BufferedImage[] fireFrames = new BufferedImage[1];
            BufferedImage[] crumblingFrames = new BufferedImage[5];
            BufferedImage[] dormantFrames = new BufferedImage[1];

            idleFrames[0] = spritesheet.getSubimage(0, 0, 40, 42);
            for (int i = 0; i < 2; i++) {
                walkingFrames[i] = spritesheet.getSubimage(i * 40, 42, 40, 42);
                lightningFrames[i] = spritesheet.getSubimage(i * 40, 84, 40, 42);
            }
            fireFrames[0] = spritesheet.getSubimage(0, 126, 40, 42);
            for (int i = 0; i < 5; i++) {
                crumblingFrames[i] = spritesheet.getSubimage(i * 40, 168, 40, 42);
            }
            dormantFrames[0] = spritesheet.getSubimage(0, 210, 40, 42);

            sprites.add(idleFrames);
            sprites.add(walkingFrames);
            sprites.add(lightningFrames);
            sprites.add(fireFrames);
            sprites.add(crumblingFrames);
            sprites.add(dormantFrames);
            sprites.add(crumblingFrames); // For REVERSE_CRUMBLING
        } catch (IOException e) {
            // Creates placeholder sprite on failure
            sprites = new ArrayList<>();
            BufferedImage placeholder = new BufferedImage(40, 42, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, 40, 42);
            g.dispose();
            BufferedImage[] frames = {placeholder};
            for (int i = 0; i < 7; i++) sprites.add(frames);
        }
        animation = new Animation();
        setAnimation(IDLE);
    }

    // Sets the current animation state
    private void setAnimation(int anim) {
        if (currentAction != anim && animation != null) {
            currentAction = anim;
            BufferedImage[] frames = sprites.get(Math.min(anim, sprites.size() - 1));
            if (anim == REVERSE_CRUMBLING) {
                // Reverses crumbling frames
                frames = new BufferedImage[5];
                for (int i = 0; i < 5; i++) {
                    frames[i] = sprites.get(CRUMBLING)[4 - i];
                }
            }
            animation.setFrames(frames);
            animation.setDelay(anim == WALKING ? 100 : (anim == LIGHTNING || anim == FIRE ? 150 : anim == CRUMBLING || anim == REVERSE_CRUMBLING ? 500 : 400));
        }
    }

    // Fires lightning at player
    private void fireLightning() {
        if (player == null) return;
        boolean right = player.getx() > x;
        Lightning lightning = new Lightning(tileMap, right, (int)(100 * (1 + phase * 0.5)));
        double spawnY = player.gety() - 100;
        double warnY = player.gety() - 50;
        lightning.setWarning(player.getx(), warnY);
        lightning.setPosition(player.getx(), spawnY);
        lightningStrikes.add(lightning);
        lastLightningTime = System.currentTimeMillis();
        setAnimation(LIGHTNING);
    }

    // Fires a wave of fire
    private void fireWave() {
        if (player == null) return;
        boolean fireRight = player.getx() > x;
        Fire wave = new Fire(tileMap, fireRight, (int)(damage * (1 + phase * 0.5)));
        wave.setPosition(x, y);
        fireWaves.add(wave);
        lastFireTime = System.currentTimeMillis();
        setAnimation(FIRE);
    }

    // Sets boss position with bounds
    @Override
    public void setPosition(double x, double y) {
        xtemp = Math.max(MIN_X, Math.min(x, MAX_X));
        super.setPosition(xtemp, GROUND_Y);
        this.y = GROUND_Y;
    }

    // Checks collision with tile map
    @Override
    public void checkTileMapCollision() {
        xtemp = x + dx;
        ytemp = GROUND_Y;
        xtemp = Math.max(MIN_X, Math.min(xtemp, MAX_X));
        y = GROUND_Y;
        falling = false;
    }

    // Handles damage taken
    @Override
    public void hit(int damage) {
        if (dead || isDormant || currentAction == CRUMBLING) return;
        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) flinching = false;
            else return;
        }
        health = Math.max(0, health - Math.max(0, damage));
        if (health == 0) {
            // Terminates boss and closes terminal
            dead = true;
            if (terminal != null) {
                terminal.close();
                terminal = null;
            }
            return;
        }
        // Triggers phase changes
        if (health <= maxHealth * 2.0 / 3.0 && !phase1Triggered && currentAction != CRUMBLING) {
            phase = 1;
            phase1Triggered = true;
            setAnimation(CRUMBLING);
            crumblingStartTime = System.currentTimeMillis();
            lightningStrikes.clear();
            fireWaves.clear();
        } else if (health <= maxHealth * 1.0 / 3.0 && !phase2Triggered && currentAction != CRUMBLING) {
            phase = 2;
            phase2Triggered = true;
            setAnimation(CRUMBLING);
            crumblingStartTime = System.currentTimeMillis();
            lightningStrikes.clear();
            fireWaves.clear();
        }
        flinching = true;
        flinchTimer = System.nanoTime();
    }

    // Enters dormant phase with terminal
    private void enterDormantPhase() {
        isDormant = true;
        dx = 0;
        dy = 0;
        y = GROUND_Y;
        setAnimation(DORMANT);
        lightningStrikes.clear();
        fireWaves.clear();
        if (gamePanel != null && tileMap != null) {
            terminal = new TerminalTile((int)x, (int)GROUND_Y, 0, tileMap, gamePanel,
                    (int)(x / tileMap.getTileSize()), (int)(GROUND_Y / tileMap.getTileSize()), true);
        }
    }

    // Checks if player is near terminal
    private boolean isPlayerNearbyTerminal(int playerX, int playerY) {
        if (terminal == null) return false;
        double dist = Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - GROUND_Y, 2));
        return dist <= 60;
    }

    // Updates boss attributes based on phase
    private void updateAttributes() {
        damage = (int)(initialDamage * (1 + phase * 0.5));
        moveSpeed = initialMoveSpeed * (1 + phase * 0.3);
        maxSpeed = initialMaxSpeed * (1 + phase * 0.3);
    }

    // Calculates next position
    private void getNextPosition() {
        if (isDormant || currentAction == CRUMBLING || currentAction == REVERSE_CRUMBLING) {
            dx = 0;
            dy = 0;
            return;
        }
        if (player == null) return;
        facingRight = player.getx() > x;
        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < CHASE_RANGE && distToPlayer > TOLERANCE) {
            // Chases player
            dx = (player.getx() > x) ? moveSpeed : -moveSpeed;
            setAnimation(WALKING);
        } else {
            dx = 0;
            setAnimation(IDLE);
        }
        dx = Math.max(-maxSpeed, Math.min(dx, maxSpeed));
        dy = 0;
        lastMoveTime = System.currentTimeMillis();
    }

    // Updates boss state and attacks
    @Override
    public void update() {
        if (player == null || tileMap == null) return;
        if (isDormant) {
            y = GROUND_Y;
            if (terminal != null) {
                terminal.update();
                if (terminal.isSolved()) {
                    // Exits dormant phase
                    isDormant = false;
                    BaseLevelState.inTerminal = false;
                    reverseCrumbling = true;
                    reverseCrumblingStartTime = System.currentTimeMillis();
                    setAnimation(REVERSE_CRUMBLING);
                    terminal.close();
                    terminal = null;
                }
            }
            if (animation != null) animation.update();
            return;
        }

        if (currentAction == CRUMBLING) {
            y = GROUND_Y;
            long elapsed = System.currentTimeMillis() - crumblingStartTime;
            if (elapsed > CRUMBLING_DURATION) {
                enterDormantPhase();
                return;
            }
            if (animation != null) animation.update();
            return;
        }

        if (currentAction == REVERSE_CRUMBLING) {
            y = GROUND_Y;
            long elapsed = System.currentTimeMillis() - reverseCrumblingStartTime;
            if (elapsed > CRUMBLING_DURATION) {
                setAnimation(IDLE);
                updateAttributes();
                return;
            }
            if (animation != null) animation.update();
            return;
        }

        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) flinching = false;
        }

        // Triggers attacks
        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < ATTACK_RANGE && !isDormant) {
            if (System.currentTimeMillis() - lastLightningTime > LIGHTNING_COOLDOWN) fireLightning();
            if (System.currentTimeMillis() - lastFireTime > FIRE_COOLDOWN) fireWave();
        }

        // Updates projectiles
        for (int i = 0; i < lightningStrikes.size(); i++) {
            Lightning lightning = lightningStrikes.get(i);
            lightning.update();
            if (lightning.shouldRemove()) lightningStrikes.remove(i--);
        }
        for (int i = 0; i < fireWaves.size(); i++) {
            Fire fire = fireWaves.get(i);
            fire.update();
            if (fire.shouldRemove()) fireWaves.remove(i--);
        }

        // Manages animation state
        if ((currentAction == LIGHTNING || currentAction == FIRE) && animation != null && animation.hasPlayedOnce()) {
            setAnimation(IDLE);
        } else if (dx != 0 && currentAction != LIGHTNING && currentAction != FIRE) {
            setAnimation(WALKING);
        } else if (currentAction != LIGHTNING && currentAction != FIRE) {
            setAnimation(IDLE);
        }

        if (animation != null) animation.update();
    }

    // Draws boss and terminal
    @Override
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) return;
        setMapPosition();
        int drawX = (int)(x + xmap - width / 2);
        int drawY = (int)(y + ymap - height);
        if (animation != null && animation.getImage() != null) {
            // Renders boss sprite
            if (facingRight) {
                g.drawImage(animation.getImage(), drawX, drawY, width, height, null);
            } else {
                g.drawImage(animation.getImage(), drawX + width, drawY, -width, height, null);
            }
        }
    }

    // Identifies as boss
    @Override
    public boolean isBoss() {
        return true;
    }

    // Returns lightning strikes
    public ArrayList<Lightning> getLightningStrikes() {
        return lightningStrikes;
    }

    // Returns fire waves
    public ArrayList<Fire> getFireWaves() {
        return fireWaves;
    }

    // Returns terminal
    public TerminalTile getTerminal() {
        return terminal;
    }

    // Handles key input
    public void handleKeyPress(int k) {
        if (player == null || terminal == null) return;
        if (k == KeyEvent.VK_E && isDormant && isPlayerNearbyTerminal((int)player.getx(), (int)player.gety())) {
            terminal.interact();
        } else if (k == KeyEvent.VK_ESCAPE && isDormant && terminal.isActive()) {
            terminal.close();
            BaseLevelState.inTerminal = false;
        }
    }

    // Handles mouse input
    public void mousePressed(int x, int y) {
        if (terminal != null && isDormant) terminal.mousePressed(x, y);
    }
}