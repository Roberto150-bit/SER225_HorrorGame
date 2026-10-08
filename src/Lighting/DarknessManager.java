package Lighting;

import Engine.GraphicsHandler;
import Level.Camera;
import Level.Player;

public class DarknessManager {

    private DarknessController darknessController;


    public DarknessManager(float startingRadius) {
        darknessController = new DarknessController(startingRadius);
    }


    public void update() {
        darknessController.update();
    }


    public void draw(
            GraphicsHandler graphicsHandler,
            Player player,
            Camera camera
    ) {

        float zoom = camera.getZoom();

        // Get the player's center position relative to the camera.
        float playerCenterX =
                (player.getCalibratedXLocation()
                        + player.getWidth() / 2.0f) * zoom;

        float playerCenterY =
                (player.getCalibratedYLocation()
                        + player.getHeight() / 2.0f) * zoom;

        darknessController.draw(
                graphicsHandler,
                playerCenterX,
                playerCenterY
        );
    }


    public void enable() {
        darknessController.enable();
    }


    public void disable() {
        darknessController.disable();
    }


    public void setTargetRadius(float radius) {
        darknessController.setTargetRadius(radius);
    }


    public boolean isEnabled() {
        return darknessController.isEnabled();
    }


    public float getCurrentRadius() {
        return darknessController.getCurrentRadius();
    }
}