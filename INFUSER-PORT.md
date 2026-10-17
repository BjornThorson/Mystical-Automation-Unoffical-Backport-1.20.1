## Network-safe 512-item UI checkpoint (28/28 GameTests)
- Vanilla 1.20.1 item packets use a signed-byte count; raw 512-item stacks cannot be transmitted as normal menu slots.
- Machine still holds and persists full 512 essence; InfuserMenu shows safe one-item slot previews, with exact count synced in 14-field ContainerData and overlaid graphically.
- Custom cursor/shift-click mechanics never expose more than 64 essence at once. Explicitly block unsafe large-slot drag/double-click/hotbar swap/drop interactions.
- In-game test packet round-trip, whole container packet, left/right/shift clicks, unsupported click rejection, and simulated Forge sided item pipes all passed.
- Real GUI appearance/mouse actions and multiplayer player interaction remain untested. This is not release ready.

## 512-item essence staging verified
- The six essence slots now admit 512 items each and reject further insertions.
- Minecraft 1.20.1's byte-sized ItemStack Count NBT is not enough; extended stacks are stored with a safe vanilla Count and LargeStackCount integer field, then restored when loading.
- Automated GameTest confirms eight batches of 64 accepted, ninth batch rejected, NBT round-trip preserves 512 and extraction of 64 leaves 448.
- 14 Infuser Minecraft GameTests currently pass. Client network/UI serialization for counts beyond normal stack sizes still needs explicit testing.

# Current Infuser port status: upgraded core and menu

Latest verified status: 13/13 Forge 1.20.1 Minecraft GameTests passed, including all six machine upgrade tiers. Functional client GUI still requires visual testing. Only the Infuser has active machine processing; six other machines remain scaffolds. Do not install in Steph's existing world.

# Initial Infuser functionality: Forge 1.20.1 (development only)

This is an unofficial MIT-licensed backport of BlakeBr0's original Mystical Automation Infuser. Original 1.21.1 upstream source is unchanged.

## Implemented
- Essences: Inferium, Prudentium, Tertium, Imperium, Supremium and Insanium. Conversion is 4:1.
- Slots: 0 infusion crystal; 1 Inferium; 2 Prudentium; 3 Tertium; 4 Imperium; 5 Supremium; 6 Insanium; 7 furnace fuel; 8 output.
- Server-side ticker: 100 ticks, 20 FE per tick per conversion; external Forge Energy or internally generated furnace-fuel power.
- Standard and Master Infusion Crystals use their 1.20.1 crafting-remainder contract.
- Sided capabilities: top for crystals and essences, horizontal for furnace fuel, bottom for output.
- Original 1.21.1 crafting recipe pattern adapted to Forge 1.20.1 ingredient tags.

## Temporary in-game controls, not original GUI parity
- Right-click with essence, an infusion crystal or fuel to insert.
- Right-click empty-handed to take output or display power and progress.
- Sneak + empty-handed right-click to cycle Prudentium through Insanium. Supremium is the default.

## Pending
- Original GUI and menus, upgrade speed/efficiency/capacity, 512-item input staging, accurate RUNNING visual indicator, client-screen testing.
- Other six machines remain nonfunctional shells.
- Never install this development JAR in Steph's current world.

## Verification
- Seven Minecraft Forge 1.20.1 GameTests passed on 2026-10-16: powered conversion, lack of power, full output blocking, coal-generated power, standard crystal durability and NBT save/load.
- GameTest fixture lives at forge-port/src/test/resources/gameteststructures/infusergametests.empty.snbt.
- Seventh GameTest PASSED: crafting recipe registration.
- Cucumber's mixin reference map requires runtime dev remapping via ForgeGradle createSrgToMcp, configured in forge-port/build.gradle.

## October 2026: first server-backed menu milestone
- Registered mysticalautomation:infuser via Forge 1.20.1 MenuType and IForgeMenuType.
- Added a 217x194 client screen using the original Infuser texture, retaining its 9 machine input/fuel/output slots and 36 player slots.
- Added server-authoritative tier-selection buttons, item slot restrictions, shift-click transfers, and live progress/energy readouts.
- Readout FE values split across signed-16-bit ContainerData indices to preserve the complete 80,000-FE range across vanilla menu synchronisation.
- Empty-hand right-click now opens the server-backed menu. Sneak + empty-hand continues to cycle target tier as a temporary shortcut. Holding a valid input inserts it into its machine slot.
- Existing inventory and recipe gameplay regression tests still pass. Added eighth Forge GameTest covering 45-slot menu registration, 80k-FE display readout, target selection and invalid-button rejection.
- **8/8 real Forge GameTests passed** in the latest run.
- Client-side graphical appearance, text clipping, actual mouse interaction and external modpack compatibility remain untested.
- Original upgrade inventory, tier scaling and full 512-item essence staging remain unimplemented. Never install this dev JAR into Steph's world.

## Machine upgrades: implemented and verified
- Reused Mystical Agriculture's actual Forge 1.20.1 machine upgrade items, interface and item handler, rather than replacing them. Old package names: com.blakebr0.mysticalagriculture.util.MachineUpgradeTier, com.blakebr0.mysticalagriculture.util.IUpgradeableMachine, com.blakebr0.mysticalagriculture.item.MachineUpgradeItem, com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler.
- Original 1.20.1 and 1.21.1 tier multiplier constants match. No new craftable machine-upgrade item IDs were added.
- Added the upstream GUI's tenth machine slot (upgrade at x192,y9), extending the menu to 46 slots including player inventory. Shift-click upgrade routing and placement restrictions added.
- Installed 1.20.1 IUpgradeableMachine support for upstream item right-click installation, including returning/replacing the previous upgrade.
- Scales processing ticks, FE per tick, internal FE capacity and fuel generation rate. Rounds 0.5 ticks for Awakened Supremium up to a one-tick minimum.
- Reconfigures stored FE capacity immediately when upgrade items change; clamps stored FE on downgrade.
- Persists upgrade inventory across NBT save/reload; drops upgrade item upon machine removal.
- UI progress now reflects current operation time.
- Runtime GameTests cover upgraded actual crafting, replacing and saving upgrades, cap reduction, and all six original tier multiplier combinations.

### Measured tier values at default Infuser parameters
| Upgrade | Ticks / conversion | FE/tick | FE capacity |
| --- | ---: | ---: | ---: |
| None | 100 | 20 | 80,000 |
| Inferium | 83 | 40 | 160,000 |
| Prudentium | 62 | 80 | 240,000 |
| Tertium | 40 | 120 | 320,000 |
| Imperium | 12 | 320 | 400,000 |
| Supremium | 2 | 960 | 480,000 |
| Awakened Supremium | 1 | 1,920 | 560,000 |

## Still pending
- Original machine's 512-item essence staging (current inputs use standard stack limits).
- Visual GUI/mouse/client testing, and checking installed pack's actual mod versions.
- More rigorous pipe and sided automation integration; dedicated multiplayer/client QA.
- Full functionality of Crafter, Farmer, Fertilizer, Infusion Altarnator, Awakening Altarnator and Enchanternator.
- No GitHub pushes; local source remains isolated to the named shared Vercel sandbox.
