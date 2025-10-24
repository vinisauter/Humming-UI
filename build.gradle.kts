import android.databinding.tool.ext.toCamelCase
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.sqlDelight).apply(false)
    alias(libs.plugins.buildConfig).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.spring.boot).apply(false)
    alias(libs.plugins.spring.dependency.management).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.spring).apply(false)
}

subprojects {
    // Configure XCFramework packaging only when KMP plugin applied (no afterEvaluate for config cache friendliness)
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        val xcFrameworkName = project.name.replace('-', '_').toCamelCase()
        val xcFrameworkDir = layout.buildDirectory.dir("XCFrameworks/release/$xcFrameworkName.xcframework")
        val distDir = layout.buildDirectory.dir("dist")
        // Precompute values needed at execution time to avoid accessing Task.project during task actions (configuration cache requirement)
        val projectPath = project.path
        val rootDirFile = rootDir
        tasks.register<Zip>("packageXCFramework") {
            group = "distribution"
            description = "Zip $xcFrameworkName Release XCFramework"
            dependsOn("assemble${xcFrameworkName}ReleaseXCFramework")
            val xcDirProvider = xcFrameworkDir
            inputs.dir(xcDirProvider)
            from(xcDirProvider.map { it.asFileTree })
            includeEmptyDirs = true
            destinationDirectory.set(distDir)
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            archiveFileName.set("$xcFrameworkName.xcframework.zip")
            // Declare output for incremental build & configuration cache metadata
            outputs.file(archiveFile)
            // Avoid referencing 'project' directly inside execution time actions for configuration cache compatibility
            doFirst {
                val xcDir = xcDirProvider.get().asFile
                if (!xcDir.exists()) throw GradleException("XCFramework directory not found: $xcDir. Run :$projectPath:assemble${xcFrameworkName}ReleaseXCFramework first.")
                if (xcDir.listFiles()?.isEmpty() == true) throw GradleException("XCFramework directory is empty: $xcDir. Ensure frameworks were built.")
                logger.lifecycle("[packageXCFramework] Preparing to zip XCFramework for $projectPath. Source dir: ${xcDir.absolutePath}")
            }
            doLast {
                val archive = archiveFile.get().asFile
                if (archive.exists() && archive.length() > 0L) {
                    logger.lifecycle("[packageXCFramework] ✅ Created XCFramework zip for $projectPath: ${archive.absolutePath} (size=${archive.length()} bytes)")
                    // Compute checksum using `swift package compute-checksum` if Swift is available
                    try {
                        val process = ProcessBuilder("swift", "package", "compute-checksum", archive.absolutePath)
                            .directory(rootDirFile) // use precomputed rootDirFile instead of accessing project
                            .redirectErrorStream(true)
                            .start()
                        val output = process.inputStream.bufferedReader().readText().trim()
                        val exitCode = process.waitFor()
                        if (exitCode == 0 && output.isNotBlank()) {
                            logger.lifecycle("[packageXCFramework] 🔐 Swift checksum: $output")
                        } else {
                            logger.warn("[packageXCFramework] Could not compute Swift checksum (exitCode=$exitCode, output='$output'). Ensure Swift toolchain is installed.")
                        }
                    } catch (e: Exception) {
                        logger.warn("[packageXCFramework] Swift checksum computation failed: ${e.message}. Install Xcode/Swift if you need the checksum.")
                    }
                } else {
                    logger.error("[packageXCFramework] ❌ Zip not found or empty for $projectPath. Expected at: ${archive.absolutePath}")
                }
            }
        }
    }

    // Maven publish configuration (no afterEvaluate, use plugin hook)
    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
            val hummingVersion: String = (project.findProperty("hummingVersion") as String?) ?: "LOCAL-SNAPSHOT"
            publishToMavenCentral()
            coordinates("me.developes.humming.sdui", project.name, hummingVersion)
            pom {
                name = "HummingUI"
                description = "Kotlin Multiplatform library"
                url = "https://github.com/vinisauter/Humming-UI"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "vinisauter"
                        name = "Vinicius Sauter"
                        url = "https://github.com/vinisauter/Humming-UI"
                    }
                }
                scm {
                    url = "https://github.com/vinisauter/Humming-UI/"
                    connection = "scm:git:git://github.com/vinisauter/Humming-UI.git"
                    developerConnection = "scm:git:ssh://git@github.com/vinisauter/Humming-UI.git"
                }
            }
            if (project.hasProperty("signing.keyId")) signAllPublications()
        }
    }
}

// Unified packaging + collection task: builds each XCFramework zip then gathers them into a single root build folder.
val xcFrameworkModules = listOf(
    "humming-core",
    "humming-actions",
    "humming-navigation",
    "humming-layout-material3"
)

// Aggregate build + collect task (kept)
// Reworked for configuration cache friendliness: avoid capturing script objects / Project in task actions.
val xcFrameworkModuleDistRelativePaths = xcFrameworkModules.map { "$it/build/dist" } // plain strings only

tasks.register<Sync>("packageAllXCFrameworks") {
    group = "distribution"
    description = "Package Release XCFrameworks for all HummingUI libraries and collect zips into build/xcframeworks and print Swift checksums"
    // Ensure individual zips are created first
    dependsOn(xcFrameworkModules.map { ":$it:packageXCFramework" })

    // Use providers & primitive values outside actions
    val targetDirProvider = layout.buildDirectory.dir("xcframeworks")
    val rootDirPath = rootDir.absolutePath // capture as String (serializable) instead of File pointing to Project internals
    val checksumsFile = layout.buildDirectory.file("xcframeworks/checksums.txt")

    into(targetDirProvider)

    // Copy produced zips from each module's dist directory via plain relative path Strings (avoids capturing Directory instances referencing the script)
    xcFrameworkModuleDistRelativePaths.forEach { distPath ->
        from(distPath) { include("*.xcframework.zip") }
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // Declare inputs (the zips we will read to compute checksums) & outputs (checksums file) for incremental & config cache safety
    // We can't declare them yet because zips appear after dependsOn tasks; we approximate by declaring potential directories.
    xcFrameworkModuleDistRelativePaths.forEach { path -> inputs.dir(path) }
    outputs.file(checksumsFile)

    doLast {
        val targetDir = targetDirProvider.get().asFile
        targetDir.mkdirs()
        val collectedFiles = targetDir.listFiles()?.filter { it.extension == "zip" }?.sortedBy { it.name } ?: emptyList()
        logger.lifecycle("[packageAllXCFrameworks] Collected XCFramework zips (${collectedFiles.size}): ${collectedFiles.joinToString { it.name }}")
        logger.lifecycle("[packageAllXCFrameworks] Output directory: ${targetDir.absolutePath}")

        val aggregatedChecksums = LinkedHashMap<String, String>()
        collectedFiles.forEach { zip ->
            try {
                val process = ProcessBuilder("swift", "package", "compute-checksum", zip.absolutePath)
                    .directory(File(rootDirPath))
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
                val exitCode = process.waitFor()
                if (exitCode == 0 && output.isNotBlank()) {
                    aggregatedChecksums[zip.name] = output
                } else {
                    logger.warn("[packageAllXCFrameworks] Could not compute Swift checksum for ${zip.name} (exitCode=$exitCode, output='$output'). Install Swift toolchain if needed.")
                }
            } catch (e: Exception) {
                logger.warn("[packageAllXCFrameworks] Swift checksum computation failed for ${zip.name}: ${e.message}")
            }
        }

        if (aggregatedChecksums.isNotEmpty()) {
            logger.lifecycle("[packageAllXCFrameworks] 🔐 Aggregated Swift checksums:")
            aggregatedChecksums.forEach { (name, sum) -> logger.lifecycle(" - $name => $sum") }
            // Write file deterministically
            val file = checksumsFile.get().asFile
            file.parentFile.mkdirs()
            file.writeText(aggregatedChecksums.entries.joinToString(separator = System.lineSeparator()) { "${it.key}=${it.value}" })
            logger.lifecycle("[packageAllXCFrameworks] Checksums written to: ${file.absolutePath}")
        } else {
            logger.warn("[packageAllXCFrameworks] No Swift checksums computed. Install Xcode/Swift if you need the checksums.")
            // Ensure empty file exists (so output is still produced for up-to-date tracking)
            checksumsFile.get().asFile.apply { parentFile.mkdirs(); writeText("") }
        }
    }
}
