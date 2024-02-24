# Cloth Gamerules Screen

A complete reimplementation of the gamerules menu using **[Cloth Config API](https://modrinth.com/mod/cloth-config)**.

This aims to make this menu more convenient to use, by bringing it some of Cloth's functionalities. Notably:
- Search gamerules by name or by key
- Reset button for each rule.
- Filter rules by mod


## Integration
### Gamerules
Mod developpers don't need to take any action to ensure their gamerules show up in this screen.

In order to figure out which mod a rule belongs to, this menu uses the namespace of the rule's category. Rules that use vanilla categories will inevitably be sorted into the vanilla gamerules.

### Menus
**This mod only replaces the gamerule screen on New World creation**. If other mods use the gamerule screen in other places, they will need to check for this mod's installation, and make the swap on their end.
