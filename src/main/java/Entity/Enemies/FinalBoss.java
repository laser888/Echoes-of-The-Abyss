package Entity.Enemies;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Entity.Projectiles.Fire;
import Entity.Projectiles.Lightning;
import TileMap.TileMap;
import TileMap.TerminalTile;
import Main.GamePanel;

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
    private long lastLightningTime;
    private long lastFireTime;
    private static final long LIGHTNING_COOLDOWN = 5000;
    private static final long FIRE_COOLDOWN = 3000;
    private static final int ATTACK_RANGE = 300;
    private static final int CHASE_RANGE = 500;
    private static final int TOLERANCE = 80;
    private int phase;
    private boolean isDormant;
    private long crumblingStartTime;
    private static final long CRUMBLING_DURATION = 5400;
    private double initialDamage;
    private double initialMoveSpeed;
    private double initialMaxSpeed;
    private boolean phase1Triggered;
    private boolean phase2Triggered;
    private static final double GROUND_Y = 170.0;

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
        loadSprites();
        facingRight = true;
        y = GROUND_Y;
    }

    private void loadSprites() {
        BufferedImage spritesheet = null;
        try {
            spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Bosses/GolemBoss.gif"));
            if (spritesheet == null) {
                throw new IOException("FinalBoss sprite sheet not found");
            }
        } catch (IOException e) {
            System.out.println("FinalBoss: Failed to load sprite: " + e.getMessage());
            spritesheet = new BufferedImage(40, 252, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = spritesheet.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, 40, 252);
            g.dispose();
            System.out.println("FinalBoss: Using magenta placeholder sprite, phase=" + phase);
        }

        sprites = new ArrayList<>();
        BufferedImage[] idleFrames = new BufferedImage[1];
        BufferedImage[] walkingFrames = new BufferedImage[2];
        BufferedImage[] lightningFrames = new BufferedImage[2];
        BufferedImage[] fireFrames = new BufferedImage[1];
        BufferedImage[] crumblingFrames = new BufferedImage[9];
        BufferedImage[] dormantFrames = new BufferedImage[1];

        try {
            idleFrames[0] = spritesheet.getSubimage(0, 0, 40, 42);
            for (int i = 0; i < 2; i++) {
                walkingFrames[i] = spritesheet.getSubimage(i * 40, 42, 40, 42);
                lightningFrames[i] = spritesheet.getSubimage(i * 40, 84, 40, 42);
            }
            fireFrames[0] = spritesheet.getSubimage(0, 126, 40, 42);
            for (int i = 0; i < 9; i++) {
                crumblingFrames[i] = spritesheet.getSubimage(i % 5 * 40, 168 + (i / 5) * 42, 40, 42);
            }
            dormantFrames[0] = spritesheet.getSubimage(0, 210, 40, 42);

        } catch (Exception e) {
            System.out.println("FinalBoss: Failed to extract frames: " + e.getMessage());
            BufferedImage placeholder = new BufferedImage(40, 42, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, 40, 42);
            g.dispose();
            idleFrames[0] = fireFrames[0] = dormantFrames[0] = placeholder;
            for (int i = 0; i < 2; i++) {
                walkingFrames[i] = lightningFrames[i] = placeholder;
            }
            for (int i = 0; i < 9; i++) {
                crumblingFrames[i] = placeholder;
            }
        }

        sprites.add(idleFrames);
        sprites.add(walkingFrames);
        sprites.add(lightningFrames);
        sprites.add(fireFrames);
        sprites.add(crumblingFrames);
        sprites.add(dormantFrames);

        animation = new Animation();
        setAnimation(IDLE);
    }

    private void setAnimation(int anim) {
        if (currentAction != anim) {
            currentAction = anim;
            animation.setFrames(sprites.get(anim));
            int delay;
            if (anim == WALKING) {
                delay = 100;
            } else if (anim == LIGHTNING || anim == FIRE) {
                delay = 150;
            } else if (anim == CRUMBLING) {
                delay = 600;
            } else {
                delay = 400;
            }
            animation.setDelay(delay);
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
        super.setPosition(x, GROUND_Y);
        this.y = GROUND_Y;
    }

    @Override
    public void checkTileMapCollision() {
        xtemp = x + dx;
        ytemp = GROUND_Y;
        super.checkTileMapCollision();
        y = GROUND_Y;
        falling = false;
    }

    @Override
    public void hit(int damage) {
        if (dead || isDormant || currentAction == CRUMBLING) {
            return;
        }
        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) {
                flinching = false;
            } else {
                return;
            }
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
        if (health <= maxHealth * 2.0 / 3.0 && !phase1Triggered) {
            phase = 1;
            phase1Triggered = true;
            setAnimation(CRUMBLING);
            crumblingStartTime = System.currentTimeMillis();
            lightningStrikes.clear();
            fireWaves.clear();
        } else if (health <= maxHealth * 1.0 / 3.0 && !phase2Triggered) {
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
        terminal = new TerminalTile((int)x, (int)GROUND_Y, 17, tileMap, gamePanel, (int)(x / tileMap.getTileSize()), (int)(GROUND_Y / tileMap.getTileSize()), true);
        if (terminal == null) {
            System.out.println("FinalBoss: WARNING: Terminal creation failed, terminal is null");
        }
    }

    private boolean isPlayerNearbyTerminal(int playerX, int playerY) {
        if (terminal == null) {
            System.out.println("FinalBoss: Terminal is null, cannot check proximity");
            return false;
        }
        double dist = Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - GROUND_Y, 2));
        boolean nearby = dist <= 200;
        //System.out.println("FinalBoss: Player proximity check: dist=" + dist + ", nearby=" + nearby + ", playerX=" + playerX + ", playerY=" + playerY + ", bossX=" + x + ", terminalY=" + GROUND_Y + ", screenX=" + (x + tileMap.getx()) + ", screenY=" + (GROUND_Y + tileMap.gety()));
        return nearby;
    }

    private void updateAttributes() {
        damage = (int) (initialDamage * (1 + phase * 0.5));
        moveSpeed = initialMoveSpeed * (1 + phase * 0.3);
        maxSpeed = initialMaxSpeed * (1 + phase * 0.3);
    }

    private void getNextPosition() {
        if (isDormant || currentAction == CRUMBLING) {
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
        if (dx != 0) {
            if (dx > maxSpeed) dx = maxSpeed;
            if (dx < -maxSpeed) dx = -maxSpeed;
        }
        dy = 0;
    }

    @Override
    public void update() {
        if (isDormant) {
            y = GROUND_Y;
            if (terminal != null) {
                //System.out.println("FinalBoss: Dormant, inTerminal=" + GameState.BaseLevelState.inTerminal + ", simonActive=" + terminal.isActive() + ", simonSolved=" + terminal.isSolved());
                if (terminal.isSolved()) {
                    isDormant = false;
                    setAnimation(IDLE);
                    updateAttributes();
                    terminal.close();
                    terminal = null;
                    //System.out.println("FinalBoss: Exiting dormant phase " + phase + " due to puzzle solved");
                }
            }
            animation.update();
            return;
        }

        if (currentAction == CRUMBLING) {
            y = GROUND_Y;
            long elapsed = System.currentTimeMillis() - crumblingStartTime;
            if (elapsed > CRUMBLING_DURATION) {
                enterDormantPhase();
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
            if (elapsed > 1000) {
                flinching = false;
            }
        }

        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < ATTACK_RANGE && !isDormant) {
            if (System.currentTimeMillis() - lastLightningTime > LIGHTNING_COOLDOWN) {
                fireLightning();
            }
            if (System.currentTimeMillis() - lastFireTime > FIRE_COOLDOWN) {
                fireWave();
            }
        }

        for (int i = 0; i < lightningStrikes.size(); i++) {
            Lightning lightning = lightningStrikes.get(i);
            lightning.update();
            if (lightning.shouldRemove()) {
                lightningStrikes.remove(i);
                i--;
            }
        }

        for (int i = 0; i < fireWaves.size(); i++) {
            Fire fire = fireWaves.get(i);
            fire.update();
            if (fire.shouldRemove()) {
                fireWaves.remove(i);
                i--;
            }
        }

        if ((currentAction == LIGHTNING || currentAction == FIRE) && animation.hasPlayedOnce()) {
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
            g.setColor(Color.RED);
            g.fillOval((int)(x + xmap - 5), (int)(GROUND_Y + ymap - 5), 10, 10);
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
            System.out.println("FinalBoss: 'E' pressed near terminal, starting puzzle");
            if (terminal != null) {
                terminal.interact();
            }
        }
    }

    public void mousePressed(int x, int y) {}
}