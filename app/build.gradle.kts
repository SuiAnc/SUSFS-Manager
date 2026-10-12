@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val androidCompileSdkVersion = rootProject.extra["androidCompileSdkVersion"] as Int
val androidCompileSdkVersionMinor = rootProject.extra["androidCompileSdkVersionMinor"] as Int
val androidMinSdkVersion = rootProject.extra["androidMinSdkVersion"] as Int
val androidTargetSdkVersion = rootProject.extra["androidTargetSdkVersion"] as Int
val androidSourceCompatibility = rootProject.extra["androidSourceCompatibility"] as JavaVersion
val androidTargetCompatibility = rootProject.extra["androidTargetCompatibility"] as JavaVersion

android {
    namespace = "dev.susfs.manager"
    compileSdk {
        version = release(androidCompileSdkVersion) {
            minorApiLevel = androidCompileSdkVersionMinor
        }
    }

    defaultConfig {
        applicationId = "dev.susfs.manager"
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        versionCode = 1
        versionName = "1.0.0"

        // ksud is only shipped for arm64-v8a, so the APK is arm64-only. Declaring the filter keeps
        // AGP from also packaging the (absent) 32-bit and x86 native directories.
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        // `libksud.so` is not a library: it is the ksud executable, renamed so Android packages it
        // and extracts it with the execute bit set. Legacy packaging is what makes that extraction
        // produce a real file on disk instead of an entry the linker would try to mmap as an ELF.
        // Upstream relies on exactly the same trick.
        jniLibs {
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = androidSourceCompatibility
        targetCompatibility = androidTargetCompatibility
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// The app execs its `ksud` payloads out of the native library dir at runtime, so an APK built
// without them installs cleanly and then fails every SUSFS call with `unrecognized subcommand
// 'susfs'` -- the exact bug this project exists to fix. `tools/static_check.py` reports the same
// thing offline.
//
// Two payloads are bundled because the app can drive two different managers:
//   libksud.so   -- FolkSU / FinaSU, `ksud susfs status|export|import|apply` (JSON protocol)
//   libksud2.so  -- SukiSU-Ultra,   `ksud susfs status|config|...` (config-key protocol)
// Android extracts every `lib*.so` from jniLibs when useLegacyPackaging is on, which is what makes
// executing them from nativeLibraryDir legal.
//
// These are plain Gradle lifecycle tasks (`preBuild` and the per-variant `preReleaseBuild`), so
// nothing here depends on an AGP variant API that may move between releases. Debug only warns, so
// the UI can still be built while a payload is being compiled; release is fatal, because shipping
// this APK would be shipping a broken app.
val ksudPayloads = listOf(
    Triple(
        "libksud.so",
        "FolkSU / FinaSU (JSON export / import / apply protocol)",
        layout.projectDirectory.file("src/main/jniLibs/arm64-v8a/libksud.so").asFile,
    ),
    Triple(
        "libksud2.so",
        "SukiSU-Ultra (susfs config-key protocol)",
        layout.projectDirectory.file("src/main/jniLibs/arm64-v8a/libksud2.so").asFile,
    ),
)
val ksudMissingMessage = ksudPayloads.joinToString("\n\n") { (name, who, file) ->
    if (file.exists()) {
        ""
    } else {
        """
        app/src/main/jniLibs/arm64-v8a/$name is missing.
        Needed to drive $who.
        The APK will install, but those SUSFS commands will fail with:
          error: unrecognized subcommand 'susfs'
        Build that manager's ksud and copy it in -- see README.md, "Bundling ksud".
        """.trimIndent()
    }
}.let { it.replace(Regex("\n{3,}"), "\n\n").trim() }

tasks.matching { it.name == "preBuild" }.configureEach {
    doFirst { if (ksudMissingMessage.isNotEmpty()) logger.warn("WARNING:\n$ksudMissingMessage") }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst { if (ksudMissingMessage.isNotEmpty()) throw GradleException(ksudMissingMessage) }
}

kotlin {
    compilerOptions {
        // The SUSFS screen is built entirely from Material 3 expressive APIs, which are still
        // opt-in in the material3 line pinned in gradle/libs.versions.toml.
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.graphics.shapes)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.io)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
