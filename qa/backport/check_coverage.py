#!/usr/bin/env python3
"""Verify every parity scenario refers to a real existing Forge port GameTest."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / "forge-port/src/main/java/com/blakebr0/mysticalautomation/gametest"
CATALOG = Path(__file__).with_name("cases.json")

def collect():
    cases = set()
    by_machine = {}
    for machine in ("infuser", "crafter", "farmer"):
        path = BASE / (machine.capitalize() + "GameTests.java")
        if not path.is_file():
            raise SystemExit(f"Missing Forge GameTest class: {path}")
        text = path.read_text(encoding="utf-8")
        names = re.findall(
            r"@GameTest\b[\s\S]{0,200}?\bpublic\s+static\s+void\s+(\w+)\s*\(",
            text,
        )
        if len(names) != len(set(names)):
            raise SystemExit(f"Duplicate GameTest method: {machine}")
        by_machine[machine] = len(names)
        cases.update(f"{machine}.{name}" for name in names)
    return cases, by_machine

if __name__ == "__main__":
    known, totals = collect()
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    recorded = [c.get("id") for c in catalog["cases"]]
    if len(recorded) != len(set(recorded)):
        raise SystemExit("Duplicate parity case ID")
    missing = known - set(recorded)
    obsolete = set(recorded) - known
    if missing or obsolete:
        raise SystemExit(
            f"Parity catalog drift: missing={sorted(missing)}, "
            f"obsolete={sorted(obsolete)}"
        )
    if len(known) < 36:
        raise SystemExit(f"Backport GameTest coverage declined: {len(known)} < 36")
    print(f"Verified {len(known)} real Forge GameTests: {totals}")
    print("Reference-vs-backport behavioural evidence remains a separate requirement.")
