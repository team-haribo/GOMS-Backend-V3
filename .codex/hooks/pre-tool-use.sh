#!/usr/bin/env bash
set -euo pipefail

payload="$(cat || true)"
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

if ! printf '%s' "$payload" | bash "$root/.codex/hooks/modules/command-guard/pre-tool-use.sh"; then
  exit 1
fi
if ! printf '%s' "$payload" | bash "$root/.codex/hooks/modules/secret-guard/pre-tool-use.sh"; then
  exit 1
fi
exit 0
