# adbs-core

A fast, lightweight Java library for converting dates between the **English (Gregorian / AD)** calendar and the **Nepali (Bikram Sambat / BS)** calendar.

---

## Table of Contents

1. [What is this?](#what-is-this)
2. [Supported Date Range](#supported-date-range)
3. [Requirements](#requirements)
4. [Building the JAR](#building-the-jar)
5. [Adding to Your Project](#adding-to-your-project)
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

**adbs-core** solves the problem of converting between these two calendar systems. You give it an English date and it gives you the Nepali date, or vice versa. It is designed to be dropped into any Java or Spring project as a dependency — no server, no database, no network call needed. All the calendar data is bundled inside the JAR itself.

This library is the second version of the original `ADBS` project. It is a **drop-in replacement** — if you already use the original, you only need to swap the dependency. No code changes are required in your project.

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

Clone or copy the `adbs-core` folder to your machine. Then run:

```bash
cd adbs-core
mvn clean install
```

This will:
1. Compile the source code
2. Run all tests
3. Package the JAR at `target/adbs-core-2.0.0.jar`
4. Install the JAR into your local Maven repository (`~/.m2`) so other projects on the same machine can use it

**If you only want the JAR file without installing to local repo:**
```bash
mvn clean package
# JAR is at: target/adbs-core-2.0.0.jar
```

---

## Adding to Your Project

### Maven

After running `mvn install`, add this to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.nepalidate</groupId>
    <artifactId>adbs-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.nepalidate:adbs-core:2.0.0'
```

### Using on a Different Machine (No Maven Repository)

If you want to use the JAR on a machine that did not build it, copy the `adbs-core-2.0.0.jar` file to that machine and run:

```bash
mvn install:install-file \
  -Dfile=adbs-core-2.0.0.jar \
  -DgroupId=com.nepalidate \
  -DartifactId=adbs-core \
  -Dversion=2.0.0 \
  -Dpackaging=jar
```

After that, the `pom.xml` dependency above will work normally.

---

## How to Use

The main class you will use is `NDC` (Nepali Date Converter). If your project uses Spring, it is registered as a `@Component` and can be injected anywhere.

### In a Spring project (recommended)

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

Make sure Spring can find the `NDC` class. If your application does not already scan the `com.nepalidate` package, add it to your Spring Boot application class:

```java
@SpringBootApplication(scanBasePackages = {"com.yourpackage", "com.nepalidate"})
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

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

The three exception types are:

| Exception | When it is thrown |
|---|---|
| `InvalidDateFormatException` | The date string is in the wrong format or contains non-numeric characters |
| `DateRangeNotSupported` | The year or month is outside the valid range |
| `InvalidBsDayOfMonthException` | The day number is higher than the days in that particular month |

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

### With Spring's global exception handler

The library includes a `@RestControllerAdvice` class (`ExceptionHandling`) that automatically catches all three exceptions and returns a JSON `400 Bad Request` response. If your project already has its own global exception handler, the library's handler will not interfere — Spring uses the most specific handler available.

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
src/main/resources/nepali_dates.csv
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
- All years from 1970 to 2100 must be present; the library will refuse to start if any year is missing
- The typical number of days per Nepali month is 29–32

---

## Where It Can and Cannot Be Used

### ✅ Where it can be used

- **Any Java 17+ project** — Spring Boot, plain Java, Jakarta EE, Micronaut, Quarkus, etc.
- **Spring Boot 2.x and 3.x** — the library's Spring annotations (`@Component`, `@RestControllerAdvice`) are compatible with both versions
- **Backend services and APIs** — for storing, displaying, or accepting Nepali dates
- **Form validation** — use `validateDate_bs()` to validate user-submitted Nepali dates with a clear field-level error message
- **Batch processing** — converting large numbers of dates is fast; no external calls are made
- **Libraries and SDKs** — because the JAR has no mandatory runtime dependencies (Spring is optional), it can safely be embedded in other libraries without forcing Spring on downstream consumers

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
- **Thread-safe** — `Pattern` and `DateTimeFormatter` instances are `static final`; the `NDC` bean can safely be shared across threads in a Spring singleton
- **No object allocation on hot path** — no `new Date()`, no `SimpleDateFormat`, no `Calendar` created during conversion

The JAR itself is **14 KB** with no mandatory transitive dependencies.

---

## License

This project is released under the **MIT License** — you are free to use, copy, modify, merge, publish, distribute, sublicense, and sell it, in personal or commercial projects, with no restrictions.

The only requirement is that the original copyright notice is kept in any copy or substantial portion of the software.

See the [LICENSE](LICENSE) file for the full license text.

---

## Version History

### 2.0.0 (current)
- Complete rewrite for performance: O(1) data access, +57-estimation algorithm for AD→BS
- Calendar data moved from Java source code to `nepali_dates.csv` — easy to update without code changes
- All error messages rewritten in plain language with examples and valid ranges
- Removed Spring Boot dependency; JAR is now 14 KB instead of 17 MB
- Java 17
- Backward compatible with the 1.0.0 API — same class names, same method signatures, same packages

### 1.0.0
- Initial release (Spring Boot 2.6.3, Java 11)
- Supported BS 1970–2100
