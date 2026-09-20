#!/usr/bin/env bash
set -euo pipefail

payload="$(cat || true)"
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
if ! printf '%s' "$payload" | bash "$root/.claude/hooks/modules/command-guard/preToolUse.sh"; then exit 1; fi
if ! printf '%s' "$payload" | bash "$root/.claude/hooks/modules/secret-guard/preToolUse.sh"; then exit 1; fi
exit 0
