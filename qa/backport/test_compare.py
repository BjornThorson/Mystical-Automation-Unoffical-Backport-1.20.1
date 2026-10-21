import json
import tempfile
import unittest
from pathlib import Path
from compare import compare, run

CATALOG = {
    "schema_version": 1,
    "reference": {"minecraft": "1.21.1", "loader": "neoforge"},
    "target": {"minecraft": "1.20.1", "loader": "forge"},
    "cases": [{"id": "crafter.single_cycle"}, {"id": "farmer.harvest"}]
}


def result(mc, loader):
    return {"schema_version": 1, "environment": {"minecraft": mc, "loader": loader},
            "cases": {i["id"]: {"status": "passed", "observed": {"output": 1}}
                      for i in CATALOG["cases"]}}


class ComparatorTests(unittest.TestCase):
    def setUp(self):
        self.a = result("1.21.1", "neoforge")
        self.b = result("1.20.1", "forge")

    def test_identical_behaviour_matches(self):
        self.assertEqual(compare(CATALOG, self.a, self.b), [])

    def test_changed_output_fails(self):
        self.b["cases"]["crafter.single_cycle"]["observed"]["output"] = 2
        self.assertTrue(any("BEHAVIOUR DIFFERENCE" in e for e in compare(CATALOG, self.a, self.b)))

    def test_missing_case_fails(self):
        del self.b["cases"]["farmer.harvest"]
        self.assertTrue(any("missing farmer.harvest" in e for e in compare(CATALOG, self.a, self.b)))

    def test_wrong_loader_fails(self):
        self.b["environment"]["loader"] = "neoforge"
        self.assertTrue(any("target: loader" in e for e in compare(CATALOG, self.a, self.b)))

    def test_failed_gametest_fails(self):
        self.a["cases"]["crafter.single_cycle"]["status"] = "failed"
        self.assertTrue(any("not passed" in e for e in compare(CATALOG, self.a, self.b)))

    def test_missing_files_are_not_falsely_a_pass(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "cases.json").write_text(json.dumps(CATALOG))
            self.assertEqual(run(root / "cases.json", root / "ref.json", root / "target.json", False), 0)
            self.assertEqual(run(root / "cases.json", root / "ref.json", root / "target.json", True), 1)


if __name__ == "__main__":
    unittest.main()
