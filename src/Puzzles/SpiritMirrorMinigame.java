
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;

public class SpiritMirrorMinigame implements PuzzleMinigame {

    private final int fragmentCount;

    private int[] currentRotations;
    private int[] targetRotations;
    private boolean solved;

    // Configure the number of fragments based on difficulty.
    public SpiritMirrorMinigame(PuzzleConfig config) {

        switch (config.getDifficulty()) {
            case EASY:
                fragmentCount = 3;
                break;

            case MEDIUM:
                fragmentCount = 4;
                break;

            case HARD:
                fragmentCount = 5;
                break;

            default:
                throw new IllegalArgumentException("Unknown difficulty");
        }

        currentRotations = new int[fragmentCount];
        targetRotations = new int[fragmentCount];

        reset();
    }

    // Rotate one fragment clockwise by 90 degrees.
    public void rotateFragment(int fragmentIndex) {

        if (solved || fragmentIndex < 0 ||
            fragmentIndex >= fragmentCount) {
            return;
        }

        currentRotations[fragmentIndex] =
            (currentRotations[fragmentIndex] + 1) % 4;

        checkSolution();
    }

    // Complete the puzzle when all fragments are aligned.
    private void checkSolution() {

        for (int i = 0; i < fragmentCount; i++) {
            if (currentRotations[i] != targetRotations[i]) {
                return;
            }
        }

        solved = true;
    }

    @Override
    public void update() {
        // No automatic updates required.
    }


    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        graphicsHandler.drawString(
            "SPIRIT MIRROR",
            300, 185,
            new Font("Serif", Font.BOLD, 28),
            Color.WHITE
        );

        graphicsHandler.drawString(
            "Rotate each fragment to match its target",
            240, 230,
            new Font("Arial", Font.PLAIN, 18),
            Color.LIGHT_GRAY
        );

        // Center fragments based on the selected difficulty.
        int fragmentSize = 65;
        int spacing = 12;
        int totalWidth = fragmentCount * fragmentSize
            + (fragmentCount - 1) * spacing;
        int startX = 400 - totalWidth / 2;

        for (int i = 0; i < fragmentCount; i++) {

            int fragmentX = startX + i * (fragmentSize + spacing);
            int fragmentY = 265;

            // Green indicates that the fragment is correctly aligned.
            boolean aligned = currentRotations[i] == targetRotations[i];

            Color fragmentColor = aligned
                ? new Color(55, 120, 90)
                : new Color(75, 85, 105);

            graphicsHandler.drawFilledRectangle(
                fragmentX, fragmentY,
                fragmentSize, fragmentSize,
                fragmentColor
            );

            // Show the fragment's current rotation in degrees.
            graphicsHandler.drawString(
                (currentRotations[i] * 90) + "°",
                fragmentX + 8, fragmentY + 38,
                new Font("Arial", Font.BOLD, 17),
                Color.WHITE
            );
        }

        graphicsHandler.drawString(
            "Click fragments to rotate them 90 degrees",
            245, 365,
            new Font("Arial", Font.PLAIN, 17),
            Color.LIGHT_GRAY
        );

        // Display completion feedback.
        if (solved) {
            graphicsHandler.drawString(
                "REFLECTION RESTORED",
                285, 410,
                new Font("Serif", Font.BOLD, 22),
                Color.GREEN
            );
        }
    }

    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) {
            return;
        }

        // Use the same positions as the drawing method.
        int fragmentSize = 65;
        int spacing = 12;
        int totalWidth = fragmentCount * fragmentSize
            + (fragmentCount - 1) * spacing;
        int startX = 400 - totalWidth / 2;

        for (int i = 0; i < fragmentCount; i++) {

            int fragmentX = startX + i * (fragmentSize + spacing);

            if (mouseX >= fragmentX &&
                mouseX <= fragmentX + fragmentSize &&
                mouseY >= 265 &&
                mouseY <= 265 + fragmentSize) {

                rotateFragment(i);
                return;
            }
        }
    }


    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {
        solved = false;

        // Temporary target rotations for each difficulty.
        for (int i = 0; i < fragmentCount; i++) {
            currentRotations[i] = 0;
            targetRotations[i] = (i % 3) + 1;
        }
    }

    public int getFragmentCount() {
        return fragmentCount;
    }

    public int getCurrentRotation(int index) {
        return currentRotations[index];
    }

    public int getTargetRotation(int index) {
        return targetRotations[index];
    }
}
