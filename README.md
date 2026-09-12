# PurrItems

Paper 1.21+ — Kotlin — custom items with abilities, stats, and RPG mechanics.

Create items with unique abilities, stats, rarity tiers, and upgrade paths. Integrates with PurrSkills for requirements.

## Features

- **Custom Items**: Define items with unique IDs, stats, and abilities
- **Ability System**: Trigger abilities on attack, break, interact, etc.
- **Rarity Tiers**: Common, Uncommon, Rare, Epic, Legendary with color-coded names
- **Stats System**: Damage, defense, speed, crit chance, etc.
- **Upgrade System**: Upgrade items through tiers with materials
- **Requirements**: Skill level, permission, or other requirements to use items
- **PDC Storage**: All item data stored in PersistentDataContainer

## Item Definition Example

Items are defined in config files:

```yaml
items:
  fire_sword:
    material: DIAMOND_SWORD
    rarity: EPIC
    display_name: "&6Flame Blade"
    stats:
      damage: 15
      crit_chance: 0.2
    abilities:
      - type: FIRE_STRIKE
        trigger: ATTACK
        cooldown: 5
        power: 10
    requirements:
      - type: SKILL_LEVEL
        skill: COMBAT
        level: 20
```

## Commands

- `/purritems give <player> <item_id> [amount]` - Give custom item
- `/purritems list` - List all registered items
- `/purritems reload` - Reload item definitions

## Abilities

Built-in abilities:
- **FireStrike**: Set enemies on fire on hit
- **Lightning**: Strike lightning at target
- **Dash**: Quick movement burst
- More coming soon...

## Building

```bash
gradle shadowJar
# → build/libs/purritems-1.0.0.jar
```

Requires `PurrCore` for database, optionally `PurrSkills` for skill requirements.

## Testing

```bash
gradle test
# 122 unit tests covering domain, abilities, serialization, upgrades
```

Coverage: 55% (13/29 files) - all critical business logic tested.

## Development

Items are registered via `ItemRegistry`. Create custom abilities by implementing `Ability` interface and registering with `AbilityRegistry`.

See `docs/ITEMS.md` for full item definition schema.
