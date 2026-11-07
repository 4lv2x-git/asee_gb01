import unittest
from pathlib import Path
from urllib.parse import urlparse

import yaml

from src.main import app


HTTP_METHODS = {"get", "post", "put", "patch", "delete"}
PROJECT_ROOT = Path(__file__).resolve().parents[3]
OPENAPI_FILES = (
    PROJECT_ROOT / "docs" / "openapi" / "artistas.yml",
    PROJECT_ROOT / "docs" / "openapi" / "blog.yml",
)


class OpenApiContractTests(unittest.TestCase):
    def test_implemented_routes_match_openapi_contracts(self):
        documented_operations = set()
        for openapi_file in OPENAPI_FILES:
            specification = yaml.safe_load(
                openapi_file.read_text(encoding="utf-8")
            )
            server_prefix = urlparse(
                specification["servers"][0]["url"]
            ).path.rstrip("/")

            for path, path_item in specification["paths"].items():
                for method in path_item:
                    if method.lower() in HTTP_METHODS:
                        documented_operations.add(
                            (f"{server_prefix}{path}", method.lower())
                        )

        implemented_operations = {
            (route.path, method.lower())
            for route in app.routes
            if route.path.startswith("/v1/")
            for method in route.methods
            if method.lower() in HTTP_METHODS
        }

        self.assertEqual(documented_operations, implemented_operations)


if __name__ == "__main__":
    unittest.main()
