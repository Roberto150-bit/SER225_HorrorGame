package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.Ghost;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.SoundPlayer;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.FlagManager;
import Level.GameListener;
import Level.Player;
import Level.TileType;
import Scripts.SimpleTextScript;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

// The huge door to the Necromancer's room, at the top of the Final Seal room.
// It isn't there at first: when the Final Seal breaks, the room shifts and grows, and the door rises out of the floor.
// Once it's up, guardians spawn around the room, and the door stays bound in chains with a red eye seal until every
// one of them is banished. The last one falling breaks the chains (a shake, the chains fall, the doors swing open),
// and that moment is a checkpoint.
// It's only the picture and these events: the door tile's DoorScript does the actual opening, locked on isUnsealed().
// Whether it's open is kept as a flag, so it stays open after leaving the room or restarting from a later checkpoint.
public class GreatDoor extends EnhancedMapTile {
    public static final String UNSEALED_FLAG = "great_door_unsealed";

    // the art is 64x80 and drawn at 2x, the same scale as the room tilesets
    private static final int SCALE = 2;
    public static final int WIDTH = 64 * SCALE;
    public static final int HEIGHT = 80 * SCALE;
    // where the eye seal sits on the art (scaled)
    private static final int SEAL_X = 32 * SCALE;
    private static final int SEAL_Y = 48 * SCALE;

    private static final BufferedImage CLOSED = ImageLoader.loadWithAlpha("great_door_closed.png");
    private static final BufferedImage CHAINS = ImageLoader.loadWithAlpha("great_door_chains.png");
    private static final BufferedImage OPEN = ImageLoader.loadWithAlpha("great_door_open.png");

    // rising out of the floor
    private static final int RISE_FRAMES = 150;
    // how long the light bursts last where the guardians appear
    private static final int SPAWN_FLASH_FRAMES = 45;
    // the unsealing, in frames: the seal flares and the chains rattle, they snap and fall, then the doors open
    private static final int RATTLE_FRAMES = 70;
    private static final int FALL_FRAMES = 30;
    private static final int OPEN_FRAME = RATTLE_FRAMES + FALL_FRAMES;
    private static final int UNSEAL_FRAMES = OPEN_FRAME + 30;

    private static final String[] UNSEAL_LINES = {
        "(The last guardian dissolves into the dark.)",
        "(The chains on the great door snap one by one\nand crash to the floor.)",
        "(Beyond it, stairs lead down into a red glow.)"
    };

    private enum State { HIDDEN, RISING, SEALED, UNSEALING, OPEN }
    private State state = State.HIDDEN;
    private int timer = 0;
    private int glowTimer = 0;
    private int spawnFlashTimer = 0;
    private final Random random = new Random();
    private final ArrayList<Ghost> guardians = new ArrayList<>();
    // called once the door has finished rising, so the room can bring out the guardians
    private Runnable onRisen;

    // location is the top-left corner of the door's art
    public GreatDoor(Point location) {
        super(location.x, location.y, new SpriteSheet(CLOSED, 64, 80), TileType.PASSABLE);
        // the wall it's set into blocks the player, the door itself never does
        setIsUncollidable(true);
    }

    // the room has grown and the door rises from the floor; onRisen runs when it's fully up
    public void rise(Runnable onRisen) {
        this.onRisen = onRisen;
        state = State.RISING;
        timer = 0;
        SoundPlayer.play("openingDoor.wav");
    }

    // already standing (coming back to the room later, or restarting from a checkpoint after it rose)
    public void appear() {
        if (state == State.HIDDEN || state == State.RISING) {
            state = isFlagSet() ? State.OPEN : State.SEALED;
        }
    }

    // the enemies that have to be banished before the door will open
    public void setGuardians(ArrayList<Ghost> guardians) {
        this.guardians.clear();
        this.guardians.addAll(guardians);
        spawnFlashTimer = SPAWN_FLASH_FRAMES;
    }

    public int getGuardiansRemaining() {
        int remaining = 0;
        for (Ghost guardian : guardians) {
            if (guardian.getHealth() > 0) {
                remaining++;
            }
        }
        return remaining;
    }

    public boolean isUnsealed() {
        return state == State.OPEN || isFlagSet();
    }

    // standing and chained shut (not hidden, rising, or in the middle of opening)
    public boolean isSealed() {
        return state == State.SEALED && !isFlagSet();
    }

    public boolean isRising() {
        return state == State.RISING;
    }

    private boolean isFlagSet() {
        FlagManager flags = map == null ? null : map.getFlagManager();
        return flags != null && flags.isFlagSet(UNSEALED_FLAG);
    }

    @Override
    public void update(Player player) {
        super.update(player);
        glowTimer++;
        if (spawnFlashTimer > 0) {
            spawnFlashTimer--;
        }

        if (state == State.RISING) {
            timer++;
            if (timer % 40 == 0) {
                SoundPlayer.play("knife_free/WEAPKnif_Knife hit, Impact, 03.wav");
            }
            if (timer >= RISE_FRAMES) {
                state = State.SEALED;
                SoundPlayer.play("ghostAttackSound.wav");
                if (onRisen != null) {
                    onRisen.run();
                    onRisen = null;
                }
            }
        } else if (state == State.SEALED) {
            if (isFlagSet()) {
                state = State.OPEN;
            } else if (!guardians.isEmpty() && getGuardiansRemaining() == 0) {
                state = State.UNSEALING;
                timer = 0;
                SoundPlayer.play("ghostAttackSound.wav");
            }
        } else if (state == State.UNSEALING) {
            timer++;
            if (timer == RATTLE_FRAMES) {
                SoundPlayer.play("knife_free/WEAPKnif_Knife hit, Impact, 01.wav");
            }
            if (timer == RATTLE_FRAMES + 12) {
                SoundPlayer.play("knife_free/WEAPKnif_Knife hit, Impact, 02.wav");
            }
            if (timer == OPEN_FRAME) {
                SoundPlayer.play("openingDoor.wav");
            }
            if (timer >= UNSEAL_FRAMES) {
                finishUnsealing();
            }
        }
    }

    private void finishUnsealing() {
        state = State.OPEN;
        if (map.getFlagManager() != null) {
            map.getFlagManager().addFlag(UNSEALED_FLAG, true);
        }
        map.setActiveScript(new SimpleTextScript(UNSEAL_LINES));
        // opening the great door is a checkpoint
        for (GameListener listener : map.getListeners()) {
            listener.onCheckpointReached();
        }
    }

    // the camera draws enhanced map tiles through drawBottomLayer, so the door's look for each state is drawn here
    @Override
    public void drawBottomLayer(GraphicsHandler graphicsHandler) {
        if (state == State.HIDDEN) {
            return;
        }
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        Graphics2D g = graphicsHandler.getGraphics();

        if (state == State.RISING) {
            // pushed up out of the floor: everything below the door's foot is cut off, with a shudder as it grinds
            float progress = timer / (float) RISE_FRAMES;
            float eased = 1f - (1f - progress) * (1f - progress);
            int sink = Math.round((1f - eased) * HEIGHT);
            int shake = random.nextInt(3) - 1;
            Shape oldClip = g.getClip();
            g.clipRect(x - 4, y - 4, WIDTH + 8, HEIGHT + 4);
            graphicsHandler.drawImage(CLOSED, x + shake, y + sink, WIDTH, HEIGHT);
            graphicsHandler.drawImage(CHAINS, x + shake, y + sink, WIDTH, HEIGHT);
            g.setClip(oldClip);
            return;
        }

        boolean showOpen = state == State.OPEN || (state == State.UNSEALING && timer >= OPEN_FRAME);
        if (showOpen) {
            graphicsHandler.drawImage(OPEN, x, y, WIDTH, HEIGHT);
            return;
        }

        // the chains rattle harder and harder, then slide down and fade as they fall
        int shakeX = 0;
        int chainDrop = 0;
        float chainAlpha = 1f;
        if (state == State.UNSEALING) {
            if (timer < RATTLE_FRAMES) {
                int strength = 1 + timer * 3 / RATTLE_FRAMES;
                shakeX = random.nextInt(strength * 2 + 1) - strength;
            } else {
                float fall = (timer - RATTLE_FRAMES) / (float) FALL_FRAMES;
                chainDrop = Math.round(fall * fall * 40);
                chainAlpha = Math.max(0f, 1f - fall);
            }
        }
        graphicsHandler.drawImage(CLOSED, x + shakeX / 2, y, WIDTH, HEIGHT);

        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, chainAlpha));
        graphicsHandler.drawImage(CHAINS, x + shakeX, y + chainDrop, WIDTH, HEIGHT);
        g.setComposite(oldComposite);
    }

    // the eye seal pulses red in the dark; while unsealing it flares, and once open red light spills out of the doorway
    @Override
    public void drawAboveDarkness(GraphicsHandler graphicsHandler) {
        if (state == State.HIDDEN) {
            return;
        }
        float x = getCalibratedXLocation();
        float y = getCalibratedYLocation();
        Graphics2D g = graphicsHandler.getGraphics();

        if (state == State.RISING) {
            // dust and grit spraying from the floor along the door's foot
            for (int i = 0; i < 6; i++) {
                float dustX = x + random.nextFloat() * WIDTH;
                float dustY = y + HEIGHT - random.nextFloat() * 18;
                RitualFire.glow(g, dustX, dustY, 6 + random.nextFloat() * 8, new Color(170, 150, 130), 0.5f);
            }
            float sealY = y + SEAL_Y + (1f - timer / (float) RISE_FRAMES) * HEIGHT;
            if (sealY < y + HEIGHT) {
                RitualFire.glow(g, x + SEAL_X, sealY, 40, new Color(220, 20, 30), 0.6f);
            }
        } else if (state == State.SEALED) {
            float pulse = 0.55f + 0.25f * (float) Math.sin(glowTimer / 20.0);
            RitualFire.glow(g, x + SEAL_X, y + SEAL_Y, 46, new Color(220, 20, 30), pulse);
            // the seal itself stays visible in the dark, so the player can find the door
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            graphicsHandler.drawImage(CHAINS, Math.round(x), Math.round(y), WIDTH, HEIGHT);
            g.setComposite(oldComposite);
        } else if (state == State.UNSEALING) {
            if (timer < RATTLE_FRAMES) {
                float flare = timer / (float) RATTLE_FRAMES;
                RitualFire.glow(g, x + SEAL_X, y + SEAL_Y, 46 + flare * 70, new Color(255, 60, 50), 0.6f + 0.4f * flare);
            } else if (timer < OPEN_FRAME) {
                // a burst as the seal breaks
                float burst = 1f - (timer - RATTLE_FRAMES) / (float) FALL_FRAMES;
                RitualFire.glow(g, x + SEAL_X, y + SEAL_Y, 140, new Color(255, 200, 170), burst);
            }
        }

        if (state == State.OPEN || (state == State.UNSEALING && timer >= OPEN_FRAME)) {
            float flicker = 0.5f + 0.08f * (float) Math.sin(glowTimer / 7.0) + 0.05f * random.nextFloat();
            RitualFire.glow(g, x + WIDTH / 2f, y + HEIGHT - 20, 90, new Color(200, 40, 30), flicker);
        }

        // red bursts where the guardians crawl out of the dark
        if (spawnFlashTimer > 0) {
            float strength = spawnFlashTimer / (float) SPAWN_FLASH_FRAMES;
            for (Ghost guardian : guardians) {
                float gx = guardian.getCalibratedBounds().getX() + guardian.getBounds().getWidth() / 2f;
                float gy = guardian.getCalibratedBounds().getY() + guardian.getBounds().getHeight() / 2f - 20;
                RitualFire.glow(g, gx, gy, 70 * (1.4f - strength * 0.4f), new Color(230, 40, 40), strength);
            }
        }
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {
        Frame frame = new FrameBuilder(spriteSheet.getSubImage(0, 0)).withScale(SCALE).build();
        return new GameObject(x, y, frame);
    }
}
