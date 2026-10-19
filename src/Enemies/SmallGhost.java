package Enemies;

import Utils.Point;

// smaller, faster ghost with 1 health
public class SmallGhost extends Ghost {
    public static final int SMALL_GHOST_HEALTH = 1;
    public static final int SMALL_GHOST_XP_REWARD = 5;

    public SmallGhost(int id, Point location) {
        super(id, location, SMALL_GHOST_HEALTH);
        speed = 1.8f;
        followDistance = 45f;
        xpReward = SMALL_GHOST_XP_REWARD;
    }

    @Override
    protected float getSpriteScale() {
        return 2;
    }
}
