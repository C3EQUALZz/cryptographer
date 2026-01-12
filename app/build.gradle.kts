plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.kapt)
}
hilt {
    enableAggregatingTask = false
}

android {
    namespace = "com.example.cryptographer"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.cryptographer"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

kapt {
    correctErrorTypes = true
    // Fix for Kotlin 2.0+ metadata issues with Hilt
    useBuildCache = true
    javacOptions {
        option("-source", "17")
        option("-target", "17")
        // Additional options for better compatibility
        option("-parameters")
    }
    // Map diagnostic locations for better error reporting
    mapDiagnosticLocations = true
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Logging
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Spotless configuration (code formatter - like ruff for Python)
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/generated/**")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable,Preview",
                ),
            )
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**", "**/generated/**")
        trimTrailingWhitespace()
        indentWithSpaces(4)
        endWithNewline()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(file("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
}

// Configure detekt tasks to use JVM target 17 (detekt doesn't support JVM 21)
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
    // Exclude color configuration files from detekt checks
    // (hex color values are not magic numbers in this context)
    exclude("**/setup/configs/theme/Color.kt")
}

// Suppress warning about dynamic Java agent loading (used by MockK)
tasks.withType<Test>().configureEach {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

tasks.named("check") {
    dependsOn("spotlessCheck", "detekt")
}

tasks.named("spotlessApply") {
    description = "Format code using Spotless (ktlint)"
}

tasks.named("detekt") {
    description = "Run Detekt static code analysis"
}

// Task to install git hooks
tasks.register("installGitHooks") {
    description = "Install git hooks for pre-commit checks"
    doLast {
        val hooksDir = rootProject.file(".githooks")
        val preCommitHook = hooksDir.resolve("pre-commit")
        val installScript = hooksDir.resolve("install.sh")

        if (!preCommitHook.exists()) {
            throw GradleException("Pre-commit hook not found at ${preCommitHook.absolutePath}")
        }

        // Make hooks executable (Unix-like systems)
        if (!System.getProperty("os.name").lowercase().contains("windows")) {
            val chmodProcess =
                ProcessBuilder("chmod", "+x", preCommitHook.absolutePath)
                    .start()
            chmodProcess.waitFor()

            if (installScript.exists()) {
                val chmodScriptProcess =
                    ProcessBuilder("chmod", "+x", installScript.absolutePath)
                        .start()
                chmodScriptProcess.waitFor()
            }
        }

        // Configure git to use .githooks directory
        val gitProcess =
            ProcessBuilder("git", "config", "core.hooksPath", ".githooks")
                .start()
        val exitCode = gitProcess.waitFor()

        if (exitCode != 0) {
            throw GradleException("Failed to configure git hooks path")
        }

        println("✅ Git hooks installed successfully!")
        println("   Pre-commit hook will now run Spotless and Detekt before each commit.")
    }
}
