---
name: git-commit
description: Reviews the scope of GOMS changes and commits safely using the existing commit rules.
---

# Git Commit

## Trigger

Use when the user requests a commit or when cohesive changes need to be committed before a PR.

## Required Tools

Use git status, diff, log, diff --check, and show.

## Investigation Steps

Check the current branch, base, user changes, and the titles of the last 30 commits. Distinguish unrelated files and pre-existing untracked files.

## Execution Steps

For harness work, decide whether the actual diff is chore or docs. The default message is `chore: AI 개발 하네스 구성` ("chore: set up AI development harness"). Stage only the related files and re-read the staged diff.

## Verification Steps

Run git diff --cached --check and git status --short, then commit and verify with git show --stat --oneline HEAD and git show --check HEAD.

## Stop Conditions

Do not commit if there are unrelated changes, suspected secret values, conflicts with user files, or a contaminated base.

## Prohibited Actions

Do not run reset --hard, clean -fdx, or force push, add Co-authored-by automatically, guess issue numbers, or add session URLs.

## Final Report

Report the commit SHA, message, included files, excluded user changes, and staged verification results.
