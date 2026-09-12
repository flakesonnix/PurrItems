package gay.nyaa.purritems.domain

/**
 * Item rarity levels.
 */
enum class Rarity(val displayKey: String, val sortOrder: Int) {
    COMMON("rarity.common", 0),
    UNCOMMON("rarity.uncommon", 1),
    RARE("rarity.rare", 2),
    EPIC("rarity.epic", 3),
    LEGENDARY("rarity.legendary", 4),
    MYTHIC("rarity.mythic", 5),
    ;

    companion object {
        fun fromString(str: String): Rarity? = entries.find { it.name.equals(str, ignoreCase = true) }
    }
}
