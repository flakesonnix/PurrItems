package gay.nyaa.purritems.domain

/**
 * Unique identifier for custom items.
 * Format: "PURR_ITEMS:ITEM_NAME"
 */
data class ItemId(val namespace: String, val key: String) {
    init {
        require(namespace.isNotBlank()) { "Namespace cannot be blank" }
        require(key.isNotBlank()) { "Key cannot be blank" }
        require(namespace.matches(Regex("[a-z0-9_]+"))) { "Namespace must be lowercase alphanumeric + underscore" }
        require(key.matches(Regex("[A-Z0-9_]+"))) { "Key must be uppercase alphanumeric + underscore" }
    }

    override fun toString(): String = "$namespace:$key"

    companion object {
        fun parse(str: String): ItemId {
            val parts = str.split(":")
            require(parts.size == 2) { "Invalid ItemId format: $str (expected namespace:key)" }
            return ItemId(parts[0], parts[1])
        }

        fun of(namespace: String, key: String) = ItemId(namespace, key)
    }
}
