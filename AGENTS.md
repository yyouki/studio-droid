# AI contributor instructions

These instructions apply to ChatGPT, Claude, and other coding assistants working on StudioDroid.

## Source of truth

Read these files before making project changes:
- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/MEMORY-PROFILES.md`
- `docs/ROADMAP.md`
- `CONTRIBUTING.md`
- `THIRD_PARTY_NOTICES.md`

The GitHub repository is the shared source of truth. Never assume another assistant has seen private chat context. Include necessary context in the issue, pull request, or committed documentation.

## Product requirements

- Target is an installable Android application, not a desktop-only build.
- The project investigates porting/adapting Android Studio Desktop and compatible IntelliJ components; it must not claim that desktop UI or plugins work unchanged on Android.
- Devices with total RAM at or below 6 GiB use LOW_MEMORY by default.
- Devices above 6 GiB use STANDARD by default, without the project's low-memory reductions.
- LOW_MEMORY must reduce resource pressure without silently removing supported IDE features.
- The maintainer's reference device is a Redmi 14C with Android 16 and 6 GB RAM.

## Working rules

1. Work on one explicit issue or task at a time.
2. Inspect the current repository state before editing.
3. Do not overwrite user or contributor work without reviewing the current file and its history.
4. Prefer small commits and focused pull requests.
5. Record exact upstream URLs, revisions, licenses, and changes for adapted code.
6. Do not introduce speculative dependencies or unverified Android APIs.
7. Do not claim code compiles, tests pass, or an APK installs unless that verification actually occurred.
8. If a build cannot be run in the current environment, state that clearly and provide exact verification steps.
9. Keep documentation synchronized with implementation and known limitations.
10. Avoid committing generated binaries, build caches, secrets, signing keys, or API credentials.

## ChatGPT / Claude handoff protocol

The assistants do not share an automatic live session. The project maintainer relays task packets between them.

A useful handoff packet contains:
- Task ID and issue/PR URL.
- Current branch and commit SHA.
- Exact files changed.
- Expected behavior and acceptance criteria.
- Tests actually run and their results.
- Open questions and known risks.

The receiving assistant must inspect the referenced commit and files rather than trust an unverified summary. One assistant may implement while the other reviews; only the maintainer decides which changes are accepted and merged.

## Required response format for task reports

- **Task:** identifier and goal.
- **Changes:** files and behavior changed.
- **Verification:** commands/tests actually run.
- **Limitations:** what remains unverified.
- **Handoff:** exact next task or review request.
