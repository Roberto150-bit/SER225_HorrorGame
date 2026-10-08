package Engine;

public final class KeyBindings {

    private static final Key[] movement = {
        Key.W, Key.A, Key.S, Key.D
    };

    private KeyBindings() {}

    public static Key get(int index) {
        return movement[index];
    }

    public static boolean set(int index, Key key) {

        // Only allow letter keys.
        if (key == null ||
            key.ordinal() < Key.A.ordinal() ||
            key.ordinal() > Key.Z.ordinal()) {
            return false;
        }

        // Reserve existing gameplay keys.
        if (key == Key.C || key == Key.E || key == Key.Q) {
            return false;
        }

        // Prevent duplicate movement bindings.
        for (int i = 0; i < movement.length; i++) {
            if (i != index && movement[i] == key) {
                return false;
            }
        }

        movement[index] = key;
        return true;
    }
}
