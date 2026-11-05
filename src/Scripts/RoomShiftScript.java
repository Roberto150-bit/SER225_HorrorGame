package Scripts;

import Engine.SoundPlayer;
import Level.GameListener;
import Level.Script;
import Level.ScriptState;
import Maps.DungeonRundownRoomMap;
import ScriptActions.LockPlayerScriptAction;
import ScriptActions.ScriptAction;
import ScriptActions.TextboxScriptAction;
import ScriptActions.UnlockPlayerScriptAction;

import java.util.ArrayList;

// Plays in the Final Seal room right after the seal is broken:
// the room rumbles, everything goes dark while the walls draw back and the room grows,
// then the great door rises out of the floor and its guardians crawl out of the shadows.
public class RoomShiftScript extends Script {

    public static final String[] SEAL_BREAKS_LINES = {
        "(The seal cracks down the middle.\nThe whole room begins to shake.)",
        "(Stone grinds against stone.\nThe walls are moving.)"
    };

    public static final String[] DOOR_RISEN_LINES = {
        "(A great door has risen where the wall stood.)",
        "(Shapes crawl out of the shadows to guard it.\nIt won't open while they remain.)"
    };

    private static final int RUMBLE_FRAMES = 70;

    private final DungeonRundownRoomMap room;

    public RoomShiftScript(DungeonRundownRoomMap room) {
        this.room = room;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> actions = new ArrayList<>();
        actions.add(new LockPlayerScriptAction());
        actions.add(new TextboxScriptAction(SEAL_BREAKS_LINES));

        // a rumble, then the room reshapes itself while the screen is black
        actions.add(new ScriptAction() {
            private int timer;
            private boolean faded;

            @Override
            public void setup() {
                timer = 0;
                faded = false;
                SoundPlayer.play("openingDoor.wav");
                for (GameListener listener : map.getListeners()) {
                    listener.onScreenShake(RUMBLE_FRAMES + 60);
                }
            }

            @Override
            public ScriptState execute() {
                timer++;
                if (timer == RUMBLE_FRAMES && !faded) {
                    faded = true;
                    for (GameListener listener : map.getListeners()) {
                        listener.onFadeAndChange(() -> {
                            room.expand();
                            room.placePlayerForDoorRise(player);
                        });
                    }
                }
                return room.isExpanded() && timer > RUMBLE_FRAMES + 40 ? ScriptState.COMPLETED : ScriptState.RUNNING;
            }
        });

        // the door rises (and the guardians appear when it's up)
        actions.add(new ScriptAction() {
            @Override
            public void setup() {
                room.raiseGreatDoor();
                for (GameListener listener : map.getListeners()) {
                    listener.onScreenShake(150);
                }
            }

            @Override
            public ScriptState execute() {
                return room.getGreatDoor().isRising() ? ScriptState.RUNNING : ScriptState.COMPLETED;
            }
        });

        actions.add(new TextboxScriptAction(DOOR_RISEN_LINES));
        actions.add(new UnlockPlayerScriptAction());
        return actions;
    }
}
