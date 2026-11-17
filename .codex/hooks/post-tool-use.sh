#!/usr/bin/env bash
set -euo pipefail

payload="$(cat || true)"
if printf '%s' "$payload" | grep -Eiq '\.(kt|kts)([" ]|$)'; then
  echo "post-tool verification notice: Kotlin changes detected; check the final compileKotlin/test." >&2
fi
if printf '%s' "$payload" | grep -Eiq '\.(json|toml|sh)([" ]|$)'; then
  echo "post-tool verification notice: config or shell changes detected; check syntax validation." >&2
fi
exit 0
