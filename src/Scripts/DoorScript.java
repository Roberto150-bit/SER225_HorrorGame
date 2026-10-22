package Scripts;

import java.util.ArrayList;
import java.util.function.Supplier;

import Engine.Key;
import Engine.KeyLocker;
import Engine.KeyBindings;
import Engine.SoundPlayer;
import Level.Map;
import Level.Script;
import Level.ScriptState;
import Puzzles.PuzzleManager;
import ScriptActions.*;
import Utils.Direction;

// Trigger script for doors/entrances -- when the player walks into the trigger, they are moved to another map
// example usage in a Map's loadTriggers():
//     triggers.add(new Trigger(x, y, width, height, new DoorScript(() -> new HallwayMap(), 5, 3, Direction.DOWN)));
// the spawn tile in the destination map should be NEXT to that map's door trigger, not on it, or the player will bounce straight back
// a door can be locked until a puzzle is solved:
//     new DoorScript(() -> new HallwayMap(), 5, 3, Direction.DOWN).lockedUntilSolved("basement_symbol_lock", "The door is sealed.")
public class DoorScript extends Script {
    protected KeyLocker keyLocker = new KeyLocker();
    protected Supplier<Map> destinationMap;
    protected int spawnTileX, spawnTileY;
    protected Direction facingDirection;
    protected Key OPEN_DOOR = Key.E;
    private String openingSound;

    // if set, the door stays locked until the puzzle with this instance id is solved
    private String requiredPuzzleId;
    private String lockedMessage;

    public DoorScript(Supplier<Map> destinationMap, int spawnTileX, int spawnTileY, Direction facingDirection) {
        this(destinationMap, spawnTileX, spawnTileY, facingDirection, null);
    }

    public DoorScript(Supplier<Map> destinationMap, int spawnTileX, int spawnTileY, Direction facingDirection,
                      String openingSound) {
        this.destinationMap = destinationMap;
        this.spawnTileX = spawnTileX;
        this.spawnTileY = spawnTileY;
        this.facingDirection = facingDirection;
        this.openingSound = openingSound;
    }

    // keeps the door locked, showing lockedMessage, until the given puzzle has been solved
    public DoorScript lockedUntilSolved(String puzzleInstanceId, String lockedMessage) {
        this.requiredPuzzleId = puzzleInstanceId;
        this.lockedMessage = lockedMessage;
        return this;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        if (requiredPuzzleId == null) {
            scriptActions.addAll(loadOpenDoorActions());
            return scriptActions;
        }

        // checked each time the door is used, since the puzzle can be solved at any point
        scriptActions.add(new ConditionalScriptAction() {{
            addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                addRequirement(new CustomRequirement() {
                    @Override
                    public boolean isRequirementMet() {
                        return PuzzleManager.isCompleted(requiredPuzzleId);
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
                        return !PuzzleManager.isCompleted(requiredPuzzleId);
                    }
                });
                addScriptAction(new LockPlayerScriptAction());
                addScriptAction(new TextboxScriptAction(lockedMessage));
                addScriptAction(new UnlockPlayerScriptAction());
            }});
        }});
        return scriptActions;
    }

    private ArrayList<ScriptAction> loadOpenDoorActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        if (openingSound != null) {
            scriptActions.add(new ScriptAction() {
                @Override
                public ScriptState execute() {
                    SoundPlayer.play(openingSound);
                    return ScriptState.COMPLETED;
                }
            });
        }
        scriptActions.add(new ChangeMapScriptAction(destinationMap, spawnTileX, spawnTileY, facingDirection));
        return scriptActions;
    }
}
