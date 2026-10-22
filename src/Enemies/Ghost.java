package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.SoundPlayer;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.MapEntityStatus;
import Level.NPC;
import Level.Player;
import Level.PlayerProgressManager;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.HashMap;

// A ghost enemy that slowly floats toward the player, but keeps a short gap instead of overlapping them
// when close enough it lunges at the player, and the player loses a heart if it hits
// has 3 health, the knife does 1 damage and relics do 3
public class Ghost extends NPC {
    protected float speed = 1.4f;
    protected float followDistance = 60f;

    // Tracks when an obstacle prevents the ghost from moving.
    protected int stuckFrames = 0;
    protected boolean isStuck = false;
    
    // Temporary direction used to navigate around obstacles.
    protected int avoidanceFrames = 0;
    protected int avoidanceDirection = 1;

    // Maximum distance at which the ghost detects the player.
    protected float detectionRadius = 220f;

    // health values
    public static final int DEFAULT_HEALTH = 3;
    protected int maxHealth;
    protected int health;

    // xp given when banished (puzzles give 50)
    public static final int DEFAULT_XP_REWARD = 15;
    protected int xpReward = DEFAULT_XP_REWARD;

    // attack values
    private static final float ATTACK_RANGE = 80f; // distance to start an attack
    private static final float HIT_RANGE = 95f; // distance where the lunge still hits
    private static final int ATTACK_COOLDOWN = 90; // frames to wait between attacks
    private static final int LUNGE_START = 20; // frame the lunge starts on
    private static final int LUNGE_FRAMES = 8;
    private static final float LUNGE_SPEED = 2f;
    private static final int HIT_FRAME = 24; // frame the hit lands on
    private static final int ATTACK_LENGTH = 44; // total frames of the ATTACK animation
    private boolean isAttacking = false;
    private int attackTimer = 0;
    private int attackCooldown = ATTACK_COOLDOWN;

    // hurt values
    private static final int HURT_FRAMES = 20;
    private static final int KNOCKBACK_FRAMES = 8;
    private static final float KNOCKBACK_SPEED = 3f;
    private int hurtTimer = 0;

    // banish values
    private static final int BANISH_FRAMES = 45;
    private boolean isBanished = false;
    private int banishTimer = 0;

    public Ghost(int id, Point location) {
        this(id, location, DEFAULT_HEALTH);
    }

    public Ghost(int id, Point location, int maxHealth) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Ghost.png"), 16, 22), "STAND_LEFT");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    // sprite scale, SmallGhost uses a smaller one
    protected float getSpriteScale() {
        return 3;
    }

    @Override
    public boolean isEnemy() {
        return true;
    }

    public int getHealth() {
        return health;
    }

    public boolean isWithinDetectionRadius(Player player) {
        if (isBanished || health <= 0 || player == null) {
            return false;
        }

        float dx = (player.getBounds().getX()
                + player.getBounds().getWidth() / 2f)
                - (getBounds().getX()
                + getBounds().getWidth() / 2f);

        float dy = (player.getBounds().getY()
                + player.getBounds().getHeight() / 2f)
                - (getBounds().getY()
                + getBounds().getHeight() / 2f);

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // A nearby ghost may follow even if it is attacking
        // or temporarily stopped close to Dean.
        return distance <= detectionRadius;
    }

    public boolean canFollowThroughDoor(Player player) {
        return isWithinDetectionRadius(player);
    }



    @Override
    public void performAction(Player player) {
        if (isBanished) {
            moveY(-0.6f);
            banishTimer--;
            if (banishTimer <= 0) {
                setMapEntityStatus(MapEntityStatus.REMOVED);
            }
            return;
        }

        float dx = (player.getBounds().getX() + player.getBounds().getWidth() / 2f) - (getBounds().getX() + getBounds().getWidth() / 2f);
        float dy = (player.getBounds().getY() + player.getBounds().getHeight() / 2f) - (getBounds().getY() + getBounds().getHeight() / 2f);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // knockback after being hit
        if (hurtTimer > 0) {
            hurtTimer--;
            if (hurtTimer > HURT_FRAMES - KNOCKBACK_FRAMES && distance > 0) {
                moveXHandleCollision(-dx / distance * KNOCKBACK_SPEED);
                moveYHandleCollision(-dy / distance * KNOCKBACK_SPEED);
            }
            return;
        }

        if (isAttacking) {
            performAttack(player, dx, dy, distance);
            return;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Stay idle until the player enters the detection radius.
        if (distance > detectionRadius) {
            currentAnimationName =
                currentAnimationName.contains("RIGHT")
                    ? "STAND_RIGHT"
                    : "STAND_LEFT";
            return;
        }

        // don't attack while the player is reading, in a cutscene, or doing a puzzle
        boolean playerIsBusy = player.isLocked() || map.isBookOpen() || map.getPuzzleUI().isOpen() || map.getTextbox().isActive();
        if (distance <= ATTACK_RANGE && attackCooldown == 0 && !playerIsBusy && !player.isDead()) {
            SoundPlayer.play("ghostAttackSound.wav");
            isAttacking = true;
            attackTimer = 0;
            currentAnimationName = dx < 0 ? "ATTACK_LEFT" : "ATTACK_RIGHT";
            return;
        }

        if (distance > followDistance) {
            if (dx < 0) {
                currentAnimationName = "WALK_LEFT";
            } else if (dx > 0) {
                currentAnimationName = "WALK_RIGHT";
            }
            float movedX = 0;
            float movedY = 0;

            if (avoidanceFrames > 0) {
                // Move perpendicular to the player to get around an obstacle.
                if (Math.abs(dx) >= Math.abs(dy)) {
                    movedY = moveYHandleCollision(avoidanceDirection * speed);
                } else {
                    movedX = moveXHandleCollision(avoidanceDirection * speed);
                }

                avoidanceFrames--;

                // Try the opposite side if this direction is blocked.
                if (Math.abs(movedX) + Math.abs(movedY) < 0.01f) {
                    avoidanceDirection *= -1;
                    avoidanceFrames = 45;
                }
            } else {
                // Normal chase behavior.
                if (Math.abs(dx) > 1) {
                    movedX = moveXHandleCollision(Math.signum(dx) * speed);
                }

                if (Math.abs(dy) > 1) {
                    movedY = moveYHandleCollision(Math.signum(dy) * speed);
                }

                boolean attemptedMovement =
                        Math.abs(dx) > 1 || Math.abs(dy) > 1;

                boolean movementBlocked =
                        Math.abs(movedX) + Math.abs(movedY) < 0.01f;

                if (attemptedMovement && movementBlocked) {
                    stuckFrames++;
                } else {
                    stuckFrames = 0;
                }

                // Begin navigating around the obstacle.
                if (stuckFrames >= 15) {
                    avoidanceFrames = 45;
                    stuckFrames = 0;
                }
            }

            isStuck = avoidanceFrames > 0;

            
        } else {
            currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
        }
    }

    // lunges at the player and hits partway through the animation
    private void performAttack(Player player, float dx, float dy, float distance) {
        attackTimer++;

        if (attackTimer > LUNGE_START && attackTimer <= LUNGE_START + LUNGE_FRAMES && distance > 0) {
            moveXHandleCollision(dx / distance * LUNGE_SPEED);
            moveYHandleCollision(dy / distance * LUNGE_SPEED);
        }

        if (attackTimer == HIT_FRAME && distance <= HIT_RANGE) {
            player.takeDamage();
        }

        if (attackTimer >= ATTACK_LENGTH) {
            stopAttacking();
        }
    }

    private void stopAttacking() {
        isAttacking = false;
        attackCooldown = ATTACK_COOLDOWN;
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
    }

    // takes damage from the knife or a relic, banished at 0 health
    @Override
    public void hitByDefensiveItem(int damage) {
        if (isBanished) {
            return;
        }
        health = Math.max(0, health - damage);
        if (isAttacking) {
            stopAttacking();
        }
        if (health == 0) {
            PlayerProgressManager.awardXP(xpReward);
            isBanished = true;
            banishTimer = BANISH_FRAMES;
            currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
        } else {
            hurtTimer = HURT_FRAMES;
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        float scale = getSpriteScale();
        // GhostAttack.png frames: 0 rise, 1 turn red, 2 lunge with mouth open, 3 recover
        SpriteSheet attackSheet = new SpriteSheet(ImageLoader.load("GhostAttack.png"), 16, 22);
        return new HashMap<String, Frame[]>() {{
            put("STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .build()
            });
            put("STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });
            put("WALK_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .build()
            });
            put("WALK_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });
            // frame delays add up to ATTACK_LENGTH
            put("ATTACK_LEFT", new Frame[] {
                    new FrameBuilder(attackSheet.getSprite(0, 0), 12)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .build(),
                    new FrameBuilder(attackSheet.getSprite(0, 1), 8)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .build(),
                    new FrameBuilder(attackSheet.getSprite(0, 2), 14)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .build(),
                    new FrameBuilder(attackSheet.getSprite(0, 3), 10)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .build()
            });
            put("ATTACK_RIGHT", new Frame[] {
                    new FrameBuilder(attackSheet.getSprite(0, 0), 12)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(attackSheet.getSprite(0, 1), 8)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(attackSheet.getSprite(0, 2), 14)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build(),
                    new FrameBuilder(attackSheet.getSprite(0, 3), 10)
                            .withScale(scale)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });
        }};
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        Composite oldComposite = g.getComposite();

        if (isBanished) {
            // fade out while being banished
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, banishTimer / (float) BANISH_FRAMES)));
        } else if (hurtTimer > 0 && (hurtTimer / 3) % 2 == 0) {
            // flicker after being hit
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }
        super.draw(graphicsHandler);
        g.setComposite(oldComposite);

        // health bar shows once damaged
        if (!isBanished && health < maxHealth) {
            drawHealthBar(graphicsHandler);
        }
    }

    private void drawHealthBar(GraphicsHandler graphicsHandler) {
        int width = Math.round(getBounds().getWidth());
        int x = Math.round(getCalibratedBounds().getX());
        int y = Math.round(getCalibratedYLocation()) - 8;
        int filledWidth = Math.round(width * health / (float) maxHealth);
        graphicsHandler.drawFilledRectangle(x, y, width, 5, new Color(40, 0, 0));
        graphicsHandler.drawFilledRectangle(x, y, filledWidth, 5, new Color(200, 30, 40));
    }
}
