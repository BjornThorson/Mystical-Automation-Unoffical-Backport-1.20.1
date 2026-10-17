#!/usr/bin/env python3
"""Static consistency check for the 1.20.1 port foundation."""
from pathlib import Path
import json
import re

base = Path(__file__).resolve().parents[1]
code = base / "src/main/java/com/blakebr0/mysticalautomation"
resources = base / "src/main/resources"
expected = {
    "infuser": 9,
    "crafter": 11,
    "farmer": 16,
    "fertilizer": 9,
    "infusion_altarnator": 11,
    "awakening_altarnator": 11,
    "enchanternator": 5,
}
blocks = (code / "init/ModBlocks.java").read_text()
items = (code / "init/ModItems.java").read_text()
tiles = (code / "init/ModBlockEntities.java").read_text()
machine = (code / "tileentity/MachineShellBlockEntity.java").read_text()
for name, slots in expected.items():
    assert re.search(r'register\("' + name + r'",\s*\(\)\s*->\s*new MachineShellBlock\(\w+\.'
                     + name.upper() + r',\s*' + str(slots) + r'\)', blocks), name + " block registration"
    assert f'blockItem("{name}", ModBlocks.{name.upper()})' in items, name + " item registration"
    if name in ("infuser", "crafter", "farmer"):
        block_entity = name.capitalize() + "BlockEntity::new"
        assert f'REGISTRY.register("{name}"' in tiles and block_entity in tiles,                 name + " concrete block-entity registration"
    else:
        assert f'register("{name}", ModBlocks.{name.upper()})' in tiles, name + " block-entity registration"
    assert (resources / f'assets/mysticalautomation/models/item/{name}.json').exists(), name + " item model"
    assert (resources / f'assets/mysticalautomation/models/block/{name}.json').exists(), name + " block model"
    assert (resources / f'assets/mysticalautomation/blockstates/{name}.json').exists(), name + " state"
    assert (resources / f'data/mysticalautomation/loot_tables/blocks/{name}.json').exists(), name + " loot"
    states = json.loads((resources / f'assets/mysticalautomation/blockstates/{name}.json').read_text())
    assert len(states["variants"]) == 8, name + " states"
    loot = json.loads((resources / f'data/mysticalautomation/loot_tables/blocks/{name}.json').read_text())
    assert loot["pools"][0]["entries"][0]["name"] == "mysticalautomation:" + name
assert machine.count('tag.put("Inventory"') == 1
assert machine.count('tag.put("Energy"') == 1
assert machine.count('tag.contains("Inventory"') == 1
assert machine.count('tag.contains("Energy"') == 1
assert 'BASE_ENERGY_CAPACITY = 80_000' in machine
assert 'ForgeCapabilities.ITEM_HANDLER' in machine
assert 'ForgeCapabilities.ENERGY' in machine
print(f"PASS: {len(expected)} registered blocks, items, block entities and asset/loot sets")
print("PASS: 1.21.1 machine inventory slot counts, NBT save/load and Forge capability hooks present")
print("CAUTION: this is a static checker; run Minecraft GameTests for behaviour and persistence")

# Farmer recipe registry and data parity. Static check only; runtime
# serialization and growth are exercised by the Minecraft GameTests.
recipes = resources / "data/mysticalautomation/recipes/farmer"
farmer_recipes = list(recipes.glob("*.json"))
assert len(farmer_recipes) == 17, f"Expected all 17 upstream Farmer recipes; got {len(farmer_recipes)}"
assert (resources / "data/mysticalautomation/recipes/farmer.json").exists(), "Farmer machine craft recipe"
print(f"PASS: {len(farmer_recipes)} Farmer recipe JSON files and machine crafting recipe")
