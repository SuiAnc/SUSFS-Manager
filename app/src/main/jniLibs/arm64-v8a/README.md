# Put the two ksud payloads here

This directory must contain **two** `ksud` binaries, renamed so Android extracts them as
executables:

| File | Manager | Page |
|------|---------|------|
| `libksud.so` | [FinaSU](https://github.com/LyraVoid/FinaSU) | FolkSU |
| `libksud2.so` | [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) | SukiSU-Ultra |

Stock KernelSU and KernelSU-Next `ksud` do **not** implement the `susfs` subcommand, so the app
cannot use the one already on the device. Build each from its own source tree:

```sh
# FolkSU / FinaSU
git clone https://github.com/LyraVoid/FinaSU.git
cd FinaSU/userspace/ksud
cargo ndk -t arm64-v8a build --release
cp target/aarch64-linux-android/release/ksud \
   <this project>/app/src/main/jniLibs/arm64-v8a/libksud.so

# SukiSU-Ultra
git clone https://github.com/SukiSU-Ultra/SukiSU-Ultra.git
cd SukiSU-Ultra/userspace/ksud
cargo ndk -t arm64-v8a build --release
cp target/aarch64-linux-android/release/ksud \
   <this project>/app/src/main/jniLibs/arm64-v8a/libksud2.so
```

The two are not interchangeable: the FolkSU page speaks the JSON `export` / `import` / `apply`
protocol, the SukiSU-Ultra page speaks its own config-key protocol. Keeping the names straight is
what routes each page to the right binary — `libksud.so` for FolkSU, `libksud2.so` for SukiSU-Ultra.

`tools/static_check.py` notes each file as missing until you do, and fails if what is here is
obviously not a real binary. The binaries are not committed because they are build artifacts of
separate Rust crates, and because they are GPL-3.0 code you produce yourself: if you distribute an
APK with them bundled, you are conveying those binaries together with this app. See `NOTICE.md`.
