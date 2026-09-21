#!/usr/bin/env bash
set -euo pipefail
base="${1:-develop}"
head="${2:-$(git branch --show-current)}"
gh pr list --state open --base "$base" --head "$head" --json number,title,url,baseRefName,headRefName
