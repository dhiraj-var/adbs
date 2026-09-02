# Releasing

This project ships via [JitPack](https://jitpack.io), which builds directly from a pushed git tag — there is no separate publish step to a package registry.

## Steps

1. **Update the changelog.** Move everything under `## [Unreleased]` in `CHANGELOG.md` into a new dated section (`## [X.Y.Z] - YYYY-MM-DD`), following [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) / [SemVer](https://semver.org/) conventions.

2. **Bump the version.** From the repo root:
   ```bash
   mvn versions:set -DnewVersion=X.Y.Z -DprocessAllModules=true
   mvn versions:commit
   ```
   This updates the parent pom's `<version>` (both submodules inherit it — there's only one version to bump).

3. **Commit.**
   ```bash
   git add pom.xml adbs-core/pom.xml adbs-spring/pom.xml CHANGELOG.md
   git commit -m "Release X.Y.Z"
   ```

4. **Tag and push.**
   ```bash
   git tag vX.Y.Z
   git push origin main --tags
   ```

5. **Confirm `release-check.yml` goes green** on the tag push — it fails the build if the tag version doesn't match the pom version, catching drift before anyone tries to depend on the tag.

6. **Force the JitPack build and confirm it succeeds.** Visit:
   ```
   https://jitpack.io/#dhiraj-var/adbs/vX.Y.Z
   ```
   This triggers a cold build if one hasn't run yet (usually 30–90 seconds). Confirm both `adbs-core` and `adbs-spring` build logs succeed — check both, since a multi-module JitPack build can partially succeed.

7. **Spot-check resolution** from a scratch consumer project:
   ```xml
   <dependency>
       <groupId>com.github.dhiraj-var.adbs</groupId>
       <artifactId>adbs-core</artifactId>
       <version>vX.Y.Z</version>
   </dependency>
   ```

## Version numbering

Follows SemVer:
- **Patch** (`X.Y.Z+1`) — bug fixes, calendar-data corrections, no API changes.
- **Minor** (`X.Y+1.0`) — new functionality, backward compatible.
- **Major** (`X+1.0.0`) — breaking changes (see `CHANGELOG.md`'s "Changed (breaking)" sections for what counts as breaking in this project, e.g. moving a public class to a different module, or a JitPack coordinate change).
