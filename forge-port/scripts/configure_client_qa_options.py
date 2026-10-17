#!/usr/bin/env python3
"""Set the Forge development client's disposable QA simulation distance.

This explicitly invoked helper only edits forge-port/run/options.txt.
It is not part of the distributed mod and never changes other Minecraft profiles.
"""

import argparse
from pathlib import Path


VALID_SIMULATION_DISTANCE = 5
OPTIONS_FILE = Path(__file__).resolve().parents[1] / "run" / "options.txt"


def updated_options(contents: str) -> str:
    lines = contents.splitlines(keepends=True)
    matches = [i for i, line in enumerate(lines) if line.startswith("simulationDistance:")]
    if len(matches) != 1:
        raise ValueError(f"Expected one simulationDistance entry; found {len(matches)}")
    index = matches[0]
    current_line = lines[index]
    newline = "\r\n" if current_line.endswith("\r\n") else ("\n" if current_line.endswith("\n") else "")
    lines[index] = f"simulationDistance:{VALID_SIMULATION_DISTANCE}{newline}"
    return "".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Verify the QA setting without modifying it")
    args = parser.parse_args()

    if not OPTIONS_FILE.is_file():
        parser.error(f"Missing disposable QA options: {OPTIONS_FILE}. Launch the dev client once first.")

    # Keep the original newline style and unrelated settings unchanged.
    with OPTIONS_FILE.open("r", encoding="utf-8", newline="") as handle:
        original = handle.read()
    try:
        updated = updated_options(original)
    except ValueError as exc:
        parser.error(str(exc))

    if args.check:
        print(f"QA simulation distance {'OK' if updated == original else 'INVALID'}: expected {VALID_SIMULATION_DISTANCE}")
        return 0 if updated == original else 1
    if updated != original:
        with OPTIONS_FILE.open("w", encoding="utf-8", newline="") as handle:
            handle.write(updated)
    print(f"QA simulation distance set to {VALID_SIMULATION_DISTANCE} chunks"
          + (" (unchanged)" if updated == original else ""))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
