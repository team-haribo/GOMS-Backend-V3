---
name: resolve-reviews
description: Collects and classifies review comments on the current PR and applies fixes only for valid comments.
---

# Resolve Reviews

## Trigger

Use when the current branch's PR has inline reviews, general comments, changed files, or a request to address reviews.

## Required Tools

Use gh pr view, gh api, git diff, related sources and tests, and Gradle. The default base is develop.

## Investigation Steps

1. Automatically find the PR linked to the current branch.
2. Collect all review threads, general comments, the changed diff, and CI status.
3. Classify each comment as VALID, INVALID, or PARTIAL based on AGENTS.md and the actual code and tests.

## Execution Steps

Fix only VALID comments with minimal changes, and separate out the uncertain scope of PARTIAL ones. Reply to INVALID ones with evidence from the code. After fixing, run related tests and the full build.

## Verification Steps

Cross-check changes, tests, and replies for each review comment, and check git diff --check. Commit and push only when the user requests it.

## Stop Conditions

Do not decide arbitrarily when comments conflict with each other or an architecture/API contract decision is needed.

## Prohibited Actions

Do not hide reviews or accept them unconditionally. Do not delete tests, weaken assertions, or auto-merge.

## Final Report

Report the PR, classification per comment, reasons for fixing or not fixing, tests, comment replies, and whether you pushed.
