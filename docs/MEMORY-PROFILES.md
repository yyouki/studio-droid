# Adaptive memory profiles

## Policy

StudioDroid has one application and two default runtime profiles:

| Total physical RAM | Profile | Expected behavior |
| --- | --- | --- |
| `<= 6 GiB` | `LOW_MEMORY` | Apply memory and concurrency controls. |
| `> 6 GiB` | `STANDARD` | Preserve standard component behavior; do not apply low-memory reductions. |

The project uses GiB consistently in documentation. The implementation must define and test the exact byte threshold rather than compare rounded display strings.

## Detection

Prefer Android's system-reported total memory, for example `ActivityManager.MemoryInfo.totalMem`. Record the source and confidence of the detected value.

Supporting signals:
- `ActivityManager.isLowRamDevice()`.
- `ActivityManager.getMemoryClass()` and `getLargeMemoryClass()`.
- Current available memory from `ActivityManager.MemoryInfo.availMem`.

The physical-RAM threshold selects the default profile. Current memory pressure is a separate runtime signal and may temporarily pause or defer expensive work without changing the device's selected profile.

If total physical RAM cannot be read, use the Android low-RAM signal and memory-class information as a documented fallback. Do not silently report a guessed RAM value.

## LOW_MEMORY controls

The following are candidates, to be measured before final values are chosen:

- Limit simultaneous indexing jobs and index projects incrementally.
- Bound editor, syntax, and project caches; evict least-recently-used inactive entries.
- Limit Gradle and language-server worker concurrency.
- Avoid starting non-essential background tasks while the application is idle or in the background.
- Cancel stale analysis when a file changes again.
- Release project resources when a workspace closes.
- Make large file and large project operations incremental and cancellable.

The profile must preserve supported user-facing behavior. Do not silently disable syntax highlighting, completion, navigation, diagnostics, or project operations. If a feature cannot run within available resources, show an explicit status and allow a retry.

## STANDARD controls

- Do not apply the LOW_MEMORY profile's special cache caps or concurrency reductions.
- Use the standard defaults of the selected editor, language service, and build tools.
- Keep normal background indexing and analysis behavior.
- Respect Android lifecycle and critical operating-system memory pressure. Safety behavior required by Android is not a low-memory product optimization.

## User controls

An advanced settings screen may eventually expose:
- Automatic (default).
- Force Low-memory (troubleshooting).
- Force Standard (advanced; may increase memory use).

Manual selection must be clearly labelled and must not override Android's need to recover from critical process pressure.

## Measurements

Before tuning constants, record:
- Cold launch and warm launch time.
- Time to open a representative project.
- Indexing duration and peak process memory.
- Editor responsiveness while indexing.
- Gradle sync/build duration and peak process memory.
- Background memory after closing a project.
- Crash, ANR, and out-of-memory event counts.

Compare LOW_MEMORY on a 6 GB-or-lower device against STANDARD on a device above 6 GB. Do not call an optimization successful unless measurements show a benefit without unacceptable feature or correctness regressions.
