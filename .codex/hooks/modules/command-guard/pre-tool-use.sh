#!/usr/bin/env bash
set -euo pipefail

payload="$(cat || true)"
blocked='(^|[;&|[:space:]])(sudo[[:space:]]+)?rm[[:space:]]+-[[:alnum:]]*r[[:alnum:]]*f[[:space:]]+(/|\\|[[:space:]]*\.\.?([/\\]|$)|[A-Za-z]:[\\/]*$)|(^|[;&|[:space:]])mkfs([[:space:]]|$)|(^|[;&|[:space:]])dd[[:space:]].*of[[:space:]]*=[[:space:]]*/dev/|curl[^\n|]*\|[[:space:]]*(sh|bash)|wget[^\n|]*\|[[:space:]]*(sh|bash)|git[[:space:]]+reset[[:space:]]+--hard|git[[:space:]]+clean[[:space:]]+-fdx|git[[:space:]]+push[[:space:]]+(-f|--force)|docker[[:space:]]+system[[:space:]]+prune|gh[[:space:]]+pr[[:space:]]+merge|gh[[:space:]]+release([[:space:]]|$)|(^|[;&|[:space:]])(/dev/|/dev/[^[:space:]]+)'

if printf '%s' "$payload" | grep -Eiq "$blocked"; then
  echo "command-guard 차단: 파괴적 명령·강제 push·보호 브랜치 push·merge/deploy 계열은 허용하지 않습니다." >&2
  exit 2
fi
if printf '%s' "$payload" | grep -Eiq 'git[[:space:]]+push.*(main|develop)'; then
  echo "command-guard 차단: main/develop 직접 push는 허용하지 않습니다." >&2
  exit 2
fi
exit 0
