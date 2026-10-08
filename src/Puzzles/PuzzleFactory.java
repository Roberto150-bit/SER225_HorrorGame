package Puzzles;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class PuzzleFactory {

    // Associates each puzzle type with its creation method.
    private static final Map<PuzzleId, Function<PuzzleConfig, PuzzleMinigame>>
            puzzleTypes = new EnumMap<>(PuzzleId.class);
    
    // Register available puzzle minigames.
    static {
        register(PuzzleId.SYMBOL_LOCK, SymbolLockMinigame::new);
        register(PuzzleId.TUNE_RADIO, TuneRadioMinigame::new);
        register(PuzzleId.HAUNTED_CLOCK, HauntedClockMinigame::new);
        register(PuzzleId.RITUAL_SEQUENCE, RitualSequenceMinigame::new);
    }

    // Registers how a particular minigame should be created.
    public static void register(
            PuzzleId type,
            Function<PuzzleConfig, PuzzleMinigame> creator) {

        if (type == null || creator == null) {
            throw new IllegalArgumentException("Invalid puzzle registration");
        }

        puzzleTypes.put(type, creator);
    }

    // Creates a controller with the correct minigame.
    public static PuzzleController create(PuzzleConfig config) {

        if (config == null) {
            throw new IllegalArgumentException("Puzzle config cannot be null");
        }

        Function<PuzzleConfig, PuzzleMinigame> creator =
                puzzleTypes.get(config.getPuzzleType());

        if (creator == null) {
            throw new IllegalStateException(
                    "Puzzle type not registered: " + config.getPuzzleType()
            );
        }

        return new PuzzleController(config, creator.apply(config));

    }

    // Checks whether a puzzle type has a registered minigame.
    public static boolean isRegistered(PuzzleId type) {
        return puzzleTypes.containsKey(type);
    }
}
