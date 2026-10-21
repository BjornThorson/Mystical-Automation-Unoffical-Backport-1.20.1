# Minecraft CI: original and existing backport

The repository contains **two real projects side by side**:
- Root `./`: original **Minecraft 1.21.1 NeoForge** source, **Java 21**.
- `forge-port/`: working **Minecraft 1.20.1 Forge 47.4.24** backport, **Java 17**.

These are already present on `main`; there is no need to create a fictional `backport/1.20.1` branch. The Forge port is implemented and GameTested for **Infuser (19 tests), Crafter (9 tests), Farmer (8 tests)**. The last owner-approved checkpoint on 18 October 2026 reported **36/36 real Forge GameTests passing**. Four remaining machines (Fertilizer, Infusion Altarnator, Awakening Altarnator, Enchanternator) are scaffolds and still require full ports.

## Automatic checks

`.github/workflows/nex-minecraft-mod-ci.yml` runs:
1. The Forge 1.20.1 build on Linux and Windows with Java 17.
2. The existing `forge-port/scripts/check_foundation.py` structural checks.
3. The **actual Forge GameTestServer** on Linux, requiring at least 36 successful tests. Test logs and development JARs become GitHub Actions artifacts.
4. A separate Java 21 build of the original NeoForge source at the repository root.

These results establish the two projects compile and the port passes its own GameTests. **They do not alone establish behavioural parity**, because the upstream NeoForge project has not emitted equivalent machine observations yet.

The parity catalogue at `qa/backport/cases.json` is generated from all 36 actual port-side GameTest methods. `qa/backport/check_coverage.py` confirms each remains present in the Forge code. Once equivalent *real reference* and *real port* observations are exported, `qa/backport/compare.py --strict` can compare them. A successful comparison must use equivalent inputs, expected outputs, tick budgets, inventory/energy changes and persistence state. It must not compare fabricated or hand-written pass records.

## Launcher testing

`qa/backport/launcher-matrix.json` provides planned reference and target test profiles across Prism Launcher, CurseForge and ATLauncher. Launchers do not replace Minecraft's mod loaders. The GameTestServer is not a graphical Minecraft client test.

The experimental `review/ci-minecraft-qa` branch already includes more ambitious disposable-client and screenshot jobs. Keep those experimental jobs separate until reviewed; screenshots alone do not prove machine functional parity.

**No unfinished mod should be installed in Steph's existing Minecraft world.** Use disposable profiles/worlds, and keep player credentials out of GitHub Actions.

The upstream `neoforge.mods.toml` metadata range `[1.21,1.21.1)` still deserves separate review, as it excludes the 1.21.1 source version. That does **not** mean the 1.20.1 Forge port is missing.
