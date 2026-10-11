# SUSFS Manager

> **Untested software — read [NOTICE.md](NOTICE.md) before building.**
> It has never been run on a device. It is a UI transplant for people who already run FinaSU and
> want the SUSFS panel as its own app; it is not a replacement for any root manager.
>
> **未经测试的软件 —— 构建前请先读 [NOTICE.md](NOTICE.md)。**
> 它从未在真机上运行过。它是给「本来就在用 FinaSU、想把 SUSFS 面板单独拿出来」的人做的界面移植，
> 不能替代任何 root 管理器。

A standalone Android app that is **only** the SUSFS management UI, extracted from
FinaSU 0.1.0-beta3 (a KernelSU fork). It does not bundle a kernel, a daemon, or a module store.

Like upstream, it execs the `ksud` binary out of its own APK. **The SUSFS subcommand only exists in
FinaSU's ksud, not in stock KernelSU or KernelSU-Next**, so this app bundles FinaSU's ksud as
`libksud.so`. See [Bundling ksud](#bundling-ksud) — building without it produces
`error: unrecognized subcommand 'susfs'` at runtime.

## What it does

The app drives the `susfs` subcommands of the bundled `ksud`:

| Action   | Command                            | Purpose                                         |
|----------|------------------------------------|-------------------------------------------------|
| status   | `<ksud> susfs status`              | Detect the SUSFS build and its capabilities      |
| export   | `<ksud> susfs export`              | Read the saved configuration as JSON             |
| import   | `printf '%s' <json> \| <ksud> susfs import` | Validate and save a configuration       |
| apply    | `<ksud> susfs apply`               | Ask the kernel to adopt the saved configuration  |

The UI is the six-page pager from upstream, unchanged:

0. **Overview** — detection, management state, saved rule count, automatic mode, apply, last report,
   import/export, and the detected feature list.
1. **Settings** — hide mounts (SUS_MOUNT), logging (ENABLE_LOG), AVC spoofing.
2. **Paths** — `path` and `map` rules.
3. **Spoofing** — `uname`, `cmdline`, and `kstat` rules.
4. **Redirect** — `redirect` rules.
5. **Extensions** — hidden modules and mount prefixes.

## Requirements

* Android 12 (API 31) or newer, on **arm64-v8a** (the only ABI ksud is built for).
* A device rooted with **one of** KernelSU, KernelSU-Next, APatch, or Magisk — this supplies `su` and
  the kernel-side SUSFS implementation — **and** a kernel built with the SUSFS patch. Without the
  kernel patch the app still installs and runs, but the status page reports `not_detected` and every
  control stays disabled: that is the intended behaviour, not a bug.
* Superuser access granted to **SUSFS Manager** itself (package `dev.susfs.manager`). The first
  launch raises the prompt; if it is denied the status page shows "root unavailable" until you grant
  it and reopen the app.

The rooting solution's own `ksud` is **not** used. Only `su` comes from it.

## Bundling ksud

`ksud` is FinaSU's Rust userspace daemon. It is compiled for Android and, like stock KernelSU,
packaged under `jniLibs` with a `.so` name so Android extracts it as a real executable with the
execute bit set. `useLegacyPackaging = true` in `app/build.gradle.kts` is what makes that extraction
produce a file on disk instead of something the linker would mmap as an ELF shared object.

Build it from the FinaSU tree, then copy it in **before** assembling this app:

```sh
cd FinaSU-0.1.0-beta3/userspace/ksud
cargo ndk -t arm64-v8a build --release
mkdir -p <this project>/app/src/main/jniLibs/arm64-v8a
cp target/aarch64-linux-android/release/ksud \
   <this project>/app/src/main/jniLibs/arm64-v8a/libksud.so
```

`tools/static_check.py` prints a note while this file is missing, and fails if the file exists but is
implausibly small. The file is deliberately **not** committed: it is a multi-megabyte binary
artifact of a separate Rust crate.

The Gradle build enforces the same rule. `preBuild` logs a warning for every variant, so a debug
build of the UI still works while you are compiling ksud; `preReleaseBuild` fails outright, because
a release APK without the payload is a broken app rather than an incomplete one.

## Building

The project is a single-module Gradle build. It needs a JDK 21 toolchain and an Android SDK with
API 37 platform and build-tools installed.

```sh
# Point Gradle at your SDK if it is not in ANDROID_HOME / ANDROID_SDK_ROOT.
echo "sdk.dir=/path/to/android-sdk" > local.properties

./gradlew :app:assembleDebug     # debug APK
./gradlew :app:assembleRelease   # release APK (minified; needs a signing config, see below)
```

The APK lands in `app/build/outputs/apk/<variant>/`.

### Signing a release build

`app/build.gradle.kts` deliberately declares no signing config, so `assembleRelease` produces an
unsigned APK. Either install the debug build, sign the release APK yourself, or add a signing block
to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore.jks")
            storePassword = "…"
            keyAlias = "…"
            keyPassword = "…"
        }
    }
    buildTypes {
        release { signingConfig = signingConfigs.getByName("release") }
    }
}
```

### Version catalog

`gradle/libs.versions.toml` pins the versions upstream used, because the SUSFS screen is written
against the Material 3 expressive APIs (`SegmentedListItem`, `ListItemShapes`,
`LargeFlexibleTopAppBar`, `RippleConfiguration`) that only exist in that line:

| Component        | Version        |
|------------------|----------------|
| AGP              | 9.4.1          |
| Kotlin           | 2.4.20         |
| Compose BOM      | 2026.09.00     |
| material3        | 1.5.0-alpha28  |
| graphics-shapes  | 1.1.0          |
| lifecycle        | 2.11.0         |
| Gradle (wrapper) | 9.7.1          |

SDK levels live in the root `build.gradle.kts` as `rootProject.extra` values
(`androidCompileSdkVersion` 37, `androidTargetSdkVersion` 37, `androidMinSdkVersion` 31, Java 21).
Editing them there retargets the whole build.

## Project layout

```
app/src/main/java/dev/susfs/manager/
├── SusfsApplication.kt              # shell builder + one startup root probe
├── Natives.kt                       # safe-mode flag (was a JNI getter upstream)
├── data/susfs/
│   ├── SusfsRepository.kt           # ksud susfs status/export/import/apply
│   └── SusfsPresentation.kt         # apply report + input validation
├── ui/
│   ├── MainActivity.kt              # the only activity
│   ├── screen/SusfsScreen.kt        # pager, top bar, import/export, overview
│   ├── screen/SusfsEditors.kt       # per-rule editors
│   ├── viewmodel/SusfsViewModel.kt  # state machine
│   ├── util/RootShell.kt            # one shared root shell
│   ├── navigation3/Navigator.kt     # stand-in for the navigation3 back stack
│   ├── component/material/          # the Material 3 components the screen needs
│   └── theme/                       # theme, shapes, motion
├── wallpaper/FolkWallpaperTokens.kt # wallpaper tokens (unset -> plain Material colours)
└── jniLibs/arm64-v8a/libksud.so     # (you supply this) ksud, bundled as a fake library

art/logo.svg                         # icon source
tools/static_check.py                # the checks below
tools/render_icons.{sh,js}           # regenerates the launcher icons from art/logo.svg
```

## What was changed from upstream, and why

The UI layer is a near-verbatim port, so this list is short on purpose.

* **Package** renamed `me.weishu.kernelsu` → `dev.susfs.manager`.
* **`Natives` is no longer JNI.** Upstream's `isSafeMode` came from `libkernelsu.so`. Here it is
  read once per root shell via `getprop persist.sys.safemode` / `ro.sys.safemode`, matching what
  `ksud` itself does. The only kernel-side safe-mode signal is an ioctl a shell cannot reach, so the
  worst case is that the app offers Apply while the kernel ignores it.
* **Root shell simplified.** Upstream keeps a global-mount shell, a bounded backoff retry loop, and
  an `sh` fallback. All three exist to serve a whole manager app. Here one shell is enough, and a
  non-root shell is reported as a failed probe rather than silently accepted.
* **Theme simplified.** Upstream's palette/wallpaper-driven theme (materialKolor, user-selectable
  colour specs) is replaced by a dynamic-colour scheme. The screen reads
  `LocalFolkWallpaperTokens` directly, and every wallpaper branch short-circuits to plain Material
  colours when that local is null, so leaving it unset is a supported path rather than a stub.
* **Navigation.** Upstream pops a navigation3 back stack; the back arrow here finishes the activity.
  The `Navigator` interface is preserved so `SusfsScreen.kt` needed no edits.
* **Removed** everything that is not SUSFS: the module store, superuser list, web UI, installers,
  update checker, boot receivers, zygote preload, and the bundled native code — except `ksud` itself,
  which is required.
* **Unused Material components removed.** `SegmentedList.kt` upstream also carries sliders,
  dropdowns, radio buttons, checkboxes and text fields. The SUSFS screen uses none of them, so they
  are dropped — which also removes this port's only dependency on the newest expressive widgets.

## Static checks

`tools/static_check.py` runs without a JDK, an SDK or a network, and catches the mistakes a port
like this actually makes: a left-behind upstream package reference, a package declaration that does
not match its directory, an internal import naming a declaration that does not exist, an
`R.string.*` that is used but never defined, a locale file missing a key, a manifest pointing at a
class or resource that is not there, and a repository that has stopped exec'ing the bundled
`libksud.so`.

```sh
python3 tools/static_check.py
```

It is not a compiler. Run a real build before shipping.

## Launcher icon

`art/logo.svg` is the source. The rendered PNGs are committed, so **a normal build needs no SVG
toolchain**. Only re-run this after changing the logo:

```sh
npm install @resvg/resvg-js     # once
tools/render_icons.sh
```

It emits legacy icons per density plus an adaptive foreground whose canvas is 108dp but whose
content is scaled to 66% and centred, so no launcher mask clips it. The script is reproducible: it
regenerates the committed PNGs byte-for-byte.

## Credits

The SUSFS UI, the `susfs` control protocol, and the Material components in
`ui/component/material/` and `wallpaper/` come from FinaSU / KernelSU (GPL-3.0). This port keeps
that licence.
