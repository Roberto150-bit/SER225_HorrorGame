package Scripts;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import Engine.Key;
import Engine.KeyLocker;
import Engine.SoundPlayer;
import Level.Map;
import Level.Script;
import Level.ScriptState;
import Level.TextboxItem;
import Puzzles.PuzzleManager;
import ScriptActions.*;
import Utils.Direction;

// Script for doors/entrances -- when the player interacts with the door, they are moved to another map
// example usage in a Map's loadScripts():
//     getMapTile(8, 4).setInteractScript(new DoorScript(() -> new HallwayMap(), 5, 3, Direction.DOWN));
// the spawn tile in the destination map should be NEXT to that map's door, not on it, or the player will bounce straight back
// a door can show a lock and stay locked until a puzzle is solved:
//     new DoorScript(() -> new HallwayMap(), 5, 3, Direction.DOWN).lockedUntilSolved("basement_symbol_lock", "The door is sealed.")
// or until anything else is true (the message can change, e.g. to count enemies left):
//     new DoorScript(...).lockedUntil(() -> bossIsDefeated(), () -> "The door won't open.")
// a door can have several locks; the first one still closed gives its message
public class DoorScript extends Script {
    protected KeyLocker keyLocker = new KeyLocker();
    protected Supplier<Map> destinationMap;
    protected int spawnTileX, spawnTileY;
    protected Direction facingDirection;
    protected Key OPEN_DOOR = Key.E;
    // played whenever a door actually opens (not when it's locked)
    public static final String DEFAULT_OPENING_SOUND = "openingDoor.wav";
    private String openingSound;

    // a condition that has to be true before the door opens, and what the player is told while it isn't
    private static class Lock {
        final BooleanSupplier isOpen;
        final Supplier<String> message;

        Lock(BooleanSupplier isOpen, Supplier<String> message) {
            this.isOpen = isOpen;
            this.message = message;
        }
    }

    private final ArrayList<Lock> locks = new ArrayList<>();

    public DoorScript(Supplier<Map> destinationMap, int spawnTileX, int spawnTileY, Direction facingDirection) {
        this(destinationMap, spawnTileX, spawnTileY, facingDirection, null);
    }

    public DoorScript(Supplier<Map> destinationMap, int spawnTileX, int spawnTileY, Direction facingDirection,
                      String openingSound) {
        this.destinationMap = destinationMap;
        this.spawnTileX = spawnTileX;
        this.spawnTileY = spawnTileY;
        this.facingDirection = facingDirection;
        this.openingSound = openingSound != null ? openingSound : DEFAULT_OPENING_SOUND;
    }

    // keeps the door locked, showing lockedMessage, until the given puzzle has been solved
    public DoorScript lockedUntilSolved(String puzzleInstanceId, String lockedMessage) {
        return lockedUntil(() -> PuzzleManager.isCompleted(puzzleInstanceId), () -> lockedMessage);
    }

    // keeps the door locked until isOpen returns true; lockedMessage is asked for each time the player tries the door
    public DoorScript lockedUntil(BooleanSupplier isOpen, Supplier<String> lockedMessage) {
        locks.add(new Lock(isOpen, lockedMessage));
        return this;
    }

    @Override
    public boolean isLocked() {
        return firstClosedLock() != null;
    }

    private Lock firstClosedLock() {
        for (Lock lock : locks) {
            if (!lock.isOpen.getAsBoolean()) {
                return lock;
            }
        }
        return null;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        if (locks.isEmpty()) {
            scriptActions.addAll(loadOpenDoorActions());
            return scriptActions;
        }

        // checked each time the door is used, since a lock can open at any point
        scriptActions.add(new ConditionalScriptAction() {{
            addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                addRequirement(new CustomRequirement() {
                    @Override
                    public boolean isRequirementMet() {
                        return !isLocked();
                    }
                });
                for (ScriptAction action : loadOpenDoorActions()) {
                    addScriptAction(action);
                }
            }});

            addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                addRequirement(new CustomRequirement() {
                    @Override
                    public boolean isRequirementMet() {
                        return isLocked();
                    }
                });
                addScriptAction(new LockPlayerScriptAction());
                addScriptAction(new LockedMessageScriptAction());
                addScriptAction(new UnlockPlayerScriptAction());
            }});
        }});
        return scriptActions;
    }

    // shows the message of whichever lock is still closed, worked out when the player tries the door
    private class LockedMessageScriptAction extends TextboxScriptAction {
        @Override
        public void setup() {
            Lock lock = firstClosedLock();
            String message = lock == null ? "" : lock.message.get();
            map.getTextbox().addText(new TextboxItem[] { new TextboxItem(message) });
            map.getTextbox().setIsActive(true);
        }
    }

    private ArrayList<ScriptAction> loadOpenDoorActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        scriptActions.add(new ScriptAction() {
            @Override
            public ScriptState execute() {
                SoundPlayer.play(openingSound);
                return ScriptState.COMPLETED;
            }
        });
        scriptActions.add(new ChangeMapScriptAction(destinationMap, spawnTileX, spawnTileY, facingDirection));
        return scriptActions;
    }
}
