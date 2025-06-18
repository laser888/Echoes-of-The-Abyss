package Entity.Enemies;

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

public class FinalBoss extends Enemy {
    private Player player;
    private ArrayList<Lightning> lightningStrikes;
    private ArrayList<Fire> fireWaves;
    private TerminalTile terminal;
    private GamePanel gamePanel;
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int LIGHTNING = 2;
    private static final int FIRE = 3;
    private static final int CRUMBLING = 4;
    private static final int DORMANT = 5;
    private static final int REVERSE_CRUMBLING = 6;
    private long lastLightningTime;
    private long lastFireTime;
    private long lastMoveTime;
    private static final long LIGHTNING_COOLDOWN = 5000;
    private static final long FIRE_COOLDOWN = 3000;
    private static final int ATTACK_RANGE = 300;
    private static final int CHASE_RANGE = 500;
    private static final int TOLERANCE = 50;
    private int phase;
    private boolean isDormant;
    private long crumblingStartTime;
    private static final long CRUMBLING_DURATION = 2500; // 5 frames at 500ms
    private boolean reverseCrumbling;
    private long reverseCrumblingStartTime;
    private double initialDamage;
    private double initialMoveSpeed;
    private double initialMaxSpeed;
    private boolean phase1Triggered;
    private boolean phase2Triggered;
    private boolean hasPlayedCrumbling;
    private static final double GROUND_Y = 170.0;
    private static final double MIN_X = 100.0;
    private static final double MAX_X = 4900.0;

    public FinalBoss(TileMap tm, Player player, GamePanel gamePanel) {
        super(tm);
        this.player = player;
        this.gamePanel = gamePanel;
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
        this.health = this.maxHealth = 1000;
        this.damage = 50;
        this.initialDamage = damage;
        this.hasPlayedCrumbling = false;
        loadSprites();
        facingRight = true;
        y = GROUND_Y;
    }

    private void loadSprites() {
        BufferedImage spritesheet;
        try {
            spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Bosses/GolemBoss.gif"));
            if (spritesheet == null) throw new IOException("FinalBoss sprite sheet not found");

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
                int x = i * 40;
                int y = 168;
                crumblingFrames[i] = spritesheet.getSubimage(x, y, 40, 42);
                boolean isTransparent = true;
                int nonTransparentPixels = 0;
                for (int px = 0; px < 40 && isTransparent; px++) {
                    for (int py = 0; py < 42; py++) {
                        int alpha = (crumblingFrames[i].getRGB(px, py) >> 24) & 0xFF;
                        if (alpha > 0) {
                            nonTransparentPixels++;
                            if (nonTransparentPixels > 20) {
                                isTransparent = false;
                                break;
                            }
                        }
                    }
                }
                if (isTransparent || crumblingFrames[i].getWidth() == 0 || crumblingFrames[i].getHeight() == 0) {
                    System.out.println("FinalBoss: WARNING: Invalid crumbling frame at index " + i + ", x=" + x + ", y=" + y + ", transparent=" + isTransparent + ", nonTransparentPixels=" + nonTransparentPixels);
                    crumblingFrames[i] = idleFrames[0];
                }
            }
            dormantFrames[0] = spritesheet.getSubimage(0, 210, 40, 42);
            boolean isDormantTransparent = true;
            int nonTransparentDormantPixels = 0;
            for (int px = 0; px < 40 && isDormantTransparent; px++) {
                for (int py = 0; py < 42; py++) {
                    int alpha = (dormantFrames[0].getRGB(px, py) >> 24) & 0xFF;
                    if (alpha > 0) {
                        nonTransparentDormantPixels++;
                        if (nonTransparentDormantPixels > 20) {
                            isDormantTransparent = false;
                            break;
                        }
                    }
                }
            }
            if (isDormantTransparent || dormantFrames[0].getWidth() == 0 || dormantFrames[0].getHeight() == 0) {
                System.out.println("FinalBoss: WARNING: Invalid dormant frame at x=0, y=210, transparent=" + isDormantTransparent + ", nonTransparentPixels=" + nonTransparentDormantPixels);
                dormantFrames[0] = idleFrames[0];
            }

            sprites.add(idleFrames);
            sprites.add(walkingFrames);
            sprites.add(lightningFrames);
            sprites.add(fireFrames);
            sprites.add(crumblingFrames);
            sprites.add(dormantFrames);
            sprites.add(crumblingFrames);

        } catch (Exception e) {
            System.out.println("FinalBoss: Failed to load sprites: " + e.getMessage());
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

    private void setAnimation(int anim) {
        if (currentAction != anim) {
            currentAction = anim;
            BufferedImage[] frames = sprites.get(anim);
            if (anim == REVERSE_CRUMBLING) {
                frames = new BufferedImage[5];
                for (int i = 0; i < 5; i++) {
                    frames[i] = sprites.get(CRUMBLING)[4 - i]; // Reverse order: 4, 3, 2, 1, 0
                }
            }
            animation.setFrames(frames);
            animation.setDelay(anim == WALKING ? 100 : (anim == LIGHTNING || anim == FIRE ? 150 : anim == CRUMBLING || anim == REVERSE_CRUMBLING ? 500 : 400));
            if (anim == CRUMBLING) {
                hasPlayedCrumbling = false;
            } else if (anim == REVERSE_CRUMBLING) {
                animation.update(); // Force initial frame
            }
        }
    }

    private void fireLightning() {
        boolean right = player.getx() > x;
        Lightning lightning = new Lightning(tileMap, right, (int)(150 * (1 + phase * 0.5)));
        double spawnY = player.gety() - 100;
        double warnY = player.gety() - 50;
        lightning.setWarning(player.getx(), warnY);
        lightning.setPosition(player.getx(), spawnY);
        lightningStrikes.add(lightning);
        lastLightningTime = System.currentTimeMillis();
        setAnimation(LIGHTNING);
    }

    private void fireWave() {
        boolean fireRight = player.getx() > x;
        Fire wave = new Fire(tileMap, fireRight, (int)(damage * (1 + phase * 0.5)));
        wave.setPosition(x, y);
        fireWaves.add(wave);
        lastFireTime = System.currentTimeMillis();
        setAnimation(FIRE);
    }

    @Override
    public void setPosition(double x, double y) {
        xtemp = Math.max(MIN_X, Math.min(x, MAX_X));
        super.setPosition(xtemp, GROUND_Y);
        this.y = GROUND_Y;
    }

    @Override
    public void checkTileMapCollision() {
        xtemp = x + dx;
        ytemp = GROUND_Y;
        xtemp = Math.max(MIN_X, Math.min(xtemp, MAX_X));
        y = GROUND_Y;
        falling = false;
    }

    @Override
    public void hit(int damage) {
        if (dead || isDormant || currentAction == CRUMBLING) return;
        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) flinching = false;
            else return;
        }
        health -= damage;
        if (health <= 0) {
            dead = true;
            if (terminal != null) {
                terminal.close();
                terminal = null;
            }
            return;
        }
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

    private void enterDormantPhase() {
        isDormant = true;
        dx = 0;
        dy = 0;
        y = GROUND_Y;
        setAnimation(DORMANT);
        lightningStrikes.clear();
        fireWaves.clear();
        terminal = new TerminalTile((int)x, (int)GROUND_Y, 0, tileMap, gamePanel, (int)(x / tileMap.getTileSize()), (int)(GROUND_Y / tileMap.getTileSize()), true);
    }

    private boolean isPlayerNearbyTerminal(int playerX, int playerY) {
        if (terminal == null) return false;
        double dist = Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - GROUND_Y, 2));
        return dist <= 60;
    }

    private void updateAttributes() {
        damage = (int)(initialDamage * (1 + phase * 0.5));
        moveSpeed = initialMoveSpeed * (1 + phase * 0.3);
        maxSpeed = initialMaxSpeed * (1 + phase * 0.3);
    }

    private void getNextPosition() {
        if (isDormant || currentAction == CRUMBLING || currentAction == REVERSE_CRUMBLING) {
            dx = 0;
            dy = 0;
            return;
        }
        facingRight = player.getx() > x;
        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < CHASE_RANGE && distToPlayer > TOLERANCE) {
            dx = (player.getx() > x) ? moveSpeed : -moveSpeed;
            setAnimation(WALKING);
        } else {
            dx = 0;
            setAnimation(IDLE);
        }
        if (dx > maxSpeed) dx = maxSpeed;
        if (dx < -maxSpeed) dx = -maxSpeed;
        dy = 0;
        lastMoveTime = System.currentTimeMillis();
    }

    @Override
    public void update() {
        if (isDormant) {
            y = GROUND_Y;
            if (terminal != null) {
                terminal.update();
                if (terminal.isSolved()) {
                    isDormant = false;
                    BaseLevelState.inTerminal = false;
                    reverseCrumbling = true;
                    reverseCrumblingStartTime = System.currentTimeMillis();
                    setAnimation(REVERSE_CRUMBLING);
                    terminal.close();
                    terminal = null;
                }
            }
            animation.update();
            return;
        }

        if (currentAction == CRUMBLING) {
            y = GROUND_Y;
            long elapsed = System.currentTimeMillis() - crumblingStartTime;
            if (elapsed > CRUMBLING_DURATION && !hasPlayedCrumbling) {
                hasPlayedCrumbling = true;
                enterDormantPhase();
                return;
            }
            if (!hasPlayedCrumbling) {
                animation.update();
            }
            return;
        }

        if (currentAction == REVERSE_CRUMBLING) {
            y = GROUND_Y;
            long elapsed = System.currentTimeMillis() - reverseCrumblingStartTime;
            int frameIndex = (int)(elapsed / 500); // 0, 1, 2, 3, 4 maps to crumblingFrames[4, 3, 2, 1, 0]
            if (elapsed > CRUMBLING_DURATION) {
                setAnimation(IDLE);
                updateAttributes();
                return;
            }
            animation.update();
            return;
        }

        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) flinching = false;
        }

        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < ATTACK_RANGE && !isDormant) {
            if (System.currentTimeMillis() - lastLightningTime > LIGHTNING_COOLDOWN) fireLightning();
            if (System.currentTimeMillis() - lastFireTime > FIRE_COOLDOWN) fireWave();
        }

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

        if ((currentAction == LIGHTNING || currentAction == FIRE) && animation.hasPlayedOnce()) {
            setAnimation(IDLE);
        } else if (dx != 0 && currentAction != LIGHTNING && currentAction == FIRE) {
            setAnimation(WALKING);
        } else if (currentAction != LIGHTNING && currentAction != FIRE) {
            setAnimation(IDLE);
        }

        animation.update();
    }

    @Override
    public void draw(Graphics2D g) {
        setMapPosition();
        int drawX = (int)(x + xmap - width / 2);
        int drawY = (int)(y + ymap - height);
        int hitboxY = (int)(y + ymap - height);
        if (animation.getImage() != null) {
            if (facingRight) {
                g.drawImage(animation.getImage(), drawX, drawY, width, height, null);
            } else {
                g.drawImage(animation.getImage(), drawX + width, drawY, -width, height, null);
            }
        }
        g.setColor(Color.RED);
        g.drawRect((int)(x + xmap - cwidth / 2), hitboxY, cwidth, cheight);
        if (terminal != null && isDormant) {
            terminal.render(g, (int)tileMap.getx(), (int)tileMap.gety());
            if (isPlayerNearbyTerminal((int)player.getx(), (int)player.gety())) {
                terminal.drawPressEPrompt(g, (int)tileMap.getx(), (int)tileMap.gety());
            }
        }
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    public ArrayList<Lightning> getLightningStrikes() { return lightningStrikes; }
    public ArrayList<Fire> getFireWaves() { return fireWaves; }
    public TerminalTile getTerminal() { return terminal; }

    public void handleKeyPress(int k) {
        if (k == KeyEvent.VK_H) {
            hit(400);
        } else if (k == KeyEvent.VK_E && isDormant && isPlayerNearbyTerminal((int)player.getx(), (int)player.gety())) {
            if (terminal != null) terminal.interact();
        } else if (k == KeyEvent.VK_ESCAPE && isDormant && terminal != null && terminal.isActive()) {
            terminal.close();
            BaseLevelState.inTerminal = false;
        }
    }

    public void mousePressed(int x, int y) {
        if (terminal != null && isDormant) terminal.mousePressed(x, y);
    }
}