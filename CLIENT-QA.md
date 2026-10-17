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
