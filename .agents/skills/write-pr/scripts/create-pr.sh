#!/usr/bin/env bash
set -euo pipefail

base="${1:-develop}"
title="${2:?PR title is required}"
body_file="${3:?PR body file is required}"
gh pr create --base "$base" --title "$title" --body-file "$body_file"
