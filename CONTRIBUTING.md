# Contributing

## Local setup

Requires Java 17+ and Maven 3.6+.

```bash
mvn clean install
```

Builds both modules (`adbs-core`, `adbs-spring`), runs all tests, JaCoCo coverage, and SpotBugs static analysis (`verify` phase), and installs both jars into `~/.m2`.

To work on just one module:

```bash
mvn -pl adbs-core -am test
mvn -pl adbs-spring -am test
```

## Running tests

```bash
mvn test
```

`FullRangeRoundTripTest` is tagged `@Tag("slow")` — it exhaustively round-trips every day in the entire supported range (~47,800 iterations) rather than spot-checking. To skip it during a fast local dev loop:

```bash
mvn test -DexcludedGroups=slow
```

CI always runs the full suite, including the slow tag.

## Static analysis

```bash
mvn spotbugs:check
```

This runs automatically as part of `mvn verify` / `mvn install` and will fail the build on any finding. SpotBugs was chosen over Checkstyle because it catches correctness defects (relevant here given the manual date arithmetic in `AdBs` and manual CSV parsing in `Lookup`) rather than style violations — this codebase has some intentional non-standard naming in its public API (`validateDate_bs`, `field_name`) that a strict style linter would immediately flag for no real benefit.

## Adding or fixing calendar data

All calendar data lives in `adbs-core/src/main/resources/nepali_dates.csv`. See the README's "Updating Calendar Data" section for the file format. After any edit, `Lookup` validates at load time (exercised by `LookupTest` and `FullRangeRoundTripTest`) that:

- all years 1970–2100 are present,
- every month has 29–32 days,
- and each year's 12 month-day-counts sum to exactly the gap between its new-year date and the next year's new-year date.

Run `mvn -pl adbs-core test` after any CSV edit — a violation of any of these throws `CalendarDataUnavailableException` naming the specific bad row.

## Branch and PR conventions

Branch names: `<type>/<short-description>`, e.g. `fix/leap-year-boundary`, `refactor/v3-modular-spring-split`.

## Releasing

See [RELEASING.md](RELEASING.md).
