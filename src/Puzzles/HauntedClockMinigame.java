
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Random;

public class HauntedClockMinigame implements PuzzleMinigame {

    private final int targetHour;
    private final int targetMinute;
    private final int minuteStep;
    private final PuzzleDifficulty difficulty;

    private final int clueHour;
    private final int clueMinute;
    private final int clueOffset;

    private int currentHour;
    private int currentMinute;
    private boolean solved;
    private boolean incorrectAttempt;

    // Coordinates matching the 500x350 clock artwork.
    private static final int CLOCK_X = 400;
    private static final int CLOCK_Y = 212;

    // Four illustrated adjustment knobs.
    private static final int[] KNOB_X = {
        300, 365, 435, 500
    };

    private static final int KNOB_Y = 330;
    private static final int KNOB_RADIUS = 24;

    // Bottom decorative plaque.
    private static final int SUBMIT_X = 280;
    private static final int SUBMIT_Y = 370;
    private static final int SUBMIT_W = 240;
    private static final int SUBMIT_H = 44;

    private static final Color DARK_INK =
        new Color(75, 35, 38);

    private static final Color GOLD =
        new Color(220, 180, 110);

    public HauntedClockMinigame(PuzzleConfig config) {

        difficulty = config.getDifficulty();

        switch (difficulty) {
            case EASY:
                minuteStep = 15;
                clueOffset = 30;
                break;

            case MEDIUM:
                minuteStep = 10;
                clueOffset = 70;
                break;

            case HARD:
                minuteStep = 5;
                clueOffset = 135;
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown difficulty"
                );
        }

        // Same instance ID produces the same target.
        long seed =
            31L * config.getInstanceId().hashCode()
            + config.getPuzzleType().ordinal();

        seed = 31L * seed + difficulty.ordinal();

        Random random = new Random(seed);

        targetHour = random.nextInt(12) + 1;

        int minutePositions = 60 / minuteStep;
        targetMinute =
            random.nextInt(minutePositions) * minuteStep;

        // Clue is calculated from the actual answer.
        int targetTotal =
            (targetHour % 12) * 60 + targetMinute;

        int clueTotal =
            Math.floorMod(targetTotal - clueOffset, 720);

        clueHour = (clueTotal / 60 == 0)
            ? 12
            : clueTotal / 60;

        clueMinute = clueTotal % 60;

        reset();
    }

    public void adjustHour(int direction) {
        if (solved) return;

        currentHour =
            Math.floorMod(currentHour - 1 + direction, 12)
            + 1;

        incorrectAttempt = false;
    }


    public void adjustMinute(int direction) {

        if (solved) {
            return;
        }

        // Convert the displayed time into total minutes.
        int totalMinutes =
            (currentHour % 12) * 60 + currentMinute;

        // Advance or rewind by the difficulty's minute step.
        totalMinutes = Math.floorMod(
            totalMinutes + direction * minuteStep,
            720
        );

        // Recalculate both hands, including hour rollover.
        currentHour = totalMinutes / 60;

        if (currentHour == 0) {
            currentHour = 12;
        }

        currentMinute = totalMinutes % 60;

        incorrectAttempt = false;
    }


    private void submitTime() {
        if (solved) return;

        if (currentHour == targetHour &&
            currentMinute == targetMinute) {

            solved = true;

        } else {
            incorrectAttempt = true;
        }
    }

    private String formatTime(int hour, int minute) {
        return String.format("%d:%02d", hour, minute);
    }

    // Center text without hardcoding character widths.
    private void drawCentered(
            GraphicsHandler graphicsHandler,
            Graphics2D g,
            String text,
            int centerX,
            int y,
            Font font,
            Color color) {

        g.setFont(font);

        int textWidth =
            g.getFontMetrics().stringWidth(text);

        graphicsHandler.drawString(
            text,
            centerX - textWidth / 2,
            y,
            font,
            color
        );
    }

    @Override
    public void update() {
        // Controlled by the illustrated buttons.
    }


    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        Graphics2D g = graphicsHandler.getGraphics();

        Font headingFont = new Font("Serif", Font.BOLD, 12);
        Font timeFont = new Font("Serif", Font.BOLD, 20);
        Font clueFont = new Font("Serif", Font.PLAIN, 11);

        // LEFT PARCHMENT: Compact clue layout.
        drawCentered(
            graphicsHandler, g,
            "CHIME",
            266, 190,
            new Font("Serif", Font.BOLD, 13),
            DARK_INK
        );

        // The recorded time is generated from the solution.
        drawCentered(
            graphicsHandler, g,
            formatTime(clueHour, clueMinute),
            266, 215,
            new Font("Serif", Font.BOLD, 21),
            DARK_INK
        );

        // Short instructions fit inside the parchment.
        String riddle;

        switch (difficulty) {
            case EASY:
                riddle = "+30 MIN";
                break;

            case MEDIUM:
                riddle = "+1 HR 10";
                break;

            case HARD:
                riddle = "+2 HR 15";
                break;

            default:
                riddle = "";
        }

        drawCentered(
            graphicsHandler, g,
            riddle,
            266, 245,
            new Font("Serif", Font.BOLD, 12),
            DARK_INK
        );


        // RIGHT PARCHMENT: Current selected time.
        drawCentered(
            graphicsHandler, g,
            "TIME",
            535, 190,
            new Font("Serif", Font.BOLD, 12),
            DARK_INK
        );

        drawCentered(
            graphicsHandler, g,
            formatTime(currentHour, currentMinute),
            535, 219,
            timeFont, GOLD
        );

        // CLOCK HANDS: Reflect the selected time.
        Stroke previousStroke = g.getStroke();

        double minuteAngle =
            Math.toRadians(currentMinute * 6 - 90);

        double hourAngle =
            Math.toRadians(
                (currentHour % 12 + currentMinute / 60.0)
                * 30 - 90
            );

        g.setColor(new Color(65, 35, 35));
        g.setStroke(new BasicStroke(
            5,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));

        g.drawLine(
            CLOCK_X,
            CLOCK_Y,
            CLOCK_X + (int)(39 * Math.cos(hourAngle)),
            CLOCK_Y + (int)(39 * Math.sin(hourAngle))
        );

        g.setColor(new Color(120, 65, 45));
        g.setStroke(new BasicStroke(
            3,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));

        g.drawLine(
            CLOCK_X,
            CLOCK_Y,
            CLOCK_X + (int)(60 * Math.cos(minuteAngle)),
            CLOCK_Y + (int)(60 * Math.sin(minuteAngle))
        );

        g.setStroke(previousStroke);

        g.setColor(GOLD);
        g.fillOval(
            CLOCK_X - 5,
            CLOCK_Y - 5,
            10, 10
        );

        // Existing illustrated Submit button.
        drawCentered(
            graphicsHandler, g,
            incorrectAttempt
                ? "TRY AGAIN"
                : "SUBMIT TIME",
            400, 388,
            new Font("Serif", Font.BOLD, 14),
            DARK_INK
        );
    }


    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) return;

        // First, check the four clock controls.
        for (int i = 0; i < KNOB_X.length; i++) {

            int dx = mouseX - KNOB_X[i];
            int dy = mouseY - KNOB_Y;

            if (dx * dx + dy * dy
                    <= KNOB_RADIUS * KNOB_RADIUS) {

                switch (i) {
                    case 0:
                        adjustHour(-1);
                        break;

                    case 1:
                        adjustHour(1);
                        break;

                    case 2:
                        adjustMinute(-1);
                        break;

                    case 3:
                        adjustMinute(1);
                        break;
                }

                return;
            }
        }

        // Then check the Submit plaque.
        boolean clickedSubmit =
            mouseX >= SUBMIT_X &&
            mouseX <= SUBMIT_X + SUBMIT_W &&
            mouseY >= SUBMIT_Y &&
            mouseY <= SUBMIT_Y + SUBMIT_H;

        if (clickedSubmit) {
            submitTime();
        }
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {
        currentHour = 12;
        currentMinute = 0;
        solved = false;
        incorrectAttempt = false;
    }

    public int getCurrentHour() {
        return currentHour;
    }

    public int getCurrentMinute() {
        return currentMinute;
    }

    public int getTargetHour() {
        return targetHour;
    }

    public int getTargetMinute() {
        return targetMinute;
    }
}
