# Crafter: Forge 1.20.1 port checkpoint

## Issue 006 real-client visual inspection (18 October 2026) - ACCEPTED

The actual Forge graphical client successfully loaded the disposable QA world and opened the Crafter GUI with a right-click. The blue nine-slot ghost blueprint, nine real-material slots, furnace-fuel slot, output, upgrade slot, inventory, and FE/progress readings were visible. Original GUI texture is byte-for-byte identical to upstream. Found a concrete label defect: `Progress: 0/200` crosses the blueprint and process area and clips beyond the right edge. A second captured image without the Minecraft tutorial overlay independently shows the upgrade slot.

Evidence: `qa-evidence/issue-006/crafter-gui-clean.jpg`, `crafter-gui-detail.jpg`, and `crafter-gui-tutorial-overlay.jpg`; available on GitHub's review-only branch. No Java/recipe/menu changes, no new GameTest run; most recent 35/35 full regression result remains Issue 005. Cursor interaction, crafting recipe workflow, multiplayer and compatibility still unverified. Owner accepted the inspection and authorized Issue 007 to correct the progress-label layout. Final backup verified separately.

---

Current result: 23/23 actual Minecraft Forge GameTests passed (14 Infuser tests + 9 Crafter tests). No client visual validation. Development JAR must NOT be installed in Steph's existing world.

## Source and port
- Upstream implementation retained read-only under original src/ tree (MIT licensed).
- Functional server-side CrafterBlockEntity registered under mysticalautomation:crafter, with real server ticker.
- Separate 9-slot nonconsumable recipe blueprint, 9-slot pooled material stock, 1 furnace fuel slot and 1 output slot.
- Uses real Forge 1.20.1 crafting recipes (shaped and shapeless through recipe manager); ingredient matching tracks consumed quantity.
- Returned crafting containers supported when they can safely fit into fully consumed source slots. Unsupported remainder allocations block crafting safely rather than deleting resources.
- Supports base 200-tick crafting at 20 FE/tick plus all six official 1.20.1 Mystical Agriculture upgrade items and their original speed, power-use and capacity multipliers.
- Save/load covers recipe pattern, item stock, upgrade, progress and fuel. FE uses shared persistent storage.
- Sided Forge item handlers support top material insertion, side furnace-fuel insertion and bottom output extraction.
- Crafter item recipe ported to 1.20.1. Uses minecraft:crafting_table for original crafting-table tag because NeoForge c:player_workstations/crafting_tables is not directly compatible with 1.20.1 Forge.
- New server-backed Forge menu and client screen uses original 176x217 Crafter texture.
- Menu has nonconsumable ghost grid, real ingredient stock, upgrade, fuel and output. Ghost clicks copy held item without consuming. Shift-click prefers configured recipe ingredients over furnace fuel.
- Menu capacity and progress values split across 16-bit sync fields; larger FE upgrade capacities remain intact.

## Runtime tests
1. Crafter recipe registered
2. Four oak planks -> crafting table with pooled source input
3. No crafting when ingredients are missing
4. Full output does not consume input
5. Blueprint and stock survive save/load
6. Awakened Supremium upgrade accelerates recipe
7. Honey bottles -> honey block and all four glass bottles are returned
8. Fake ghost slots never consume cursor input
9. Shift-click routes crafting ingredients correctly and upgraded capacity/progress are synced

## Outstanding work
- Actual graphical client testing, multiplayer sync, external mod pipe interoperability and complete recipe coverage.
- Recipes with crafting remainders that cannot safely return to exhausted input slots are currently blocked. General remainder collection, additional output handling and item-transfer policies remain for full parity.
- Ingredient alternatives/tag matching via recipe blueprints need more coverage.
- JEI and UI polish, fuel-container return and additional server persistence tests.
- Infuser's 512-item staging is NBT-safe and server-tested, but client item packet/UI count behavior with stacks above vanilla max needs explicit validation.
- Five other machines (Farmer, Fertilizer, Infusion Altarnator, Awakening Altarnator, Enchanternator) remain nonfunctional shells.
