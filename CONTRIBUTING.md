# Contributing

Thank you for helping with StudioDroid.

## Before starting

1. Read [README.md](README.md), [ARCHITECTURE.md](docs/ARCHITECTURE.md), and [MEMORY-PROFILES.md](docs/MEMORY-PROFILES.md).
2. Open or find an issue describing the task.
3. Keep changes focused and explain the Android use case.
4. Check the license of any code, binary, design, or documentation copied from another project.

## Source and licensing

StudioDroid is an unofficial community project. Do not imply that it is made by Google or JetBrains.

Do not copy upstream source into this repository until:
- The exact upstream repository and revision are recorded.
- The applicable license is verified for the specific files.
- Required copyright, license, and notice files are preserved.
- Redistribution restrictions and bundled dependency licenses are reviewed.

A component being visible on a public Git repository does not automatically mean it can be relicensed under StudioDroid's planned license.

## Code quality

- Prefer small, reviewable pull requests.
- Explain why a change is needed and which devices or Android versions it affects.
- Include tests for behavior changes where practical.
- Avoid blocking the main thread.
- Make long-running work cancellable and lifecycle-aware.
- Avoid unbounded caches, worker pools, and background tasks.
- Do not add secrets, API keys, signing keys, or personal data to commits.
- Do not claim a build or device test passed unless it actually ran.

## Memory-profile changes

Changes to memory policies must:
- Preserve supported user-facing features.
- Include measurements or a reproducible test plan.
- Test the LOW_MEMORY behavior at or below the 6 GB threshold.
- Confirm that devices above the threshold keep the STANDARD profile.
- Explain any change to caches, indexing, worker counts, or background scheduling.

## Pull request checklist

- [ ] The change has a clear issue or task reference.
- [ ] The code and dependencies have been license-checked.
- [ ] Tests or manual verification steps are documented.
- [ ] Android version and ABI impact are documented.
- [ ] Both memory profiles have been considered.
- [ ] No credentials or private data are included.
- [ ] Documentation is updated where needed.
