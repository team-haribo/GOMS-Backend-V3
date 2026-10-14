#!/usr/bin/env bash
set -euo pipefail
exec bash "$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../../.codex/hooks/modules/command-guard" && pwd)/pre-tool-use.sh"
