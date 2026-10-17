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
