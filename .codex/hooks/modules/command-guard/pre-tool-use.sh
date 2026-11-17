#!/usr/bin/env bash
set -euo pipefail

payload="$(cat || true)"
blocked='(^|[;&|[:space:]])(sudo[[:space:]]+)?rm[[:space:]]+-[[:alnum:]]*r[[:alnum:]]*f[[:space:]]+(/|\\|[[:space:]]*\.\.?([/\\]|$)|[A-Za-z]:[\\/]*$)|(^|[;&|[:space:]])mkfs([[:space:]]|$)|(^|[;&|[:space:]])dd[[:space:]].*of[[:space:]]*=[[:space:]]*/dev/|curl[^\n|]*\|[[:space:]]*(sh|bash)|wget[^\n|]*\|[[:space:]]*(sh|bash)|git[[:space:]]+reset[[:space:]]+--hard|git[[:space:]]+clean[[:space:]]+-fdx|git[[:space:]]+push[[:space:]]+(-f|--force)|docker[[:space:]]+system[[:space:]]+prune|gh[[:space:]]+pr[[:space:]]+merge|gh[[:space:]]+release([[:space:]]|$)|(^|[;&|[:space:]])(/dev/|/dev/[^[:space:]]+)'

if printf '%s' "$payload" | grep -Eiq "$blocked"; then
  echo "command-guard blocked: destructive commands, force push, protected-branch push, and merge/deploy commands are not allowed." >&2
  exit 2
fi
if printf '%s' "$payload" | grep -Eiq 'git[[:space:]]+push.*(main|develop)'; then
  echo "command-guard blocked: pushing directly to main/develop is not allowed." >&2
  exit 2
fi
exit 0
