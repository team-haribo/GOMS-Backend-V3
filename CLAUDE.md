# GOMS Backend V3 Claude Code Instructions

The source of truth for project rules is AGENTS.md at the repository root. This file only supplements Claude Code's execution and tool-use instructions; if it conflicts with AGENTS.md, follow AGENTS.md.

## Work Order

1. Check the current path, git status, branch, origin/develop, and related PRs.
2. Before changing anything, read the related code, tests, PR template, and project docs.
3. Make minimal changes without going beyond the requested scope. In harness work with no application behavior change, do not modify src or build.gradle.
4. After changes, self-review the diff and run syntax, consistency, and Gradle verification.
5. Proceed to commit, push, and PR creation only when the user explicitly asks. Do not merge, auto merge, release, or deploy.

## Tool Use

- For reading, prefer rg, git diff, git log, and gh.
- Modify files with patches so existing files are not overwritten.
- Do not read or print the contents of secrets, .env, Firebase credentials, or private keys.
- Dangerous commands are blocked by the repository's .codex/hooks and .claude/hooks. Do not bypass the blocks.
- Do not add new dependencies, formatters, or plugins.
- Do not write Gradle tasks whose existence you have not verified into run instructions.

## GOMS Core Rules

DTOs use Kotlin data classes and @field: validation, and Controllers return ResponseEntity. Exceptions follow the GlobalException/ErrorCode contract, and logs use LogFormat.message(). Apply a readOnly transaction to reads and a transaction to state changes. For detailed rules and real examples, read AGENTS.md and .agents/skills.

## Verification Defaults

When possible, run ./gradlew compileKotlin test and ./gradlew build --no-daemon. On Windows, use gradlew.bat. Do not report test failures as success; record the first project-code stack frame and the cause.
