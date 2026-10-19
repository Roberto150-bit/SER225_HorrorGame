package Engine;

// how an item in the hotbar behaves when the player uses it
public enum ItemUse {
    NONE,       // can't be used (notes, rocks)
    SINGLE_USE, // rare relics (eye, ear, crucifix), thrown for 3 damage and removed after one use
    REUSABLE    // knife, swung for 1 damage and stays in the hotbar
}
