package Engine;

import GameObject.SpriteSheet;
import Level.Player;

import java.awt.Color;

public class HotbarUI {
    
    private SpriteSheet itemSpriteSheet;

    private int slotX = 30;
    private int slotY = 480;
    private int slotSize = 60;

    public HotbarUI() {
        itemSpriteSheet = new SpriteSheet(ImageLoader.load("Rock.png"), 16, 16);
    }

    public void draw(Player player, GraphicsHandler graphicsHandler) {

        // draw hotbar slot
        graphicsHandler.drawRectangle(
                slotX,
                slotY,
                slotSize,
                slotSize,
                Color.WHITE,
                3
        );

        // only show item if player collected it
        if (player.hasCollectible()) {
            graphicsHandler.drawImage(
                    itemSpriteSheet.getSubImage(0, 0),
                    slotX + 10,
                    slotY + 10,
                    40,
                    40
            );
        }
    }
}
