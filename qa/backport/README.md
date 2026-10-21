# Mystical Automation: original 1.21.1 versus Forge 1.20.1

The **Forge backport is real and already in this repository**, at `forge-port/` on `main`. The untouched original 1.21.1 NeoForge source is the root project. The branch named `backport/1.20.1` in earlier draft plans was a mistaken assumption; **we do not require or create that branch**.

## Verified implementation

| Machine | Port-side Forge GameTests | Status at 18 Oct 2026 checkpoint |
|---|---:|---|
| Infuser | 19 | Functional; item limits, energy, recipes, GUI/input rules |
| Crafter | 9 | Functional; ghost blueprint, recipe execution, container limits |
| Farmer | 8 | Functional server engine and GUI; 17 recipes, menu tests |
| **Total** | **36** | **36/36 GameTests passed at last recorded checkpoint** |

The four additional machines, Fertilizer, Infusion Altarnator, Awakening Altarnator and Enchanternator, remain registered shells. The current work is still not safe for Steph's existing Minecraft world. See `PORTING.md`, `CLIENT-QA.md` and `FARMER-PORT.md` for actual 1.20.1 Forge evidence.

## How the automation works

- `nex-minecraft-mod-ci.yml` builds **both** original 1.21.1 NeoForge (Java 21) and Forge port 1.20.1 (Java 17); on Linux it runs the **actual** 36 Forge GameTests; on Windows it builds the port. These are separate from runtime visual tests.
- `nex-backport-parity.yml` confirms that the catalogue covers exactly the real registered port GameTests, exercises the comparator, and checks two **real measured observation snapshots** when present.
- `cases.json` records all 36 real port GameTest method identities; `check_coverage.py` catches missing, newly added or renamed tests.
- `compare.py` rejects missing cases, failed observations, mismatched Minecraft/loader environments and differing recorded outputs. An absent pair of evidence snapshots reports **PARITY NOT MEASURED** by default; `--strict` is for future release gates and fails if the snapshots are absent.

## Capturing reference and target behaviour

For each deterministic scenario, execute equivalent test inputs in:
1. Original Minecraft 1.21.1 NeoForge, `./`, Java 21.
2. Backport Minecraft 1.20.1 Forge 47.4.24, `forge-port/`, Java 17.

Record observed machine inputs/outputs, FE/energy changes, recipe/upgrade behaviour, tick progression, inventory persistence and state transitions. Port-side Forge GameTest success alone **is not equivalent** to a 1.21.1 reference capture. Matching GameTests and evidence export on the original source still need implementation; do not synthesize results from assertions or screenshots.

When actual observation exporters exist, store `qa/backport/evidence/reference.json` and `qa/backport/evidence/target.json` (schema documented in `compare.py` and the preceding comparator README) with every catalogue case. Never add fabricated green outputs.

## Launchers and graphical tests

`launcher-matrix.json` defines Prism Launcher, CurseForge and ATLauncher scenarios across clean reference and port profiles. Pin the actual launcher's release, Minecraft version, loader and dependencies before measuring. The review-only `review/ci-minecraft-qa` workflow already experiments with disposable HeadlessMC and Forge visual QA; keep this separate until the results are reviewed. Never install experimental port builds in Steph's real world.

These are incremental tests for the **same ongoing seven-machine port**, not a new project.
