---
name: write-pr
description: Writes a GOMS PR reflecting the PR template, base, labels, and verification results.
---

# Writing a Pull Request

## Trigger

Use when the user requests creating a PR or writing a PR body.

## Required Tools

Use the PR template, git diff/log, gh pr list/view/create, gh label list, and Gradle verification.

## Investigation Steps

1. Read .github/pull_request_template.md and 3-5 recent merged PRs, and check body length and bullet usage.
2. Confirm that the base of a regular development PR is develop. Use main only for release PRs.
3. Following the GitHub Metadata rules in `AGENTS.md`, check the related Issue's existence, assignee, and labels, the PR title convention, and the actual labels.
4. Summarize the changed files and verification results truthfully.

## Execution Steps

Write a concise Korean title and a body that keeps the existing template structure. In `관련 이슈` (Related issues), use `Closes #<issue-number>` when it resolves the issue and `Related to #<issue-number>` when it is merely related. Write only the key changes briefly in `작업내용` (Changes), and only what is actually needed in `참고사항` (Notes). Keep the existing headings and checklist, and do not add headings that are not in the template by default. Check only the items actually performed, and before creating the PR, finalize the develop base, related Issue, and existing labels according to `AGENTS.md`.

## Verification Steps

Record JSON/TOML/shell/harness consistency, Gradle, and git diff --check results in the body, and after pushing, re-query the PR's title, base/head, draft, assignee, labels, related Issue, files, and CI. If the metadata differs after creation, fix it and re-verify.

## Stop Conditions

Do not create the PR if the base, permissions, change scope, or labels are unclear, or if unrelated files are mixed in.

## Prohibited Actions

Do not create new labels, open regular PRs targeting main, check verifications that were not actually done, or merge/auto merge/release/deploy.

## Final Report

Report the branch, commits, title, base/head, assignee/labels, verification, PR URL, and that it was not merged.
