## Issue 009: Crafter full crafting workflow through real GUI (18 October 2026) - ACCEPTED

Objective: Verify that a player can configure a real 2x2 oak-plank ghost recipe, supply **four real oak planks** and **one coal fuel item**, let the Crafter process them, and collect **exactly one crafting table** through the actual Forge 1.20.1 graphical client. This is a disposable Survival-mode singleplayer QA session, not a creative-mode crafting shortcut.

**Clean-isolation test procedure:** With the client in `Nex Disposable QA`, clear the *player* inventory, explicitly replace the existing test Crafter with `minecraft:air`, then place a fresh `mysticalautomation:crafter` block at the same disposable test position. This two-step replacement was necessary because repeated `/setblock` with the same block type may preserve the original block entity and its output. Starting from an empty machine was verified by the new initial GUI screenshot. Give precisely four oak planks and one coal to separate player hotbar slots; pick up the planks, click four ghost blueprint cells in a 2x2 shape, return the four planks to the hotbar, shift-click planks to real ingredient storage, then shift-click coal into the fuel input. The machine advances its processing progress. On completion, the real stock row becomes empty and the output displays one crafting table. Click the output and place that single table in the player's hotbar.

**Visual outcomes:** Screenshots from the completed isolated run show (1) no starting machine input or output, (2) correct four-cell oak-plank ghost blueprint, (3) four real planks transferred into stock, (4) progress advancing after coal was added, (5) empty stock plus exactly one crafting-table output, and (6) empty output and one real crafting table in the player hotbar after pickup. The ghost recipe remains displayed throughout, as intended. This proves the observed one-cycle conservation **4 planks in -> 1 crafting table out** in the GUI. It is not an independent direct NBT query.

**Important QA false positive caught and corrected:** An earlier run reused the same block entity between launches. Its blueprint screenshot already showed one crafting table in the output *before the new planks were inserted*, and after the next cycle its output showed two. That was an invalid isolation state, not evidence of recipe duplication or an observed mod defect. The initial contaminated screenshot is retained as `qa-reused-machine-detected.jpg`; the final outcome relies **only** on the explicitly destroyed-and-recreated test machine. Future machine end-to-end tests should reset the disposable block entity explicitly.

Regression and audit: Original Forge client GUI and menu were used, with standard `CrafterMenu` ghost slots, ingredient quick-transfer and coal-fuel handling. Existing `CrafterGameTests.craftsVanillaTableFromPooledInputs` already independently checks that four stock planks produce one crafting table after 200 machine ticks, consuming all four. A fresh actual Forge build, static foundation check and Minecraft GameTestServer execution passed, **35/35 required GameTests**. Seven original real-client screenshots plus a five-stage comparison and one contaminated-run transparency capture are archived under `qa-evidence/issue-009/`. Both client runs restored the original `tutorialStep:movement` in the ignored development profile.

No production source, upstream files, recipes, textures, menus, machine logic, tests or unrelated project files changed. Multiple crafts, nondefault patterns, complex ingredient remainders, insufficient fuel handling in a real client, multiplayer, JEI, inventories after full world reload, pipelines and automation interoperability remain separate verification tasks. Incomplete development JAR is **not safe for Steph's existing world**.

Status: **accepted by project owner** after reviewing the five-stage real Minecraft screenshot comparison; GitHub main and Google Drive checkpoint backup verified separately.

---

## Issue 008: Crafter ghost-blueprint mouse interactions (18 October 2026) - ACCEPTED

Goal: Validate the Crafter's *real graphical client* blueprint interaction, where clicking a blueprint cell with a carried item should copy a **single nonconsumable ghost ingredient**, then clicking that cell with an empty cursor should clear it. Keep all gameplay logic, slot positions and upstream assets unchanged.

Actual Forge 1.20.1 Survival-mode QA in the disposable `Nex Disposable QA` world (Xvfb/Mesa):
- Placed the Crafter, opened its real 176x217 GUI, and prepared **32 oak planks** in player hotbar slot 0.
- Picked up the real stack with a normal left click; the hotbar slot became empty while the cursor held the stack.
- Left-clicked the *first* upper-left blue ghost blueprint cell with the 32-plank cursor. A single oak plank icon appeared in the ghost cell, with the actual items remaining available to return to inventory.
- Left-clicked the player's first hotbar slot to return the full **32 oak planks**, leaving the recipe ghost visible. Closed and reopened the same Crafter GUI: the ghost blueprint entry was still displayed and all 32 items remained in the player's hotbar.
- A shorter subsequent disposable-client pass re-established the ghost pattern and then left-clicked it with an *empty cursor*. The ghost entry visibly disappeared, and the original **32 oak planks** remained in the hotbar.

Evidence: `qa-evidence/issue-008/` contains six original unaltered Minecraft screenshots, a combined six-stage comparison image, and `verification.txt`. First run ended when the Vercel session retired after the reopening screenshot, so its clearing step was **not** credited. The separate completed second run proves clearing. Both QA sessions restored `tutorialStep:movement` in the ignored disposable profile.

Mechanism and regression: The existing `CrafterMenu.clicked` processes blueprint slots 12..20 on a PICKUP click by copying `getCarried()` and constraining the copy to one, without decrementing the held stack; empty cursor writes an empty ghost pattern. The existing `CrafterGameTests.ghostMenuDoesNotConsumeTemplateItem` tests these two server-side cases. A new Forge build, static foundation check and actual Minecraft GameTestServer run all passed; **35/35 required GameTests**. No Forge Java class, test, menu, recipe, packet or texture changes were necessary.

Limits: Only the first blueprint cell and left-click PICKUP were exercised in a real GUI. Right-click variants, middle-click, shift-click, drag, nine-cell workflows, shaped/shapeless recipe execution, persistence across server shutdown, multiplayer, creative-mode gestures and network jitter need separate coverage. This development build remains **unsafe for Steph's existing world**.

Status: **accepted by project owner** after review of the actual six-stage Minecraft comparison; checkpoint backup to GitHub main and Google Drive verified separately.

---

## Issue 007: Crafter progress-label alignment (18 October 2026) - ACCEPTED

Scope: Correct the progress-text overlap documented in Issue 006 by changing **only** `forge-port/src/main/java/com/blakebr0/mysticalautomation/client/screen/CrafterScreen.java`. Retain the original upstream 176x217 Crafter GUI texture, all inventory and ghost-blueprint slot locations, the output/fuel/upgrade widgets, machine data, networking, persistence, gameplay and recipes.

Implementation: The old `Progress: ...` label began at GUI x=100,y=75, crossing the last recipe-blueprint row and overrunning the right edge. The corrected label occupies the clear strip under the blueprint and above the ingredient row, positioned within x=88..169, y=87..96. A small screen-only helper centers the string in this **81-pixel-wide bound** and scales longer readings down to fit instead of drawing over inventory slots or extending outside the panel. The value still comes directly from existing `menu.getProgress()` and `menu.getOperationTicks()`.

Verification: A clean disposable Forge 1.20.1 client launched and opened the Crafter by real right-click after the correction, with the introductory tutorial notification temporarily disabled for photographic QA and restored afterward. A new full-resolution screenshot, a cropped detail and a before/after comparison were checked. The observed `Progress: 0/200` readout is fully visible below the grid, does not overlap slots or the arrow, and stays inside the original panel. The Forge `./gradlew build` passed and a **fresh real Minecraft GameTestServer run passed all 35 required tests**; the static foundation checker passed. Evidence: `qa-evidence/issue-007/`.

Audit boundaries: No upstream `src/` changes and no machine, menu, recipe, asset, test fixture, inventory or saved-world changes. Unverified: all possible live high progress values, nondefault GUI scales, modded font settings, multiplayer and Crafter ghost-blueprint mouse interaction. The latter remains the next proposed focused issue. This incomplete development JAR remains **unsafe for Steph's existing world**.

Status: **accepted by project owner** after review of the actual Minecraft screenshot and before/after comparison. Final GitHub main and Drive backup verified separately.

---

## Issue 006: Crafter graphical GUI inspection (18 October 2026) - ACCEPTED

Objective: Open the existing Forge 1.20.1 Crafter menu in a real Minecraft client and visually inspect the original background, nine-slot ghost recipe blueprint, independent nine-slot material storage, output, fuel and machine-upgrade slots, labels, energy reading, and player inventory. This is a **visual-opening issue only**. Blueprint mouse interactions and recipe execution in the client are separate issues.

Verified in disposable `Nex Disposable QA` Survival-mode world with Xvfb/Mesa software rendering: the client joined the integrated server, commands placed a real `mysticalautomation:crafter` block and oriented the player, and a simulated right-click opened the actual Crafter screen. The screen displayed the original 176x217 GUI panel, three-by-three blue ghost blueprint area, nine distinct material slots, fuel slot, output slot, upper-right upgrade slot, player inventory, and energy/progress text. The original and ported background PNG files are **byte-identical** (SHA-256 `a8c228040713acf3dc1b3131463ecadd50794d8e1903a258f341c1040ca67594`).

**Defect discovered for a separate issue:** The long `Progress: 0/200` readout begins inside the ghost blueprint's lower-right area and extends across the processing region and beyond the right edge of the GUI. Visual evidence and `CrafterScreen.java` label placement (x=100, y=75 in the 176-pixel-wide panel) support this. The right-side tutorial toast in the first screenshot obscured the upper-right upgrade slot. A second run temporarily used `tutorialStep:none` in the *ignored disposable QA client's* `options.txt` to obtain a clean screenshot; the original `tutorialStep:movement` setting was restored after the run. No permanent QA setting change.

**Reviewable screenshots:** Real-client full-screen capture `qa-evidence/issue-006/crafter-gui-clean.jpg`, a closer crop `crafter-gui-detail.jpg`, and the unaltered first capture `crafter-gui-tutorial-overlay.jpg`. A separate screenshot-only GitHub branch `review/issue-006-crafter-gui` exists for owner review; it must not be mistaken for the approved `main` backup.

Audit and limits: No production mod code, tests, texture, recipes, or block/menu implementation changed. Static foundation verification passed. No fresh build or GameTestServer run was needed for this inspection-only milestone; the **35/35 GameTest result from accepted Issue 005 remains the most recent full regression checkpoint**, not a new test result for Issue 006. Forge client logs reported nonfatal missing audio/narrator libraries in the headless Linux environment, but no identified Crafter screen exception. The visual appearance of empty slots does not prove blueprint cursor safety, real ingredient transfers, JEI behaviour, crafting, multiplayer, or save/reload. The incomplete development JAR remains **unsafe for Steph's existing world**.

Status: **accepted by project owner** as a visual inspection; the layout defect is reserved for separate Issue 007. Backup verified separately.

---

## Issue 005: Real-client Infuser 512-item buffer interactions (18 October 2026) - ACCEPTED

Scope: Graphical and server-backed inventory QA for the existing safe-preview / `ContainerData` architecture, using a *disposable* Minecraft Forge 1.20.1 Survival-mode world. No machine implementation, menu logic, network code, recipes, test source or resource assets were changed.

**64-item baseline (17 October, UTC):** A command placed 64 Inferium Essence in an empty player's hotbar. An actual shift-click moved the entire batch into the Infuser's first input slot. A left-click withdrew the batch to the cursor, and a normal click placed it back into the hotbar. Screenshots before, after insertion, during cursor withdrawal and after returning the stack show the conservation of 64 items.

**512-item boundary and overflow (18 October, UTC):** Starting with an empty player inventory and new Infuser, `/give` supplied 512 Inferium Essence in eight normal 64-item player stacks. Eight real GUI shift-clicks filled the first staging input. The menu's separately synchronized count overlay visibly read **512**. An additional `/give` supplied a ninth 64-item stack, which remained in the player's hotbar after shift-clicking against the full input; the input remained **512**. Clicking the staging slot withdrew a normal 64-item batch, and the displayed backing count changed to **448**. Returning that batch to a second player hotbar slot left two ordinary 64-item stacks with the player, plus 448 in the machine: all **576** accounted for.

Verification method: The client GUI's displayed count comes from `InfuserMenu.getEssenceCount()`, bound to server-provided `ContainerData`, while the slot sends a safe one-item preview. Existing Minecraft GameTests separately cover full 512-item backing storage, capacity rejection, the 512-to-448 extraction, NBT persistence and packet-safe previews. A new Forge build, static foundation check and complete GameTestServer run passed: **35/35 required tests**. This confirms these observed mouse interactions but is not an independent block-NBT query; no claim is made about all possible inventory gestures, modded pipes or multiplayer.

Evidence: `qa-evidence/issue-005/` contains nine exact client screenshots, focused comparison images, and `verification.txt` with hash list and the relevant test logs. Detailed client/test logs remain ignored under `forge-port/run/`.

Unverified/future work: direct cursor deposit into an already partly filled buffer, right-click single-item transfers, drag/double-click/hotbar-number restrictions in the live client, rapid repeated clicks, reconnect/reload, other essence tiers, multiplayer and external automation interoperability. No defects were observed within this issue's tested scope; full backport remains incomplete and **must not be installed in Steph's existing world**.

**Review evidence policy:** For future graphical QA issues, include a viewable real-client screenshot (preferably a clickable direct image link) with the review **before** requesting approval. Use a comparison image for visual before/after or multi-state results when helpful. If in-chat images are unavailable, publish a review-only screenshot without updating the main backup branch or treating the issue as approved. The Issue 005 review preview was published on `review/issue-005-screenshot` as a separate branch; the final images are archived in `qa-evidence/issue-005/`.

Status: **accepted by project owner** after inspection of an actual Minecraft screenshot; checkpoint is backed up and verified separately.

---

## Issue 004: Infuser GUI target-selection arrows (17 October 2026) - ACCEPTED

Objective: Verify the existing Forge Infuser GUI arrow buttons with real mouse input and confirm server-authoritative tier synchronization. No processing or interface behaviour replacement was necessary.

Evidence from real Minecraft 1.20.1 Forge disposable client (Xvfb/Mesa, world `Nex Disposable QA`):
- Opened the actual Infuser GUI with an empty-hand simulated right click. Initially showed `Supremium` (tier 4).
- Clicked the GUI right arrow; the displayed tier updated to `Insanium` (tier 5). Reopened the same machine and clicked the left arrow; the display returned to `Supremium` (tier 4).
- The screen's tier label reads `InfuserMenu.getTarget()`, whose `ContainerData` is supplied from server-side `InfuserBlockEntity.getSelectedIndex()`. Client button clicks call `handleInventoryButtonClick`, and the server's `InfuserMenu.clickMenuButton` updates the block entity. The screen does not directly mutate a local selection value. Thus these observed changes provide strong evidence of client-to-server-to-client synchronization.
- Three 1280x720 screenshots and a comparison crop are preserved in `qa-evidence/issue-004/`. This verifies the two exercised transitions and visually responsive buttons, not full inventory interaction.
- A separate `/data get block ... SelectedIndex` command was attempted during QA but did not produce a corresponding result in the retained Minecraft log. It is **not** counted as independent NBT confirmation. No unexplained extra functionality is claimed.

Regression coverage: Extended the pre-existing `InfuserGameTests.menuSelectionSyncData` GameTest to assert initial target 4, selection 5 then 4, another valid selection 3, rejection of invalid button 99, and consistency between server block entity and menu data. No new test fixture or new GameTest was added, so the total remains 35. Final Gradle build passed, and the **actual Minecraft GameTestServer logged all 35 required tests passed**.

Audit boundaries: Only that GameTest Java class plus Issue 004 documentation and QA evidence changed. No machine behavior, recipes, client screen, texture, networking, inventories, saves, world data or unrelated projects were modified. Testing all tier boundaries, rapid clicks, disconnection/reconnection, 512-item transfers and multiplayer remains separate work. The mod is still unsuitable for Steph's live world.

Status: accepted by project owner after technical and regression validation; included in Issue 004 backup checkpoint.

---

## Issue 003: Disposable client QA simulation distance (17 October 2026) - ACCEPTED

Scope: Correct the *development-only* Minecraft 1.20.1 Forge client configuration. No mod Java, recipe, block, menu, network, saved-world or gameplay logic changed.

Cause and fix:
- The existing ignored `forge-port/run/options.txt` contained `simulationDistance:2`. Minecraft 1.20.1 rejected this value (valid range 5..33), so the integrated server had previously used distance 12.
- The disposable QA profile now uses `simulationDistance:5`, the lowest supported setting. Existing `renderDistance:2` and `maxFps:30` remain unchanged.
- Added explicit, tracked helper `forge-port/scripts/configure_client_qa_options.py` to reproduce the correction without tracking or distributing the local Minecraft `run/options.txt`. It only changes that file's single simulationDistance line; `--check` validates without changing it.
- After a fresh first launch creates `run/options.txt`, run from the project root: `python3 forge-port/scripts/configure_client_qa_options.py`. Before subsequent QA launches, use `python3 forge-port/scripts/configure_client_qa_options.py --check`.

Verification:
- Pure Python checks exercised old invalid values, idempotence, CRLF preservation, missing and duplicated settings.
- A real Forge software-rendered client was launched with `-PqaQuickPlayWorld="Nex Disposable QA"`, joined the disposable world, and logged `Changing simulation distance to 5, from 0`. No simulation-distance options parsing error was present in this run.
- Local proof: `qa-evidence/issue-003/verification.txt`. The detailed ignored client log is `forge-port/run/qa-issue003-client.log` with runtime Minecraft events in `forge-port/run/logs/latest.log`.

Audit boundaries: The script is opt-in; normal `runClient` invocation and other Minecraft instances are untouched. The unrelated Infuser UI and the seven-machine parity roadmap remain unchanged. No new Java GameTest run was required for this isolated QA configuration correction; the verified 35/35 Minecraft GameTest checkpoint from Issue 002 remains the latest full regression result.
Status: accepted by project owner after real-client verification and audit. Development JAR remains unsafe for Steph's existing world.

---

## Issue 002: Infuser GUI label alignment (17 October 2026) - ACCEPTED

Scope: Adjust text placement only in Forge `InfuserScreen.java`, retaining the original 217x194 upstream texture, menus, inventory positions, output slot, machine logic and controls.
- The progress value now appears above the six essence slots, center-aligned within a bounded 84-pixel area; long text scales down to fit.
- The caption `Target:` appears right of the furnace-fuel slot, with the essence tier on its own line in the open lower-left panel. The tier name is centered inside a 70-pixel area and shrinks if necessary.
- The first visual trial identified a residual fuel-slot overlap. The final position was corrected and checked again in a real Minecraft client.

Verification: successful Forge build; fresh actual Minecraft GameTestServer run `All 35 required tests passed :)`; static foundation check passed. Disposable singleplayer Forge client loaded, block spawned and right-clicked, and the final Infuser GUI screenshot was inspected. No overlaps of these labels with slots or buttons were visible in the tested default Supremium state.
Evidence: `qa-evidence/issue-002/infuser-gui-after-2026-10-17.jpg` and `qa-evidence/issue-002/verification.txt`.
Audit: Only the screen Java class and this issue's documentation/evidence are in scope; no machine, recipe, storage, menu packet or save-data changes. Other tier labels, dynamic high-value readouts, input interactions and multiplayer remain for future testing. The unrelated `simulationDistance:2` problem remains unresolved.
Status: accepted by project owner after technical verification and audit. Capture and test records are preserved with the checkpoint. Development JAR remains unsafe for Steph's existing world.

---

## Issue 001 accepted: first real-client Infuser GUI opening (17 October 2026)

Status: **ACCEPTED for the narrow GUI-opening/visual-capture milestone**. This does not assert the whole GUI is functional or visually correct.

Evidence:
- Minecraft 1.20.1 Forge development client was launched using the disposable `Nex Disposable QA` world in Xvfb, with Mesa software rendering. The integrated server logged the player joining.
- The previous failed interaction had produced ordinary chat text `p @s ~ ~ ~ 0 20`, consistent with chat-focus timing consuming the command prefix. With explicit chat open (`xdotool key t`), a 2-second focus delay, slow character input (`xdotool type --delay 95`), and Return, the `/say` diagnostic and subsequent world commands executed successfully.
- To target the machine reliably from the player's *actual* position, the QA run entered `/execute at @s run setblock ~ ~1 ~3 mysticalautomation:infuser` and `/tp @s ~ ~ ~ 0 0`. A pre-click screenshot confirmed the block in the crosshair. A simulated right-click opened `InfuserScreen` and a post-click screenshot clearly showed the real in-game GUI.
- Saved full-resolution screenshot (1280x720): `qa-evidence/issue-001/infuser-gui-2026-10-17.jpg`. SHA-256: `0abe153cb1c5d6c11cca9acf88ab740d5b0558cd1c1791912ed39529259873be`. Raw disposable-run logs and other screenshots remain under the ignored `forge-port/run/` directory.
- The successful run is recorded in `forge-port/run/qa-issue001d-run.log` (local QA only); it finished after capturing the GUI. This test establishes actual opening and rendered appearance, not inventory interaction.

Audit findings / separate future issues:
- The screenshot visibly shows the target label extending into the right-arrow button and the progress text crossing the central process area. Audit `InfuserScreen.java` label positions and GUI texture dimensions in a **separate layout issue**.
- `forge-port/run/options.txt` contains `simulationDistance:2`, rejected by the Minecraft 1.20.1 client as below the permitted 5..33 range. Correct the disposable QA profile in a **separate environment issue**, without touching Steph's settings.
- All cursor/shift-click controls, actual 512-item quantity overlays, network synchronization, multiplayer, client reconnect, save/reload, external automation and Crafter visual tests remain **unverified**.
- This issue made **no gameplay/source-code changes**. The previous 35/35 GameTest result is preserved historical evidence; GameTests were **not rerun** for this documentation-and-screenshot checkpoint.
- The mod is still development-only. **Do not install it in Steph's existing world.**

---

## Disposable graphical world test, incomplete GUI milestone
- Created a real disposable singleplayer Creative world called Nex Disposable QA within forge-port/run/saves.
- Client render thread loaded the Forge mod and rendered the Minecraft title and world creation UI.
- The integrated Minecraft server completed its Forge handshake and logged Dev joining the world.
- The bounded Xvfb client run expired immediately after joining, before an Infuser or Crafter screen could be positively opened or visually inspected.
- To avoid title/world-creation delays, build.gradle now supports an optional development-only property: -PqaQuickPlayWorld="Nex Disposable QA". When absent normal runClient is unchanged.
- Graphical machine UI/real mouse interactions, multiplayer, and any modpack-specific features remain unverified.

## Interactive client checkpoint
- Loaded the actual Forge 1.20.1 Minecraft graphical client in a virtual Xvfb display and inspected the main menu screenshot.
- Mouse automation successfully selected Singleplayer and reached Minecraft's disposable-world creation screen.
- The Vercel session/time limit interrupted the attempt before entering the world, opening machines or testing graphical mouse interaction.
- Do not describe the GUI as visually tested until those actions are verified. Real visual inspection remains pending.

# Client and 512-item inventory QA, Forge 1.20.1

## Verified automated results
- All 28 required Minecraft Forge GameTests passed in the latest run.
- Six Infuser essence slots can store 512 items each. Persistence uses separate integer LargeStackCount in addition to a safe vanilla NBT Count field.
- In Minecraft 1.20.1, vanilla FriendlyByteBuf.writeItem writes item count as a signed byte. Sending a raw count of 512 through normal slot syncing is unsafe.
- The Forge Infuser menu now transmits one-item preview stacks for each extended slot and syncs exact 0..512 quantities separately in ContainerData. The client screen overlays the actual count.
- Actual menu click logic for extended slots is server-authoritative. Left click takes up to 64, right click takes/returns one, and shift-click moves up to 64 at a time.
- Drag merge, pickup-all, hotbar swap, clone and drop-through interactions are blocked for extended slots. All other non-extended slots keep normal Forge menu logic.
- Runtime GameTests verify packet round-trip for an item and a whole ClientboundContainerSetContentPacket with a 512-item backing stack; cursor transactions; bidirectional shift-click; safe handling of unsupported clicks.
- Forge top-side item capability accepted eight 64-item batches and rejected the ninth at 512; bottom-side output extraction succeeded; forbidden bottom input extraction was denied.
- Crafter menu and ghost recipe interactions remain part of the passing regression suite.

## Graphical client smoke test
- A real Minecraft Forge 1.20.1 graphical development client started under Xvfb using Mesa software rendering and Java 17.
- Minecraft render thread, Mystical Automation mod resource loading, and texture atlas creation were seen in the client log /tmp/mystical-client-smoke.log.
- Original 256x256 PNG textures for Infuser and Crafter GUI are present and accommodate the port's 217x194 and 176x217 UI backgrounds.
- Optional Linux narrator library (flite) was absent. Minecraft continued rendering.
- Client was stopped after a bounded smoke-test window. No real world was joined or machine screen opened.
- **Do not claim the GUI has passed actual mouse/render or multiplayer QA yet.**

## Remaining client and compatibility checks
1. In a disposable Forge 1.20.1 installation, open both machine screens; check textures, labels, 0..512 quantity overlays, clickable UI, focus and tooltips at normal and high GUI scales.
2. Infuser: insert/extract, left/right/shift click, reject drag and hotbar replacement on buffers; switch target; move upgrades; confirm power/progress updating across client/server.
3. Crafter: set/clear ghost recipe slots while cursor stack stays unchanged; fill ingredient stock, shift-click planks as material (not fuel), take output and verify crafting remainders.
4. Validate a real multiplayer remote client. Verify chunk save, world reopen, network resync after disconnect/reconnect and machine break drops.
5. Test real installed-pipe mods and item transports; some other mods may assume max vanilla stacks and need explicit compatibility.
6. Run all GameTests again after client-facing changes.
7. Do not install this unfinished 0.0.1-dev JAR in Steph's existing world. Five machine processors are still missing.

## Re-run commands
From forge-port/:
- ./gradlew build --no-daemon --console=plain --max-workers=2
- ./gradlew runGameTestServer --no-daemon --console=plain --max-workers=2
- Optional Linux desktop smoke test: timeout 115s xvfb-run -a -s '-screen 0 1280x720x24 +extension GLX' env LIBGL_ALWAYS_SOFTWARE=1 ./gradlew runClient --no-daemon --console=plain --max-workers=2

No GitHub writes were made. Keep the upstream MIT provenance and original source intact.

## Quick Play attempt and graphics cost
- Opt-in Quick Play loaded the existing Nex Disposable QA level. The integrated server reached 99% spawn preparation, but timed out before logging player join.
- World creation itself was successful in the preceding session, which logged Dev joined the game.
- Virtual client defaults were renderDistance=12, simulationDistance=12 and maxFps=120. Reduced these only in forge-port/run/options.txt to 2, 2 and 30 for isolated software-rendered QA.
- Actual machine GUI visuals and mouse behaviour remain unverified.
