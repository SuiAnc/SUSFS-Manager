# A SUSFS Manager Built on FinaSU 0.1.0-beta3 — Read This First

# 基于 FinaSU-0.1.0-beta3 构建的 SUSFS 管理器 —— 请先读这里

> **Untested software. 未经大量测试的软件。**
>
> This app has never been run on a real device by its author. Every claim below is about what the
> source code does, not about what has been observed to work.
>
> 本应用作者从未在真机上运行过它。下文所有描述说的都是「代码写了什么」，不是「实测能跑通什么」。

---

## English

### What this is

A standalone Android app that exposes exactly one screen: the SUSFS management UI extracted from
[FinaSU](https://github.com/) `0.1.0-beta3` (package `me.yuki.folksu`, a KernelSU derivative).

It is a **UI transplant, not a reimplementation**. The Compose screens, theme components, view model,
JSON configuration model, and the full validation logic are FinaSU's code, carried over with the
KernelSU-specific packaging stripped out. Only five things were replaced:

| Upstream | This app | Why |
| --- | --- | --- |
| `libksud.so` inside the APK | `libksud.so` inside the APK | Unchanged — see the warning below |
| `Natives.isSafeMode` (JNI) | `getprop` over the root shell | The JNI library belongs to FinaSU's build |
| `RootShell` with global-mount slot, backoff, `sh` fallback | Simplified single-slot shell | Same behaviour for this screen, less to go wrong |
| `navigation3` back stack | `Navigator` interface backed by `finish()` | Removes a whole navigation dependency |
| materialKolor wallpaper theming | Dynamic color (API 31+) | Drops a theme dependency; the wallpaper token provider returns `null`, which the components already treat as a supported path |

Everything else — the six-page pager, the JSON schema, `SusfsInputs.validPath`, the concurrent-edit
guard in `replaceRule`, the 128 KiB import/export limits, the 15 s command timeout — is upstream
logic, byte for byte.

### Read this before you build: ksud is not optional

The app runs `susfs status` / `export` / `import` / `apply` by executing a **`ksud` binary that it
carries inside its own APK**, extracted from `nativeLibraryDir`. This mirrors upstream.

That binary must be **FinaSU's ksud**. The `susfs` subcommand is a FinaSU addition — `cli.rs` in the
FinaSU tree declares `Susfs { command }` and dispatches it to the SUSFS module. Stock KernelSU and
KernelSU-Next `ksud` do **not** implement it. Use the wrong binary and every operation fails with:

```
error: unrecognized subcommand 'susfs'
```

There is no runtime workaround. The app cannot borrow the manager's ksud, cannot read SUSFS state
from `/proc` directly, and cannot fake the protocol. The `susfs` commands *are* the interface.

So: build ksud from the FinaSU tree, copy it to
`app/src/main/jniLibs/arm64-v8a/libksud.so`, and only then assemble. `README.md` has the commands.
The Gradle build enforces this — a **release** build fails outright when the payload is missing, a
debug build only warns.

### Why this app exists, and why you may not want it

FinaSU is a full root manager: module store, superuser list, installer, web UI, update checker. The
SUSFS panel is one screen inside it. To get that screen, you install a whole manager.

That is a real cost:

- **You probably already have a manager.** Magisk, KernelSU, APatch — pick one. Nobody installs a
  second root manager to get a settings page.
- **Two managers on one device is not obviously safe.** They may both want `/data/adb`, both watch
  for module changes, both try to apply configuration at boot. This project cannot promise
  otherwise, because it has not been tested on a device that has both.
- **This app is not a replacement.** It does not grant root, does not manage modules, does not
  handle su requests. It needs a root solution already present and willing to grant it root.

The intended use is narrow: **you already run FinaSU, you want the SUSFS panel, and you would rather
have it as its own launcher entry than buried inside the manager.** If that is not you, this app
probably is not for you — and that is a reasonable conclusion, not a failure of the app.

### Requirements

- Android 12 (API 31) or newer. Target/compile SDK 37.
- `arm64-v8a`. The `abiFilters` line makes this the only supported ABI.
- A kernel with the SUSFS patch **already applied**, plus KernelSU / KernelSU-Next / APatch running
  and granting root to `dev.susfs.manager`.
- **FinaSU's `ksud`, bundled into the APK at build time.**

If SUSFS is not in the kernel, the status page reports `not_detected` and every control is disabled.
That is the app working correctly, not a bug.

Note that `protocol` must be `compatible` for anything to be writable. FinaSU's `classify()` only
grants that when the SUSFS version is exactly `v2.3.0` with a GKI/NON-GKI variant and a recognised
feature set. An older or newer SUSFS patch yields `unknown_protocol` and a read-only UI.

### Status of this build

| | |
| --- | --- |
| Compiles (author's claim) | Not verified — never built by the author |
| Runs on a device | **Never tested** |
| Unit tests | None added; upstream's `SusfsPresentationTest` / `SusfsStatusTest` port only if its test deps are wired up |
| Static checks | `tools/static_check.py` passes: unresolved imports, missing strings, manifest class references, leftover package names, stray `TODO(`/`GlobalScope` |
| Signature | None. `assembleRelease` produces an unsigned APK by design |

The static checker is a text checker, not a compiler. It cannot catch a type error, a Compose
recomposition mistake, or an API that moved between library versions. **A real build is the only
evidence that matters, and it has not happened.**

### Known gaps

- **Never executed.** No APK from this project has been installed.
- **The `abiFilters` + `useLegacyPackaging` pairing is the load-bearing assumption.** If Android
  stops extracting `libksud.so` to disk with the execute bit set, the app breaks at the first command.
- **Untested against a second root manager.** Coexistence with FinaSU, Magisk, or another KernelSU
  build at boot is unverified territory.
- **`Natives.isSafeMode` reads `persist.sys.safemode` / `ro.sys.safemode` via `getprop`.** If the
  kernel is in safe mode but neither property reflects it, the app may attempt an apply the kernel
  will silently ignore.
- **Only `arm64-v8a`.** Other ABIs will not install the payload.
- **No installer, no module management, no update check, no boot receiver.** All intentionally
  removed from upstream.

### Licence

GPL-3.0. The UI, the JSON protocol, and the command semantics are FinaSU's and KernelSU's work.

---

## 中文

### 这是什么

一个只做一件事的独立安卓应用：把 [FinaSU](https://github.com/) `0.1.0-beta3`（包名
`me.yuki.folksu`，KernelSU 的一个衍生版本）里的 SUSFS 管理界面单独抽出来。

它是**界面移植，不是重新实现**。Compose 页面、主题组件、ViewModel、JSON 配置模型以及整套校验
逻辑都是 FinaSU 的代码，只是去掉了 KernelSU 特有的打包部分。只替换了五处：

| 上游 | 本应用 | 原因 |
| --- | --- | --- |
| APK 内的 `libksud.so` | APK 内的 `libksud.so` | 未改动 —— 见下方警告 |
| `Natives.isSafeMode`（JNI） | 通过 root shell 执行 `getprop` | JNI 库属于 FinaSU 自己的构建 |
| 带 global-mount 槽、退避重试、`sh` 回退的 `RootShell` | 简化的单槽 shell | 对本界面行为一致，出错面更小 |
| `navigation3` 返回栈 | `Navigator` 接口 + `finish()` | 去掉整个导航依赖 |
| materialKolor 壁纸取色 | 动态取色（API 31+） | 去掉主题依赖；壁纸 token provider 返回 `null`，组件本就把它当受支持路径 |

其余部分 —— 六页 Pager、JSON schema、`SusfsInputs.validPath`、`replaceRule` 里的并发编辑保护、
128 KiB 导入导出上限、15 秒命令超时 —— 都是上游逻辑，逐字未改。

### 构建前必读：ksud 不是可选项

应用是通过执行**自己 APK 内携带的 `ksud` 二进制**（从 `nativeLibraryDir` 解压出来）来跑
`susfs status` / `export` / `import` / `apply` 的。这一点与上游一致。

那个二进制必须是 **FinaSU 的 ksud**。`susfs` 是 FinaSU 自己加的子命令 —— FinaSU 源码的
`cli.rs` 里声明了 `Susfs { command }` 并派发到 SUSFS 模块。原版 KernelSU 和 KernelSU-Next 的
`ksud` **没有**实现它。用错二进制，每条操作都会失败：

```
error: unrecognized subcommand 'susfs'
```

运行时没有绕过的办法。应用无法借用管理器的 ksud，无法直接从 `/proc` 读 SUSFS 状态，也无法伪造
协议。`susfs` 这组命令**就是**那个接口。

所以：先从 FinaSU 源码树编译 ksud，复制到
`app/src/main/jniLibs/arm64-v8a/libksud.so`，然后才能打包。命令见 `README.md`。Gradle 构建也做了
兜底 —— **release** 缺载荷直接失败，debug 只警告。

### 为什么会有这个应用，以及为什么你可能并不需要它

FinaSU 是一个完整的 root 管理器：模块商店、超级用户列表、安装器、Web UI、更新检查。SUSFS 面板
只是里面的一个页面。为了这一个页面，你得装一整个管理器。

这个代价是实打实的：

- **你多半已经有管理器了。** Magisk、KernelSU、APatch 三选一。没人会为了一个设置页面去装第二个
  root 管理器。
- **一个设备上装两个管理器，不一定安全。** 它们可能都想占 `/data/adb`，都监听模块变化，都在开机时
  试图应用配置。本项目无法保证不会冲突，因为根本没在「两个管理器共存」的设备上测过。
- **它不是替代品。** 它不授予 root，不管模块，不处理 su 请求。它需要设备上已经有一套 root 方案，
  并且那套方案愿意给它 root。

预期用法很窄：**你本来就在用 FinaSU，你想要那个 SUSFS 面板，并且你宁愿它在桌面上单独占一个图标，
而不是埋进管理器里。** 如果你不是这种情况，这个应用大概不适合你 —— 这是个合理的判断，不是应用的
失败。

### 系统要求

- Android 12（API 31）及以上。target/compile SDK 37。
- `arm64-v8a`。`abiFilters` 让它成为唯一支持的 ABI。
- 内核**已经打好** SUSFS 补丁，并且 KernelSU / KernelSU-Next / APatch 正在运行、已给
  `dev.susfs.manager` 授予 root。
- **FinaSU 的 `ksud`，构建时打进 APK。**

如果内核里没有 SUSFS，状态页会显示 `not_detected`，所有控件禁用。这是应用在正常工作，不是 bug。

注意 `protocol` 必须是 `compatible` 才有任何可写项。FinaSU 的 `classify()` 只在 SUSFS 版本恰好为
`v2.3.0`、变体为 GKI/NON-GKI、且特性集可识别时才给出这个值。版本偏老或偏新都会得到
`unknown_protocol`，界面变成只读。

### 当前版本的状态

| | |
| --- | --- |
| 能编译（作者声称） | 未验证 —— 作者从未构建过 |
| 真机运行 | **从未测试** |
| 单元测试 | 未添加；上游的 `SusfsPresentationTest` / `SusfsStatusTest` 只有在测试依赖接好后才能移植 |
| 静态检查 | `tools/static_check.py` 通过：无法解析的 import、缺失字符串、清单类引用、残留包名、游离的 `TODO(`/`GlobalScope` |
| 签名 | 无。`assembleRelease` 按设计产出未签名 APK |

静态检查是文本检查，不是编译器。它抓不到类型错误、Compose 重组错误，也抓不到库版本之间挪走的
API。**真正构建一次才是唯一有意义的证据，而这件事还没发生。**

### 已知缺口

- **从未执行过。** 本项目没有产出过被安装的 APK。
- **`abiFilters` + `useLegacyPackaging` 这个组合是关键假设。** 如果 Android 不再把
  `libksud.so` 解压到磁盘并带上执行位，应用会在第一条命令就崩掉。
- **未在存在第二个 root 管理器的设备上测试。** 与 FinaSU、Magisk 或另一个 KernelSU 构建在开机
  阶段的共存属于未知领域。
- **`Natives.isSafeMode` 读的是 `getprop persist.sys.safemode` / `ro.sys.safemode`。** 如果内核
  处于安全模式但这两个属性都没反映出来，应用可能发起一次会被内核静默忽略的 apply。
- **只支持 `arm64-v8a`。** 其他 ABI 不会带上载荷。
- **没有安装器、没有模块管理、没有更新检查、没有开机广播接收器。** 都是从上游有意移除的。

### 许可

GPL-3.0。界面、JSON 协议与命令语义均来自 FinaSU 与 KernelSU 的工作。
