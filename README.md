# Humming-UI

[![Maven Central](https://img.shields.io/maven-central/v/me.developes.humming.sdui/humming-core?label=maven%20central)](https://central.sonatype.com/search?namespace=me.developes.humming.sdui)
[![Release](https://img.shields.io/github/v/release/vinisauter/Humming-UI?label=release)](https://github.com/vinisauter/Humming-UI/releases)
[![License](https://img.shields.io/github/license/vinisauter/Humming-UI)](LICENSE)

Kotlin Multiplatform UI library with navigation/actions/material layout helpers and a prebuilt Apple XCFramework for Swift integration.

> Learn more about the concepts and motivations of Humming-UI in the article [Server Driven UI: Flexibility and performance with declarative UI](https://medium.com/@vinicius.sauter/server-driven-ui-flexibilidade-e-performance-com-ui-declarativa-a556496acf92), published on Medium. The text explores how the server-driven model and the declarative approach provide flexibility and performance in building cross-platform interfaces, highlighting the integration between Kotlin Multiplatform and Swift. Reading it is recommended to understand the fundamentals and benefits of the library.

## Modules
| Module (Gradle) | Swift Product (SPM) | Description |
|-----------------|---------------------|-------------|
| humming-core | HummingCore | Core primitives, shared UI and utilities. |
| humming-actions | HummingActions | Action abstractions / intent handling helpers. |
| humming-navigation | HummingNavigation | Navigation helpers / route infrastructure. |
| humming-layout-material3 | HummingLayoutMaterial3 | Material 3 layout & design components. |

## Kotlin (Gradle) Usage
Add to your `settings.gradle.kts` repositories (already present):
```kotlin
mavenCentral()
```

Add only what you need (replace VERSION with the latest release, e.g. 1.0.0):
```kotlin
dependencies {
    implementation("me.developes.humming.sdui:humming-core:VERSION")
    implementation("me.developes.humming.sdui:humming-actions:VERSION") // optional
    implementation("me.developes.humming.sdui:humming-navigation:VERSION") // optional
    implementation("me.developes.humming.sdui:humming-layout-material3:VERSION") // optional
}
```
Or selectively:
```kotlin
dependencies {
    implementation("me.developes.humming.sdui:humming-core:VERSION")
    implementation("me.developes.humming.sdui:humming-navigation:VERSION")
}
```

## Swift Package Manager
After a release is published, `Package.swift` in `main` is updated automatically with concrete URLs & checksums.

Add the package to Xcode:
1. File > Add Package > `https://github.com/vinisauter/Humming-UI.git`
2. Choose the release tag (e.g. `1.0.0`).
3. Select one or more products: `HummingCore`, `HummingActions`, `HummingNavigation`, `HummingLayoutMaterial3`.

Import what you need in Swift:
```swift
import HummingCore
import HummingNavigation // if using navigation
```

### Manual `Package.swift` Dependency (for other SwiftPM projects)
```swift
.package(url: "https://github.com/vinisauter/Humming-UI.git", from: "1.0.0")
```
Then declare targets as needed:
```swift
.target(name: "MyApp", dependencies: [
    .product(name: "HummingCore", package: "Humming-UI"),
    .product(name: "HummingActions", package: "Humming-UI"),
])
```

## Releasing
There are now TWO supported ways to publish a release using the same workflow (`Release & Publish`):

A. Tag-driven (recommended for CI traceability) – push an annotated tag `vX.Y.Z`.
B. Manual dispatch – run the workflow from the GitHub Actions UI and supply the version (no leading `v`). The workflow will verify `hummingVersion` in `gradle.properties` matches and then create the tag implicitly when creating the GitHub Release.

> In both cases the version number (without the `v`) must match `hummingVersion` in `gradle.properties` or the workflow fails early.

### Tag-driven flow (Option A)
1. Choose version (e.g. `1.0.1`).
2. Update `gradle.properties` (`hummingVersion=1.0.1`) and commit.
3. Create & push tag:
   ```bash
   git tag -a v1.0.1 -m "Release 1.0.1"
   git push origin v1.0.1
   ```
4. Workflow runs automatically.

### Manual dispatch flow (Option B)
1. Choose version (e.g. `1.0.1`).
2. Update and commit `gradle.properties` (`hummingVersion=1.0.1`) on the branch you want to release.
3. Open GitHub > Actions > Release & Publish > Run workflow.
4. Enter `1.0.1` in the version input and run.
5. The workflow builds, publishes, updates `Package.swift` and creates release + tag `v1.0.1` pointing to the triggering commit.

### What the workflow does (both flows)
1. Publishes all Kotlin artifacts to Maven Central via `./gradlew publish`.
2. Runs `./gradlew packageAllXCFrameworks` producing every module zip + `checksums.txt`.
3. Replaces placeholders in `Package.swift` (`${VERSION}`, `${HUMMING_*_CHECKSUM}`).
4. Commits `Package.swift` if changed.
5. Creates (or uses existing) GitHub tag + Release with zipped XCFramework assets.

### Verify the release
After the workflow succeeds:
- GitHub Releases page: tag `v1.0.1` exists with all XCFramework zip assets.
- `Package.swift` on `main` shows concrete URLs & checksum strings (no leftover `${...}` placeholders).
- SwiftPM can resolve the package at version `1.0.1` in Xcode.
- Maven Central listing (may take several minutes to appear / index). You can check staging/logs at Sonatype if needed.

### Consumer upgrade examples
Gradle:
```kotlin
implementation("me.developes.humming.sdui:humming-core:1.0.1")
```
SwiftPM (Package.swift snippet):
```swift
.package(url: "https://github.com/vinisauter/Humming-UI.git", exact: "1.0.1")
```

### Troubleshooting
| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| Workflow not triggered | Tag name missing leading `v` | Recreate tag as `vX.Y.Z` and push |
| Maven publish failed | Missing or wrong credentials / GPG key | Re‑upload secrets, retry by deleting & re‑pushing tag (or push a new tag) |
| Package.swift still has placeholders | Check `checksums.txt` format or workflow logs in update step | Ensure lines are `Name.zip=<sha256>`; re-run with corrected build |
| SwiftPM checksum mismatch | Corrupted or changed zip after checksum replacement | Delete release, regenerate tag (or bump patch) |
| Artifacts absent in Release | Build job failed before release | Inspect earlier jobs' logs |

### Manually regenerating a failed release
If artifacts or checksums are wrong, it's usually safer to:
1. Delete the GitHub Release (keep the tag if you just want to re-run by retagging) OR delete the tag locally & remotely:
   ```bash
   git tag -d v1.0.1
   git push origin :refs/tags/v1.0.1
   ```
2. Apply any fixes (e.g., code, build scripts).
3. Recreate tag (same or incremented version) and push again.

## Required GitHub Secrets
These names must match what the workflow references (environment variables passed to Gradle):

| Secret | Purpose |
|--------|---------|
| MAVEN_CENTRAL_USERNAME | Sonatype (OSSRH) username |
| MAVEN_CENTRAL_PASSWORD | Sonatype (OSSRH) password |
| MAVEN_GPG_PRIVATE_KEY | ASCII-armored private GPG key used for signing (full block) |
| MAVEN_GPG_PASSPHRASE | Passphrase for the GPG private key |

(If your existing local Gradle config expects other property names, map them inside the workflow or adjust the README.)

## Local XCFramework Build
Prerequisites:
- macOS host (for Kotlin/Native Apple targets)
- Xcode Command Line Tools (`xcode-select -p` should succeed)
- JDK 17 available (Gradle will use the configured toolchain/`JAVA_HOME`)

The assembled XCFramework is placed under:
```
humming-core/build/XCFrameworks/release/HummingCore.xcframework
```
The packaging task zips it to:
```
humming-core/build/dist/HummingCore.xcframework.zip
```

Recommended full build sequence:
```bash
# Optional clean if you suspect stale artifacts
./gradlew :humming-core:clean

# Assemble the release XCFramework (iOS x64 + arm64 + simulator arm64)
./gradlew :humming-core:assembleHummingUICoreReleaseXCFramework --rerun-tasks --info

# Package it (will fail fast if the directory is missing/empty)
./gradlew :humming-core:packageHummingUICoreXCFramework --rerun-tasks --info
```

Compute checksum for SwiftPM (if doing manual update):
```bash
swift package compute-checksum humming-core/build/dist/HummingCore.xcframework.zip
```

You can build & package individual modules (example for core + navigation):
```bash
./gradlew :humming-core:packageXCFramework :humming-navigation:packageXCFramework
```
All modules & aggregated checksum file:
```bash
./gradlew packageAllXCFrameworks
```

Resulting zips (one per module) are collected into `build/xcframeworks/` and checksums written to `build/xcframeworks/checksums.txt`:
```
HummingCore.xcframework.zip=<sha256>
HummingActions.xcframework.zip=<sha256>
HummingNavigation.xcframework.zip=<sha256>
HummingLayoutMaterial3.xcframework.zip=<sha256>
```

(See the section below for the full original build instructions.)

## License
MIT
