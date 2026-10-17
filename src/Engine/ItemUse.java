package Engine;

// how an item in the hotbar behaves when the player uses it
public enum ItemUse {
    NONE,       // can't be used (notes, rocks)
    SINGLE_USE, // defensive item that disappears after one use (eye, ear, crucifix)
    REUSABLE    // defensive item that stays in the hotbar after use (knives, bow and arrow, etc.)
}
