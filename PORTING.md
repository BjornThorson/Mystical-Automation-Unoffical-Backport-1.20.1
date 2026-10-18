ISSUE 009 ACCEPTED (18 Oct 2026): Real Forge Survival-client end-to-end Crafter test now passes: clean new block entity, 2x2 oak-plank blueprint, shift-click four real planks and one coal, process progress, exactly one crafting table output, four planks consumed, real output collected in player hotbar. QA first caught and corrected reused block-entity contamination; preserved screenshot shows old result preexisting, not a mod duplication defect. Fresh Forge build, static checker, and actual GameTestServer 35/35 passed. No production changes. Screenshots and comparison in qa-evidence/issue-009/. Owner accepted the screenshot review; completed checkpoint backup verified separately. NOT safe for Steph's live world.

ISSUE 008 ACCEPTED (18 Oct 2026): Real Forge client Crafter ghost-blueprint first-cell left click copied an oak-plank ghost from a carried 32-stack without consuming any items; all 32 returned to player hotbar. Ghost remained visible after closing/reopening Crafter and cleared with empty-cursor click during a separate completed client pass. Actual screenshots in qa-evidence/issue-008/; existing server-side blueprint-clear GameTest plus fresh 35/35 Minecraft GameTests, Forge build and static foundation checks passed. No source or recipe changes. Owner accepted the review screenshots; completed checkpoint backup verified separately. Still not for Steph's live world.

ISSUE 007 ACCEPTED (18 Oct 2026): Crafter GUI progress label moved below the 3x3 ghost recipe blueprint and within original 176x217 GUI bounds; long values shrink inside 81-pixel clear strip. Same texture and slots retained. Real Forge client screenshot and before/after comparison confirmed corrected default 0/200 state; build, static foundation and new 35/35 Minecraft GameTests passed. Only CrafterScreen.java and QA documentation/screenshots changed. Owner accepted the corrected screenshot; final checkpoint backup verified separately. See CLIENT-QA.md and qa-evidence/issue-007/. NOT safe for Steph's live world.

ISSUE 006 ACCEPTED (18 Oct 2026): Real Forge graphical QA successfully opened and photographed the Crafter GUI; original 176x217 upstream texture is byte-identical. Ghost 3x3 blueprint, nine real ingredient slots, upgrade, fuel, output, labels and player inventory visually present. DEFECT: progress text overlaps blueprint/process region and clips at GUI right edge; fix as separate issue. Full and cropped real-client screenshots in qa-evidence/issue-006/ and on review-only GitHub branch review/issue-006-crafter-gui. No gameplay code changes; no new GameTests (latest established baseline 35/35 from Issue 005). Owner accepted the visual inspection and requested the layout fix as Issue 007. Final backup verified separately. NOT for Steph's live world.

ISSUE 005 ACCEPTED (18 Oct 2026): Real Forge Survival-mode Infuser GUI safely shift-clicked 8x64 Inferium into its 512-item backing slot, rejected the ninth 64-stack, and allowed one normal 64-item withdrawal leaving synchronized overlay count 448. Separate real-client 64-item insertion/withdrawal/return tested. Full item conservation observed; screenshots in qa-evidence/issue-005/. Build, static checks and real Minecraft GameTests 35/35 passed; no source-code changes. Accepted after screenshot review and full audit; checkpoint backed up and verified separately. Remaining gestures/multiplayer still unverified. NOT safe for Steph's live world.

ISSUE 004 ACCEPTED (17 Oct 2026): Real Forge client Infuser right arrow selected Insanium (tier 5) from Supremium (tier 4), then left arrow restored Supremium. Screen updates are server-synchronized by menu data; a separate /data command query did not yield usable logging evidence. Extended one existing GameTest to assert selected tier and menu data stay consistent, including invalid-selection rejection. Build and fresh real GameTestServer regression: 35/35 passed. No production mod changes; evidence in qa-evidence/issue-004/ and CLIENT-QA.md. Accepted and preserved with QA evidence; NOT safe for Steph's live world.

ISSUE 003 ACCEPTED (17 Oct 2026): Disposable Forge client QA simulationDistance corrected from invalid 2 to supported 5. Reproducible opt-in helper: forge-port/scripts/configure_client_qa_options.py; real integrated server logged simulation distance 5, with no options parse error. No gameplay or launcher behaviour changed. Verified 35/35 GameTests from Issue 002 remain the latest regression baseline; no new GameTests run for this environment-only fix. See CLIENT-QA.md and qa-evidence/issue-003/. NOT for Steph's live world.

ISSUE 002 ACCEPTED (17 Oct 2026): Infuser GUI Target/Progress label overlap corrected in client-only screen. Real Forge client screenshot confirms target/fuel and progress/output separation for default Supremium state. Build, static foundation, and a new 35/35 real GameTest run passed. Evidence: qa-evidence/issue-002/. Accepted after technical audit; verified GitHub and Drive backup recorded separately. No machine or recipe behaviour changes. See CLIENT-QA.md. Still NOT for Steph's live world.

ISSUE 001 ACCEPTED (17 Oct 2026): Real Forge graphical client successfully opened and rendered the Infuser screen in disposable QA world; screenshot preserved at qa-evidence/issue-001/infuser-gui-2026-10-17.jpg. Client mouse/item interaction and 512-item overlays remain unverified. Separate findings: overlapping Target/Progress text and invalid QA simulationDistance=2. No gameplay code changes or new GameTest run; 35/35 remains the last proven regression baseline. See CLIENT-QA.md. NOT safe for Steph's live world.

CURRENT: 35/35 real Minecraft 1.20.1 Forge GameTests passed. Two productive machines (Infuser, Crafter), third Farmer server processing implementation now tested. All 17 Farmer recipes and shaped machine recipe load; original 16-slot Farmer inventory retained. Dedicated Farmer GUI, visual client opening of machine GUIs and full multiplayer tests remain outstanding. Four other machines remain scaffolds. Development-only; NOT for Steph's live world. See FARMER-PORT.md and CLIENT-QA.md.

LATEST: 33/33 Minecraft Forge GameTests passed, including five Farmer server processing checks. Farmer recipe type and all 17 recipe JSON files ported. Farmer GUI and client visual interaction still pending. Infuser and Crafter continue passing their regression suite. Four other machines remain shells. See FARMER-PORT.md and CLIENT-QA.md. Do not install this build in Steph's real world.

LATEST: 28/28 real Forge 1.20.1 GameTests passed. Infuser 512-item buffer now uses a network-safe client preview, separate synced counts and special conservative cursor/shift-click handling. A real Minecraft graphical client started under virtual display, but no live machine GUI mouse/render or full multiplayer tests yet. See CLIENT-QA.md. Five remaining machines still pending. DO NOT install this development JAR in Steph's live world.

LATEST CHECKPOINT: 23/23 actual Minecraft GameTests passed. Infuser has 512-item staging with extended NBT, and Crafter has functional server-side crafting and ghost recipe menu. Five other machines still need implementation; see INFUSER-PORT.md and CRAFTER-PORT.md. Development JAR is not safe to install in Steph's live world.

CURRENT: 13/13 real Forge GameTests passed. Infuser has original-item machine upgrade compatibility, server-backed UI, scaled power/speed/capacity and persistent upgrade slot. Next: 512-item input staging and/or Crafter port. Six other machine processors still outstanding. See INFUSER-PORT.md. Not for Steph's live world.

CURRENT: 8/8 real Forge GameTests passed. Infuser has server-backed menu; upgrade parity and other six machines are pending. See INFUSER-PORT.md.

CURRENT: Infuser conversion engine passed seven in-game Forge GameTests. See INFUSER-PORT.md. Full backport incomplete.

# Mystical Automation: full Forge 1.20.1 backport

Upstream: BlakeBr0/MysticalAutomation branch 1.21 commit 4149601b9c443eb7de5ebab6ecc9b6bdd7d6462d, licensed MIT.
Target: Minecraft 1.20.1, Forge 47.4.24, Java 17, Gradle 8.8, all seven machines.
Source reference: upstream src/ tree remains unchanged. Forge project: forge-port/.
Dependency baseline from BlakeBr0 Maven: Cucumber 1.20.1-7.0.16, Mystical Agriculture 1.20.1-7.0.24, Mystical Agradditions 1.20.1-7.0.12.
Preserve licence and mark this as unofficial. Do NOT install early development JAR in Steph's world.

## Full-scope machine checklist
- [ ] Infuser: essence conversions, crystal durability/reuse, side IO, energy and machine upgrades
- [ ] Crafter: crafting grids, remaining items, side IO and upgrades
- [ ] Farmer: growth, seeds, farmland, crux, outputs and upgrades
- [ ] Fertilizer: area and fertilizing items, upgrades
- [ ] Infusion Altarnator: altar crafting and recipe matches
- [ ] Awakening Altarnator: awakening ingredients and altar crafting
- [ ] Enchanternator: enchanting, XP and resource consumption

## Integration and quality
- [ ] Forge registration, block entities, containers, screens and network packets
- [ ] Translate NeoForge item and FE capabilities to Forge 1.20.1
- [ ] Port recipe registration and all JSON data/models/localizations
- [ ] Port JEI, Patchouli, CraftTweaker as compatible
- [ ] Check configs, machine upgrades, dedicated server, multiplayer, save/reload, automation IO
- [ ] World-safe testing, no item duplication/loss, maintain gameplay balance

## Milestones
1. Clean reproducible Forge build with correct mod identity and dependencies.
2. Registries and items COMPILE; seven temporary visual shells registered. Block entities and recipes not yet implemented.
3. Implement all seven machines with UI and recipes.
4. Test feature parity on an isolated 1.20.1 instance before installation.

## Checkpoint after resumed sandbox
- Forge and pinned mod dependencies compile on JDK 17.
- Registered all seven upstream machine block IDs and matching block items for Forge 1.20.1.
- Added horizontal facing + running blockstates; copied original client assets (models, textures, localisation).
- Added Forge 1.20.1 loot table paths and build dependency metadata.
- CAUTION: Block registrations are machine shells only. Energy, inventories, logic, configs, recipes, menus and networks remain unported.

## Checkpoint: shared machine data layer
- [x] Seven Forge 1.20.1 block entity registry IDs, one per original machine.
- [x] Per-machine item storage sizes matching upstream: Infuser 9, Crafter 11, Farmer 13, Fertilizer 9, Infusion Altarnator 11, Awakening Altarnator 11, Enchanternator 5.
- [x] Common item-handler and receive-only FE storage capabilities, with 80,000 FE base capacity.
- [x] Inventory and energy NBT serialization and restoration hooks.
- [x] Inventory drops on block replacement/removal rather than silently deleting stored items.
- [x] Capability lifecycle cleanup hooks and resource/registration static integrity script: forge-port/scripts/check_foundation.py.
- [ ] Not yet runtime tested: saving a world, inserting/extracting with pipes, chunk unloading, block breaking.
- [ ] Not yet implemented: per-slot restrictions, machine upgrades, energy consumption by processing, recipes, menus, networking, crafting algorithms, full behaviour.
- [ ] Not for Steph's existing world. All seven remain development shells.

## Checkpoint: first Infuser processing engine (2026-10-16)
- [x] Forge Infuser-specific block entity, active server-side ticker and temporary in-game interaction controls.
- [x] Four-to-one processing of Inferium, Prudentium, Tertium, Imperium, Supremium and Insanium tiers using registered 1.20.1 items.
- [x] Master and standard Infusion Crystal detection; consumes the crystal's declared crafting remainder.
- [x] 100 processing ticks at 20 FE/tick; accepts external Forge Energy; burns furnace fuel into an 80k FE internal buffer.
- [x] Sided automation: crystal and essence insertion on top, fuel on sides, output extraction below.
- [x] Temporary controls: right-click with input item to insert, empty-hand right-click to take output; sneak + empty-hand right-click cycles target tier.
- [x] Matching 1.20.1 shaped crafting recipe for Infuser (official upstream pattern and materials).
- [x] Development JAR compilation and packaging succeeded.
- [ ] Original Infuser GUI, machine upgrade slot/speed/energy scaling, 512-item stage input support and original fine-grained slot behaviour.
- [ ] User-visible runtime validation of conversions and item preservation (two GameTests compiled but did not execute).
- [!] GameTest run launched but Cucumber 7.0.16 RecipeManagerMixin raised InvalidInjectionException during server start, preventing tests from running. Inspect /tmp/mystical-gametest-run.log.
- [!] The Gradle game test task may exit 0 despite a server-side mixin failure. A successful Gradle exit is NOT a passed Minecraft test.
- [!] This experimental build must NOT be installed in Steph's world.


## Current Infuser checkpoint (supersedes historical failed test setup notes)
- Seven required tests passed within a real Minecraft Forge 1.20.1 GameTest server.
- The Cucumber mixin remapping development startup issue was resolved with ForgeGradle refmap configuration.
- Infuser has a working server-side conversion engine, crystal handling, FE and fuel consumption, sides and basic controls. Its GUI and upgrade parity are incomplete.
- Insanium requires Mystical Agradditions, now listed as a mandatory runtime dependency.
- Seventh test PASSED for crafting-recipe registration. All other machines are still shells.
- Do NOT install current development JAR in Steph's world.

## GUI implementation checkpoint
- Added Forge 1.20.1 InfuserMenu, ModMenuTypes, client-only InfuserScreen registration and original texture.
- The new screen displays the nine machine slots and player's 36 slots, synced progress, 80k-FE readouts, and live selected tier.
- Two selector buttons are server-authoritative via clickMenuButton; basic shift-click and slot restrictions included.
- The GUI exposes actual machine inventory; transitional manual insertion for held inputs is still supported.
- Eight in-game GameTests pass (seven existing processing/safety checks plus menu handling).
- IMPORTANT: Headless tests do not validate graphical rendering or actual mouse use. The machine GUI still needs human client validation, and the original machine-upgrade slot/behaviour is pending.
- Keep the original upstream MIT attribution; no GitHub writes; continue in the same Vercel sandbox.

## Upgrade compatibility checkpoint (13/13 GameTests)
- Full 1.20.1 machine-upgrade interface reused from Mystical Agriculture's published Forge library. Earlier concern about the old API being absent was incorrect; it exists in different package names.
- Exactly six original tier multipliers matched in source and runtime tests; upgrade inventory and right-click compatibility via IUpgradeableMachine.
- Upgrade slot at upstream GUI position; server menu now 46 slots. Stored FE capacity can grow to 560,000 and is split across 16-bit menu data fields.
- Energy/timing scaling, NBT persistence, item replacement and removal-drop implementation compiled and tested where covered.
- 13/13 headless in-game GameTests pass (all prior 8 and five new upgrade tests).
- Remaining parity gap: 512-item input storage, GUI visual checks and six other machine processors.
