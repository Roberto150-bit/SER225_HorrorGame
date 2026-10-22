package ScriptActions;

import Level.ScriptState;

//When player clicks button, the note is picked up and its page is added to the journal
//Class is used in NoteScript to pick up a story note

public class PickupScriptAction extends ScriptAction {

    private final int page;

    // page is which journal page (1-5) this note holds
    public PickupScriptAction(int page) {
        this.page = page;
    }

    @Override
    public ScriptState execute() {
        if (entity != null) {
            entity.setIsHidden(true);
            entity.setIsUncollidable(true);
            entity.markCollected(); // stays gone when the player comes back to this map
            map.getUI().addStoryPage(page);
            map.setBookOpen(true);
        }
        return ScriptState.COMPLETED;
    }
}
