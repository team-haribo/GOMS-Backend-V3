#!/usr/bin/env bash
set -euo pipefail
exec bash "$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../../.codex/hooks/modules/secret-guard" && pwd)/pre-tool-use.sh"
