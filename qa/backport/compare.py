#!/usr/bin/env python3
"""Compare *measured* reference and target GameTest evidence, never inferred results."""
import argparse
import json
import os
import sys
from pathlib import Path


def load_json(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def compare(catalog, reference, target):
    errors = []
    if catalog.get("schema_version") != 1:
        return ["Unsupported case catalog schema"]
    ids = [c.get("id") for c in catalog.get("cases", [])]
    if not ids or len(ids) != len(set(ids)) or any(not isinstance(x, str) or not x for x in ids):
        return ["Invalid or duplicate case IDs in catalog"]

    for label, observed in (("reference", reference), ("target", target)):
        expected = catalog[label]
        if observed.get("schema_version") != 1:
            errors.append(f"{label}: unsupported evidence schema")
            continue
        env = observed.get("environment")
        if not isinstance(env, dict):
            errors.append(f"{label}: missing environment")
            continue
        for field in ("minecraft", "loader"):
            if env.get(field) != expected[field]:
                errors.append(f"{label}: {field} expected {expected[field]}, got {env.get(field)!r}")
        results = observed.get("cases")
        if not isinstance(results, dict):
            errors.append(f"{label}: cases must be an object")
            continue
        extra = sorted(set(results) - set(ids))
        if extra:
            errors.append(f"{label}: unregistered cases: {', '.join(extra)}")
        for ident in ids:
            outcome = results.get(ident)
            if not isinstance(outcome, dict):
                errors.append(f"{label}: missing {ident}")
                continue
            if outcome.get("status") != "passed":
                errors.append(f"{label}: {ident} not passed ({outcome.get('status')})")
            if not isinstance(outcome.get("observed"), dict) or not outcome["observed"]:
                errors.append(f"{label}: {ident} has no measured observations")
    if errors:
        return errors

    for ident in ids:
        a = reference["cases"][ident]["observed"]
        b = target["cases"][ident]["observed"]
        if a != b:
            errors.append(f"BEHAVIOUR DIFFERENCE: {ident}\n  reference={json.dumps(a, sort_keys=True)}\n  backport ={json.dumps(b, sort_keys=True)}")
    return errors


def run(catalog_path, reference_path, target_path, strict=False):
    catalog = load_json(catalog_path)
    missing = [str(p) for p in (reference_path, target_path) if not Path(p).is_file()]
    if missing:
        print("PARITY NOT MEASURED: missing runtime evidence: " + ", ".join(missing))
        return 1 if strict else 0
    issues = compare(catalog, load_json(reference_path), load_json(target_path))
    if issues:
        print("PARITY FAILED:\n" + "\n".join("- " + x for x in issues))
        return 1
    print(f"PARITY MATCH: {len(catalog['cases'])} recorded behaviour scenarios matched exactly.")
    return 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--catalog", default="qa/backport/cases.json")
    parser.add_argument("--reference", default="qa/backport/evidence/reference.json")
    parser.add_argument("--target", default="qa/backport/evidence/target.json")
    parser.add_argument("--strict", action="store_true", help="Require both evidence files, for release gates")
    args = parser.parse_args()
    sys.exit(run(args.catalog, args.reference, args.target, args.strict))


if __name__ == "__main__":
    main()
