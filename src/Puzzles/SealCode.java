package Puzzles;

import java.util.Random;

// the code for the final seal in the rundown room
// a new random code is made each playthrough, and each code note found around the dungeon reveals one digit of it
public class SealCode {
    public static final int LENGTH = 5;

    private static final Random random = new Random();
    private static String code = makeCode();
    private static final boolean[] found = new boolean[LENGTH];

    private static String makeCode() {
        StringBuilder newCode = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            newCode.append(random.nextInt(10));
        }
        return newCode.toString();
    }

    // called when a new game starts
    public static void reset() {
        code = makeCode();
        for (int i = 0; i < LENGTH; i++) {
            found[i] = false;
        }
    }

    public static String getCode() {
        return code;
    }

    public static char getDigit(int index) {
        return code.charAt(index);
    }

    public static void markFound(int index) {
        found[index] = true;
    }

    public static boolean hasFoundAnyDigit() {
        for (boolean digitFound : found) {
            if (digitFound) {
                return true;
            }
        }
        return false;
    }

    // e.g. "7 _ 3 _ _" -- digits the player has found so far, used by the journal
    public static String getFoundDigits() {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            if (i > 0) {
                digits.append(' ');
            }
            digits.append(found[i] ? code.charAt(i) : '_');
        }
        return digits.toString();
    }

    // prevents SealCode from being instantiated
    private SealCode() { }
}
