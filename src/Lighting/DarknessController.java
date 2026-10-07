package Lighting;

import Engine.GraphicsHandler;

public class DarknessController {

    private DarknessEffect darknessEffect;

    // Determines whether the darkness is currently visible.
    private boolean enabled;

    // Radius currently being shown on screen.
    private float currentRadius;

    // Radius the effect is trying to reach.
    private float targetRadius;

    // How quickly the radius expands or contracts.
    private float transitionSpeed;


    public DarknessController(float startingRadius) {
        darknessEffect = new DarknessEffect();

        enabled = true;

        currentRadius = startingRadius;
        targetRadius = startingRadius;

        transitionSpeed = 3.0f;
    }


    public void update() {

        // Expand toward the target radius.
        if (currentRadius < targetRadius) {
            currentRadius += transitionSpeed;

            // Prevent the radius from going past the target.
            if (currentRadius > targetRadius) {
                currentRadius = targetRadius;
            }
        }

        // Contract toward the target radius.
        else if (currentRadius > targetRadius) {
            currentRadius -= transitionSpeed;

            // Prevent the radius from going past the target.
            if (currentRadius < targetRadius) {
                currentRadius = targetRadius;
            }
        }
    }


    public void draw(
            GraphicsHandler graphicsHandler,
            float centerX,
            float centerY
    ) {

        // If darkness is disabled, nothing is drawn.
        if (!enabled) {
            return;
        }

        darknessEffect.draw(
                graphicsHandler,
                centerX,
                centerY,
                currentRadius
        );
    }


    public void enable() {
        enabled = true;
    }


    public void disable() {
        enabled = false;
    }


    public void expandTo(float radius) {
        targetRadius = radius;
    }


    public void contractTo(float radius) {
        targetRadius = radius;
    }


    public void transitionTo(float radius) {
        targetRadius = radius;
    }


    public boolean isEnabled() {
        return enabled;
    }


    public float getCurrentRadius() {
        return currentRadius;
    }
}