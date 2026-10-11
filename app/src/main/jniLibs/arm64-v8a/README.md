# Put ksud here

This directory must contain `libksud.so`, which is the `ksud` binary from
[FinaSU](https://github.com/LyraVoid/FinaSU) renamed.

Stock KernelSU and KernelSU-Next `ksud` do **not** implement the `susfs` subcommand, so the app
cannot use the one already on the device. Build FinaSU's from source:

```sh
git clone https://github.com/LyraVoid/FinaSU.git
cd FinaSU/userspace/ksud
cargo ndk -t arm64-v8a build --release
cp target/aarch64-linux-android/release/ksud \
   <this project>/app/src/main/jniLibs/arm64-v8a/libksud.so
```

`tools/static_check.py` notes this file as missing until you do, and fails if what is here is
obviously not a real binary. The binary is not committed because it is a build artifact of a
separate Rust crate, and because it is GPL-3.0 code you produce yourself: if you distribute an APK
with it bundled, you are conveying that binary together with this app. See `NOTICE.md`.
