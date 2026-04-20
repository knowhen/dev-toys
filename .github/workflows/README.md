# GitHub Actions Workflows

Continuous-integration configuration for DevToys Java.

## Files

### `ci.yml` — Build & Test

Triggered on:
- Every push to `master`
- Every pull request targeting `master`
- Manual dispatch from the Actions tab

What it does:
- Runs on **Linux (Ubuntu)** and **Windows** in parallel
- Sets up JDK 21 (Eclipse Temurin)
- Caches `~/.m2/repository` keyed on all `pom.xml` files
- Runs `mvn -B clean install` — this compiles every module and runs every test
- Uploads Surefire test reports as artifacts (7-day retention) so failures
  can be inspected from the Actions UI without reproducing locally

Typical runtime (cold cache): **3-5 minutes** per OS.
With warm cache: **1-2 minutes** per OS.

## Viewing test failures

When CI fails, click on the failing run, then scroll to **Artifacts** at the
bottom. Download `surefire-reports-<os>` and look inside
`<module>/target/surefire-reports/` for the `*.txt` or `*.xml` file of the
failing test class.

## Adding more workflows

Common additions for later:

| Need                | Add                                                                |
| ------------------- | ------------------------------------------------------------------ |
| Release packaging   | `release.yml` triggered on `push: tags: ['v*']` running `jpackage` |
| macOS coverage      | Add `macos-latest` to the matrix in `ci.yml`                       |
| Nightly dependency  | `nightly.yml` with `schedule: [{cron: '0 3 * * *'}]`               |
| Static analysis     | Run SpotBugs / PMD / Error Prone as extra step                     |
| Coverage report     | JaCoCo + Codecov upload                                            |

## Notes

- `concurrency` cancels previous runs on the same branch when a new commit
  lands, saving Action minutes during active development.
- `fail-fast: false` keeps both OS jobs running even when one fails, so
  you get complete diagnostic info on a single push.
- The built-in `cache: 'maven'` in `setup-java@v4` is simpler than a manual
  `actions/cache` step and works for this project. If cache restoration
  stops working for any reason (there are [known quirks][1]), switch to a
  manual step with `actions/cache@v4` and `path: ~/.m2/repository`.

[1]: https://github.com/orgs/community/discussions/178460
