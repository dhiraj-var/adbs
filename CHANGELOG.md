# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [3.0.0] - 2026-09-02

### Added
- Split the project into a multi-module Maven build: `adbs-core` (the converter, zero framework dependencies) and `adbs-spring` (Spring MVC bean wiring + a global exception handler).
- `adbs-spring`'s `NdcConfiguration` (`@Configuration` + `@Bean`) — import this or component-scan `com.nepalidate.ADBS.spring` to get `NDC` as a Spring bean.
- GitHub Actions CI (`.github/workflows/ci.yml`) running `mvn verify` (tests, JaCoCo coverage, SpotBugs static analysis) on every push and PR to `main`.
- `.github/workflows/release-check.yml` — fails a tag push if the pushed tag's version doesn't match the parent pom's version, closing the previous manual, error-prone version/tag process.
- `jitpack.yml` pinning the JitPack build JDK to OpenJDK 17 (JitPack's own default is Java 8, which would otherwise fail to build this project).
- JaCoCo coverage reporting and SpotBugs static analysis, both wired into the Maven `verify` phase for every module.
- `.editorconfig` for consistent formatting across editors.
- New tests: `LookupTest` (exercises the CSV loader directly, including malformed-input handling), `FullRangeRoundTripTest` (exhaustively round-trips every day in the entire supported range, not just spot checks), `ThreadSafetyTest` (verifies the claimed thread-safety of a shared `NDC` instance under concurrent load).
- A load-time consistency check in `Lookup` that verifies each year's month-day-count total matches the actual day gap to the next year's new-year date, so a corrupted or miscalculated calendar-data row fails loudly and specifically at load time instead of producing a wrong or confusingly-mislabeled error later.
- `CalendarDataUnavailableException` — thrown for any calendar-data loading problem (missing CSV, malformed rows, internal inconsistencies) instead of a raw `ExceptionInInitializerError`.
- `CONTRIBUTING.md` and `RELEASING.md`.

### Fixed
- Corrected internally-inconsistent rows in `nepali_dates.csv` — each affected year's 12 month-day-counts didn't sum to the actual gap between its new-year date and the next year's new-year date, which produced wrong or incorrectly-rejected conversions for real dates near those year boundaries (e.g. `adToBs("1916-04-12")` incorrectly threw `DateRangeNotSupported`). Corrected years: BS 1972, 1974, 1975, 1980, 1990, 1992, 1996, 1999, 2083, 2085, 2086, 2089, 2090, 2092, 2093, 2098 (1974 and 1975 weren't part of the originally-detected 16 but were exposed by fixing their neighbors). Cross-checked against 4 independent sources (`amitgaru/nepali-datetime`, `sbmdkl/nepali-date-converter`, `remotemerge/nepali-date-converter`, and the original upstream `ADBS` project this library was rewritten from) — most corrections have 2+ independent sources agreeing.
  - **Known caveat:** for BS 1972 and BS 1974, no source (including upstream) has the correct per-month breakdown — only the correct yearly total is well-corroborated. The extra day was placed in Chaitra (the last month) as the least-disruptive choice; this is a best-effort placement, not independently verified, and should be revisited if an authoritative source for these two years is found.
- `Lookup`'s calendar-data loading no longer runs in a static initializer block. Previously, any load failure (a bad or missing CSV) threw `ExceptionInInitializerError` and permanently poisoned the class for the rest of the JVM's life — even a subsequent valid CSV couldn't recover it without restarting the process. Loading is now lazy and retry-capable.
- Removed the dead `throws ParseException` from `AdBs.convertAdToBs` — the method never actually threw it (already noted as backward-compatibility-only in its own doc comment).
- `ErrResponse` no longer exposes its internal `Date` field directly (defensive copy on both the constructor and getter) — flagged by the newly-added SpotBugs check.

### Changed (breaking)
- **`NDC` is no longer a Spring bean by default.** It has zero Spring dependency now. If you relied on component-scanning to auto-wire `NDC`, add the `adbs-spring` dependency and either `@Import(NdcConfiguration.class)` or component-scan `com.nepalidate.ADBS.spring`.
- **`ExceptionHandling` and `ErrResponse` moved from the core artifact to a new `adbs-spring` artifact.** Add `com.nepalidate:adbs-spring` (or, via JitPack, `com.github.dhiraj-var.adbs:adbs-spring`) if you use these classes. The Java package (`com.nepalidate.ADBS.ExceptionHandling`) is unchanged — only which jar they ship from has changed.
- **JitPack coordinates changed.** Because this is now a multi-module repo, JitPack publishes per-module coordinates: `com.github.dhiraj-var.adbs:adbs-core:v3.0.0` and `com.github.dhiraj-var.adbs:adbs-spring:v3.0.0`. The old-style single coordinate (`com.github.dhiraj-var:adbs:v3.0.0`) still resolves via JitPack's aggregator, but it transitively pulls in `adbs-spring` (and therefore Spring) even for consumers who only want the core converter — update to the per-module coordinate that matches what you actually need. See the README's "Migrating from v2.0.0" section.
- `AdBs.convertAdToBs` no longer declares `throws ParseException` — a source-incompatible change only for callers with an explicit `catch (ParseException e)` around this call (their catch block becomes unreachable and won't compile).

## [2.0.0]
- Complete rewrite for performance: O(1) data access, +57-estimation algorithm for AD→BS
- Calendar data moved from Java source code to `nepali_dates.csv` — easy to update without code changes
- All error messages rewritten in plain language with examples and valid ranges
- Removed Spring Boot dependency; JAR is now 14 KB instead of 17 MB
- Java 17
- Backward compatible with the 1.0.0 API — same class names, same method signatures, same packages

## [1.0.0]
- Initial release (Spring Boot 2.6.3, Java 11)
