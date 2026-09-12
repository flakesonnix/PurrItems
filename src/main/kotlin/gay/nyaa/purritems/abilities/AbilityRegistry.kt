package gay.nyaa.purritems.abilities

import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for ability implementations.
 */
class AbilityRegistry {
    private val abilities = ConcurrentHashMap<String, Ability>()

    fun register(ability: Ability) {
        val existing = abilities.putIfAbsent(ability.id, ability)
        require(existing == null) { "Ability ${ability.id} already registered" }
    }

    fun get(id: String): Ability? = abilities[id]

    fun contains(id: String): Boolean = abilities.containsKey(id)

    fun all(): Collection<Ability> = abilities.values.toList()
}
