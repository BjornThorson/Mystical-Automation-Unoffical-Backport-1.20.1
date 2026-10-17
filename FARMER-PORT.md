CURRENT VERIFIED FARMER CHECKPOINT: 7 new Farmer runtime tests, 35/35 total Minecraft Forge GameTests passed. All 17 Farmer processing recipes load, including alternate dirt/sand Sugar Cane soil, and the Farmer machine's shaped crafting recipe registers. The original Farmer inventory has 16 backing slots, with active input/fuel/output indices 0 through 12 and three reserved slots. Server processing works; dedicated GUI still pending.

# Farmer: Forge 1.20.1 server engine milestone

Status: the Farmer has a real registered Forge block entity, dedicated server ticker, custom recipe type/serializer, 17 converted recipe JSON files and six passing Farmer Minecraft GameTests. Prior baseline: 33 passed; new inventory parity check must be retested across the backport. The Farmer GUI is not yet implemented. This is a development checkpoint, NOT ready for Steph's real world.

## Fidelity to original Mystical Automation
- Farmer is a recipe-driven virtual crop grower, **not** an area-harvesting block.
- Reusable slot 0 seeds, slot 1 farmland/soil, slot 2 optional crux, slot 3 furnace fuel; output slots 4 through 12. Original 16-slot handler preserves reserved slots 13 through 15.
- Base recipe progression: 800 operation ticks, 20 FE per tick; stage threshold uses operation time multiplied by configured recipe's 7 stages, and operation progress thereafter.
- The 17 upstream Farmer recipes are ported into Forge 1.20.1 recipes/farmer/*.json; the upstream shaped machine recipe is separately converted.
- Result rolls respect the upstream chance and item quantities, using Minecraft's server random source. Input seeds/soil are not consumed on successful harvest.
- Before committing harvest, all randomized results are checked against a simulated nine-slot output inventory. If they cannot fit, they are not discarded.
- Machine-upgrade item interface uses existing published Mystical Agriculture 1.20.1 items and its real multipliers.
- Supports Forge item-handler top inputs, fuel on horizontal sides and downward output extraction, alongside FE capabilities.
- Inventory, energy, growth stages, progress, fuel and installed upgrade persist in NBT.
- Original 1.21.1 source remains unchanged; this is an unofficial MIT-licensed port.

## In-Minecraft verification
The following Farmer tests passed alongside all earlier Infuser/Crafter tests:
1. Upstream Farmer custom recipe registry and recipe loading.
2. Harvesting Inferium Essence with an Awakened Supremium upgrade while retaining seeds and farmland.
3. No harvest without FE.
4. No item loss from fully occupied output slots.
5. Upgrade, growth stage and FE state survive NBT save/load.

## Remaining critical work
- The original Farmer container/screen, upgrade slot and player-facing input interaction (currently only internal/test/Forge capability access).
- End-to-end recipes across all crop seed/soil/tier combinations, chance distributions and crux behaviour, including optional mod data.
- More complete automated item/external mod interoperability, actual chunk save-and-reopen, multiplayer/client rendering and gameplay balance checks.
- Dedicated area/side configuration and original GUI running/progress overlays.
- Functional ports of Fertilizer, Infusion Altarnator, Awakening Altarnator and Enchanternator (4 machines still shells).

Do NOT install this build in Steph's live world.


## Data compatibility discovered by exhaustive tests
- Older Minecraft/Forge JSON machine recipes require result.item, not the 1.21 result.id field.
- Farmer soil/seeds/crux JSON may contain arrays of alternate ingredients. Serializer now reads generic JsonElement with Ingredient.fromJson instead of requiring objects.
- Added checks that all 17 processing recipes and the Farmer machine crafting recipe load in the real Minecraft server.
- The original Farmer handler had 16 slots; updated block registration and static checks from an earlier incorrect assumption of 13.
- Real 1.20.1 gameplay GUI is still missing; no finished-world installation.
