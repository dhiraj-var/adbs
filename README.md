# adbs

[![JitPack](https://jitpack.io/v/dhiraj-var/adbs.svg)](https://jitpack.io/#dhiraj-var/adbs)
[![CI](https://github.com/dhiraj-var/adbs/actions/workflows/ci.yml/badge.svg)](https://github.com/dhiraj-var/adbs/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A fast, lightweight Java library for converting dates between the **English (Gregorian / AD)** calendar and the **Nepali (Bikram Sambat / BS)** calendar.

As of v3.0.0 this is a two-module project: **`adbs-core`** (the converter itself, zero framework dependencies) and **`adbs-spring`** (optional Spring MVC bean wiring and a global exception handler). Most consumers only need `adbs-core`.

---

## Table of Contents

1. [What is this?](#what-is-this)
2. [Supported Date Range](#supported-date-range)
3. [Requirements](#requirements)
4. [Building the JAR](#building-the-jar)
5. [Adding to Your Project](#adding-to-your-project)
   - [Migrating from v2.0.0](#migrating-from-v200)
   - [Migrating from v3.0.0](#migrating-from-v300)
6. [How to Use](#how-to-use)
7. [API Reference](#api-reference)
8. [Error Handling](#error-handling)
9. [Updating Calendar Data](#updating-calendar-data)
10. [Where It Can and Cannot Be Used](#where-it-can-and-cannot-be-used)
11. [Performance](#performance)
12. [License](#license)
13. [Version History](#version-history)

---

## What is this?

Nepal uses the **Bikram Sambat (BS)** calendar, which runs roughly 56–57 years ahead of the Gregorian (AD) calendar. For example, today's English date **2026-05-22** is **2083/02/08** in the Nepali calendar.

**adbs** solves the problem of converting between these two calendar systems. You give it an English date and it gives you the Nepali date, or vice versa. It is designed to be dropped into any Java or Spring project as a dependency — no server, no database, no network call needed. All the calendar data is bundled inside the JAR itself.

This library began as the second version of the original `ADBS` project and was a drop-in replacement for it. **v3.0.0 and v4.0.0 are both breaking releases** — see [Migrating from v2.0.0](#migrating-from-v200) and [Migrating from v3.0.0](#migrating-from-v300) below before upgrading.

---

## Supported Date Range

| Calendar | From | To |
|---|---|---|
| Bikram Sambat (BS) | 1970 Baisakh 1 | 2100 Chaitra 30 |
| Gregorian (AD) | April 13, 1913 | April 12, 2044 |

Dates outside this range will throw a `DateRangeNotSupported` exception with a clear message telling you the valid range.

---

## Requirements

- **Java 17** or higher
- **Maven 3.6+** (to build the JAR)
- Your consuming project can be any Java/Spring project — Spring Boot 2.x or 3.x both work

---

## Building the JAR

Clone this repository. Then, from the repo root (the parent `pom.xml` builds both modules):

```bash
mvn clean install
```

This will:
1. Compile both modules
2. Run all tests, JaCoCo coverage, and SpotBugs static analysis (`mvn verify`, which `install` includes)
3. Package `adbs-core/target/adbs-core-4.0.0.jar` and `adbs-spring/target/adbs-spring-4.0.0.jar`
4. Install both JARs into your local Maven repository (`~/.m2`) so other projects on the same machine can use them

**If you only want the JAR files without installing to local repo:**
```bash
mvn clean package
```

**If you only need `adbs-core`** (no Spring integration), you can build just that module:
```bash
mvn -pl adbs-core -am clean install
```

---

## Adding to Your Project

This is now a multi-module repository, so JitPack publishes **one coordinate per module** rather than a single repo-wide coordinate. Pick the module(s) you actually need:

- **`adbs-core`** — the converter itself. No Spring dependency. Use this unless you specifically need the Spring integration below.
- **`adbs-spring`** — adds Spring bean wiring (`NdcConfiguration`) and a `@RestControllerAdvice` global exception handler for the three conversion exceptions. Depends on `adbs-core` transitively; requires Spring on your own classpath (`provided` scope — bring your own version).

### Maven (via JitPack — recommended)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<!-- Core converter only -->
<dependency>
    <groupId>com.github.dhiraj-var.adbs</groupId>
    <artifactId>adbs-core</artifactId>
    <version>v4.0.0</version>
</dependency>

<!-- Add this too if you want the Spring bean wiring / exception handler -->
<dependency>
    <groupId>com.github.dhiraj-var.adbs</groupId>
    <artifactId>adbs-spring</artifactId>
    <version>v4.0.0</version>
</dependency>
```

Note the groupId has an extra segment (`com.github.dhiraj-var.adbs`, not `com.github.dhiraj-var`) — this is how JitPack names per-module coordinates for a multi-module repo, derived from the GitHub org + repo name, not from this project's own internal Maven coordinates (`com.nepalidate:adbs-core` / `com.nepalidate:adbs-spring`). The Java package names inside the jars are unaffected.

To release a new version, push a new git tag (e.g. `v4.0.1`) — JitPack builds it automatically on first request (usually 30–90 seconds for a cold build, then cached).

### Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.dhiraj-var.adbs:adbs-core:v4.0.0'
    implementation 'com.github.dhiraj-var.adbs:adbs-spring:v4.0.0' // optional
}
```

### Building locally (offline, or no JitPack access)

If you can't reach `jitpack.io` (e.g. an isolated network), you can still build and install the jars yourself, using their internal coordinates:

```bash
mvn clean install
```

This installs `com.nepalidate:adbs-core:4.0.0` and `com.nepalidate:adbs-spring:4.0.0` into your local `~/.m2` — add them to your `pom.xml` the same way as above, but with the internal `groupId`/`artifactId`/`version` instead of the JitPack coordinates.

To use a locally-built jar on a different machine, copy `adbs-core/target/adbs-core-4.0.0.jar` over and run:

```bash
mvn install:install-file \
  -Dfile=adbs-core-4.0.0.jar \
  -DgroupId=com.nepalidate \
  -DartifactId=adbs-core \
  -Dversion=4.0.0 \
  -Dpackaging=jar
```

### Migrating from v2.0.0

v3.0.0 is a breaking release. Before upgrading:

1. **`NDC` is no longer a Spring bean by default.** Component-scan-based `@Autowired NDC` will fail at Spring context startup, silently (no compile error) until you fix it. Add the `adbs-spring` dependency and either `@Import(com.nepalidate.ADBS.spring.NdcConfiguration.class)` on a configuration class, or add `com.nepalidate.ADBS.spring` to your component-scan base packages.
2. **`ExceptionHandling` / `ErrResponse` moved to `adbs-spring`.** If you use these classes (or rely on the global exception handler being auto-registered via component scanning), add the `adbs-spring` dependency.
3. **Watch the old-style JitPack coordinate.** `com.github.dhiraj-var:adbs:v3.0.0` (without the module name) still resolves — JitPack auto-generates an aggregator for multi-module repos — but it depends on *all* submodules, so it silently pulls Spring back in even if you only wanted `adbs-core`. Use the per-module coordinates above instead.
4. If you call `AdBs.convertAdToBs(String)` directly and have `catch (java.text.ParseException e)` around it, remove that catch block — the method no longer declares that checked exception (it never actually threw it).

See [CHANGELOG.md](CHANGELOG.md) for the complete list of changes.

### Migrating from v3.0.0

v4.0.0 is also a breaking release — it replaces `adbs-spring`'s bespoke error response with an [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) "Problem Details" shape. Before upgrading:

1. **`ErrResponse` is gone, replaced by `ProblemDetail`** (same package, `com.nepalidate.ADBS.ExceptionHandling`). Field mapping:

   | Old (`ErrResponse`) | New (`ProblemDetail`) | Notes |
   |---|---|---|
   | `statusCode` | `status` | Same meaning |
   | `timestamp` (`java.util.Date`) | `timestamp` (`java.time.Instant`) | Type changed |
   | `message` | `detail` | Same content |
   | `description` | `instance` | The `uri=` prefix is now stripped |
   | *(none)* | `type` | New — a `urn:adbs:problem:...` identifier per error type |
   | *(none)* | `errorCode` | New — a stable machine-readable code, also available without Spring via `getErrorCode()` on the exception itself |

2. **Response media type changed** from `application/json` to `application/problem+json`. If you have a hardcoded `Accept`/content-type check on the client side, update it.
3. **Status codes changed for two exceptions**: `DateRangeNotSupported` and `InvalidBsDayOfMonthException` now return `422 Unprocessable Entity` instead of `400 Bad Request` (they represent well-formed input with a semantically invalid value, not a malformed request — see the [Response shape](#response-shape-rfc-7807) table). Update any client logic branching on the exact status code for these two.
4. **`CalendarDataUnavailableException` is now actually handled.** Previously it silently fell through to Spring's default whitelabel/blank error response (a bug); it now returns a structured `500 Internal Server Error` `ProblemDetail` body like everything else.

See [CHANGELOG.md](CHANGELOG.md) for the complete list of changes.

---

## How to Use

The main class you will use is `NDC` (Nepali Date Converter). It has no Spring dependency, so plain Java usage requires nothing extra. For Spring, add the `adbs-spring` dependency and import its configuration to get `NDC` as a bean.

### In a Spring project (with the `adbs-spring` dependency added)

```java
import com.nepalidate.ADBS.spring.NdcConfiguration;
import org.springframework.context.annotation.Import;

@Import(NdcConfiguration.class)
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

```java
import com.nepalidate.ADBS.NepaliDateConverter.NDC;

@Service
public class MyService {

    @Autowired
    private NDC ndc;

    public void example() {
        // English to Nepali
        String nepaliDate = ndc.adToBs("2026-05-22");
        System.out.println(nepaliDate); // "2083/02/08"

        // Nepali to English
        String englishDate = ndc.bsToAd("2083/02/08");
        System.out.println(englishDate); // "2026-05-22"

        // Validate a Nepali date (useful for form validation)
        boolean valid = ndc.validateDate_bs("2083/02/08", "dateOfBirth");
        System.out.println(valid); // true
    }
}
```

Alternatively, component-scan `com.nepalidate.ADBS.spring` instead of using `@Import`, if you prefer scanning over explicit imports.

### Without Spring (plain Java)

```java
import com.nepalidate.ADBS.NepaliDateConverter.NDC;

NDC ndc = new NDC();

String nepaliDate = ndc.adToBs("2026-05-22");  // "2083/02/08"
String englishDate = ndc.bsToAd("2083/02/08"); // "2026-05-22"
```

---

## API Reference

All methods are on the `NDC` class located at:
```
com.nepalidate.ADBS.NepaliDateConverter.NDC
```

---

### `adToBs(String adDate)`

Converts an English (Gregorian) date to a Nepali (Bikram Sambat) date.

| | |
|---|---|
| **Input format** | `YYYY-MM-DD` |
| **Input example** | `"2026-05-22"` |
| **Output format** | `YYYY/MM/DD` |
| **Output example** | `"2083/02/08"` |

```java
ndc.adToBs("2026-05-22")  // returns "2083/02/08"
ndc.adToBs("2024-04-13")  // returns "2081/01/01"  (Nepali New Year 2081)
ndc.adToBs("1913-04-13")  // returns "1970/01/01"  (earliest supported date)
```

**Throws:**
- `InvalidDateFormatException` — if the format is wrong or the date values are invalid (e.g. month 13)
- `DateRangeNotSupported` — if the date is outside the supported range

---

### `bsToAd(String bsDate)`

Converts a Nepali (Bikram Sambat) date to an English (Gregorian) date.

| | |
|---|---|
| **Input format** | `YYYY/MM/DD` |
| **Input example** | `"2083/02/08"` |
| **Output format** | `YYYY-MM-DD` |
| **Output example** | `"2026-05-22"` |

```java
ndc.bsToAd("2083/02/08")  // returns "2026-05-22"
ndc.bsToAd("2081/01/01")  // returns "2024-04-13"  (Nepali New Year 2081)
ndc.bsToAd("1970/01/01")  // returns "1913-04-13"  (earliest supported date)
```

**Throws:**
- `InvalidDateFormatException` — if the format is wrong
- `DateRangeNotSupported` — if the month is invalid or the year is out of range
- `InvalidBsDayOfMonthException` — if the day exceeds what that month actually has

---

### `validateDate_bs(String bsDate, String field_name)`

Validates a Nepali date. If the date is valid, returns `true`. If invalid, throws an exception with a message that includes the `field_name` label — this is useful when validating form fields so the error message tells the user which field has the problem.

| | |
|---|---|
| **Input format** | `YYYY/MM/DD` |
| **field_name** | Any label you choose (e.g. `"dateOfBirth"`, `"startDate"`) |
| **Returns** | `true` if valid |

```java
ndc.validateDate_bs("2083/02/08", "dateOfBirth")  // returns true

// Invalid examples — these throw exceptions:
ndc.validateDate_bs("2083/13/01", "dateOfBirth")  // month 13 does not exist
ndc.validateDate_bs("2083/01/35", "startDate")    // day 35 does not exist in that month
ndc.validateDate_bs("2101/01/01", "endDate")      // year 2101 is beyond supported range
```

**Throws:**
- `InvalidDateFormatException` — if the format is wrong
- `DateRangeNotSupported` — if the year or month is out of range
- `InvalidBsDayOfMonthException` — if the day is invalid for that month

---

## Error Handling

All exceptions extend `RuntimeException`, so you do not need to declare them in method signatures. They will bubble up to your global exception handler automatically.

The four exception types are:

| Exception | When it is thrown |
|---|---|
| `InvalidDateFormatException` | The date string is in the wrong format or contains non-numeric characters |
| `DateRangeNotSupported` | The year or month is outside the valid range |
| `InvalidBsDayOfMonthException` | The day number is higher than the days in that particular month |
| `CalendarDataUnavailableException` | The bundled calendar data itself couldn't be loaded or is internally inconsistent — a packaging/data problem, not a bad input; you should not normally see this unless the CSV was hand-edited incorrectly |

All are in the package `com.nepalidate.ADBS.NepaliDateConverter`.

### Catching exceptions individually

```java
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;

try {
    String bs = ndc.adToBs(userInput);
} catch (InvalidDateFormatException e) {
    // show e.getMessage() to the user — it is written in plain language
} catch (DateRangeNotSupported e) {
    // date is valid format but outside the supported range
} catch (InvalidBsDayOfMonthException e) {
    // day number is too high for that specific month
}
```

### With Spring's global exception handler (`adbs-spring`)

If you've added the `adbs-spring` dependency, it includes a `@RestControllerAdvice` class (`ExceptionHandling`) that automatically catches all four of the library's exceptions — plus the JDK's `java.time.DateTimeException`, as a defensive case — and returns an [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) `application/problem+json` response, as long as it's on your component-scan path. If your project already has its own global exception handler, the library's handler will not interfere — Spring uses the most specific handler available.

#### Response shape (RFC 7807)

The response body follows the RFC 7807 "Problem Details for HTTP APIs" standard, plus one extension member (`errorCode`) for callers who want to branch on a stable machine-readable code instead of parsing `detail` text:

```json
{
  "type": "urn:adbs:problem:invalid-bs-day-of-month",
  "title": "Invalid Bikram Sambat Day Of Month",
  "status": 422,
  "detail": "Day 32 is not valid for month 1 of Nepali year 2083. This month only has 31 days.",
  "instance": "/api/dates/convert",
  "errorCode": "INVALID_BS_DAY_OF_MONTH",
  "timestamp": "2026-09-02T10:15:30.123Z"
}
```

| Exception | HTTP status | `errorCode` | `type` |
|---|---|---|---|
| `InvalidDateFormatException` | 400 Bad Request | `INVALID_DATE_FORMAT` | `urn:adbs:problem:invalid-date-format` |
| `DateRangeNotSupported` | 422 Unprocessable Entity | `DATE_RANGE_NOT_SUPPORTED` | `urn:adbs:problem:date-range-not-supported` |
| `InvalidBsDayOfMonthException` | 422 Unprocessable Entity | `INVALID_BS_DAY_OF_MONTH` | `urn:adbs:problem:invalid-bs-day-of-month` |
| `java.time.DateTimeException` | 400 Bad Request | `INVALID_DATE_FORMAT` | `urn:adbs:problem:invalid-date-format` |
| `CalendarDataUnavailableException` | 500 Internal Server Error | `CALENDAR_DATA_UNAVAILABLE` | `urn:adbs:problem:calendar-data-unavailable` |

400 is used for input that couldn't even be parsed into a candidate date (wrong format); 422 is used for input that parsed fine but is semantically out of range (unsupported year/month, or a day that doesn't exist in that month); 500 is used for `CalendarDataUnavailableException` specifically, since that's a server-side data defect, not a mistake in the request — retrying the same request won't help until the underlying data is fixed.

`errorCode` is also available without Spring: all four of the library's own exception classes expose a `getErrorCode()` method directly, so plain-Java (`adbs-core`-only) consumers get the same stable code without needing `adbs-spring`.

`timestamp` is a `java.time.Instant`, serialized as an ISO-8601 string. This requires Jackson's `jackson-datatype-jsr310` module to be registered, and `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS` to be disabled — Spring Boot's web starter configures both automatically; a plain Spring Framework app without Boot needs to configure both itself, or `timestamp` will serialize as a numeric epoch value instead.

### Example error messages

All messages are written in plain English and tell the user exactly what is wrong and what to provide instead:

```
Invalid English date: '22/05/2026'. Please use the format YYYY-MM-DD (example: 2026-04-14).

Month 13 is not valid. Nepali calendar months are numbered 1 to 12.

Day 32 is not valid for month 1 of Nepali year 2083. This month only has 31 days.

Nepali year 2101 is beyond the supported range. Supported years are 1970 to 2100.

English date 1900-01-01 is before the supported range. Dates from April 13, 1913 onwards are supported.
```

---

## Updating Calendar Data

All calendar data lives in one file:

```
adbs-core/src/main/resources/nepali_dates.csv
```

Each line represents one Nepali year:

```
# bs_year, new_year_ad_date (yyyy-MM-dd), days in months 1 through 12
2083,2026-04-14,31,31,32,31,31,30,30,29,30,29,30,30
2084,2027-04-14,31,31,32,31,31,30,30,30,29,30,30,30
```

- **Column 1** — Nepali (BS) year
- **Column 2** — The English date on which Baisakh 1 (Nepali New Year) falls
- **Columns 3–14** — Number of days in each of the 12 Nepali months (Baisakh through Chaitra)

### To add a new year

Append a new line at the bottom of the CSV with the correct data, then rebuild:

```bash
mvn clean install
```

### To fix a month's day count

Find the line for that year, update the number in the relevant column, then rebuild. No Java code needs to change.

### Notes on the CSV

- Lines starting with `#` are comments and are ignored
- Blank lines are ignored
- Line order does not matter — the library indexes data by year number, not by position in the file
- All years from 1970 to 2100 must be present
- The typical number of days per Nepali month is 29–32
- Each year's 12 month-day-counts must sum to exactly the number of days between that year's new-year date and the next year's new-year date — this is validated at load time
- Any of these problems throws `CalendarDataUnavailableException` with a message naming the specific bad row, on first use of the library (not at class-load time — a bad CSV no longer poisons the JVM permanently, so fixing it and re-running works without a restart)
- **Known data caveat:** for BS 1972 and BS 1974, the correct yearly total is well-corroborated across independent sources, but the exact per-month breakdown isn't — the current file places the extra day in Chaitra (month 12) as a best-effort, unverified choice. See `CHANGELOG.md`'s v3.0.0 entry for details if you have an authoritative source for these two years.

---

## Where It Can and Cannot Be Used

### ✅ Where it can be used

- **Any Java 17+ project** — Spring Boot, plain Java, Jakarta EE, Micronaut, Quarkus, etc. — via `adbs-core` alone
- **Spring Boot 2.x and 3.x** — via the additional `adbs-spring` module (`NdcConfiguration`, `@RestControllerAdvice`); compatible with both versions
- **Backend services and APIs** — for storing, displaying, or accepting Nepali dates
- **Form validation** — use `validateDate_bs()` to validate user-submitted Nepali dates with a clear field-level error message
- **Batch processing** — converting large numbers of dates is fast; no external calls are made
- **Libraries and SDKs** — `adbs-core` has zero mandatory runtime dependencies (not even Spring, optionally or otherwise), so it can safely be embedded in other libraries without forcing anything onto downstream consumers

### ❌ Where it cannot be used

- **Frontend / JavaScript / TypeScript** — this is a Java library only; it cannot run in a browser or Node.js
- **Android** — not tested or configured for Android; Android uses a different Java runtime (ART)
- **Java versions below 17** — the library uses Java 17 language features and APIs
- **Dates before BS 1970 (April 13, 1913 AD)** — no calendar data exists for earlier years; the library will throw an exception
- **Dates after BS 2100 (April 12, 2044 AD)** — same reason; extend by adding rows to `nepali_dates.csv`
- **As a standalone REST API** — the library has no built-in HTTP server or endpoints; it is a dependency to be embedded in your own project. (If you need a standalone API, wrap it in a Spring Boot `@RestController` in your own project)
- **Non-Maven/Gradle projects** — there is no published package on Maven Central; you must build and install the JAR locally as described above

---

## Performance

The library is optimised for high-throughput use:

- **No external calls** — all data is loaded from the bundled CSV at startup; conversions are pure in-memory computation
- **O(1) year lookup** — year data is stored in a plain array indexed by `bsYear - 1970`; no HashMap lookups or linear searches
- **O(1–2) AD→BS year estimation** — uses the known offset (~57 years between calendars) to jump directly to the right year instead of scanning all 131 years
- **Thread-safe** — `Pattern` and `DateTimeFormatter` instances are `static final`; the `NDC` bean can safely be shared across threads in a Spring singleton (verified under concurrent load by `ThreadSafetyTest`)
- **No object allocation on hot path** — no `new Date()`, no `SimpleDateFormat`, no `Calendar` created during conversion

The JAR itself is **14 KB** with no mandatory transitive dependencies.

---

## License

This project is released under the **MIT License** — you are free to use, copy, modify, merge, publish, distribute, sublicense, and sell it, in personal or commercial projects, with no restrictions.

The only requirement is that the original copyright notice is kept in any copy or substantial portion of the software.

See the [LICENSE](LICENSE) file for the full license text.

---

## Version History

See [CHANGELOG.md](CHANGELOG.md) for the full version history.
- Supported BS 1970–2100
