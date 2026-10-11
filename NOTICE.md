# A SUSFS Manager Built on FinaSU 0.1.0-beta3 — Read This First

# 基于 FinaSU-0.1.0-beta3 构建的 SUSFS 管理器 —— 请先读这里

> **Built with an AI coding agent. Please be gentle.**
> **本项目由 AI 大模型构建，还请轻喷。**
>
> This project was written with a large language model as the coding agent. The code was generated,
> not hand-typed, and it has not been through a human review pass. Treat it accordingly: it is a
> working transplant, not a hardened, audited release.
>
> 本项目的代码由 AI 大模型（编程智能体）生成，并非人工逐行编写，也未经过人工完整审阅。请据此判断：
> 它是一个能用的移植，不是经过加固与审计的正式版本。
>
> **Verified working only on two devices: Redmi Note 11T Pro and Realme GT7 Pro. No guarantee of
> usability anywhere else.**
> **仅在 Redmi Note 11T Pro 与 Realme GT7 Pro 上测试可用，不保证其他设备可用。**
>
> Every claim below is about what the source code does and what was observed on those two devices.
> Anything not tested on them is unverified.
>
> 下文所有描述说的是「代码写了什么」以及「在那两台设备上观察到了什么」。未在那两台设备上验证过的
> 部分，都属于未经证实。

---

## English

### Read this first: who this is for, and who it is not for

**This is a personal project, published for the author's own use. It will not be maintained.**

No bug fixes, no feature requests, no compatibility updates, no response to issues. If it breaks on
your device, on a newer Android, on a newer SUSFS, or on a kernel you patched differently, it will
stay broken. That is the deal, and it is better to know it now than after you have spent an evening
building it.

**You almost certainly have better options.** If you want SUSFS management on KernelSU, use one of
these instead — both are actively maintained, both are modules rather than standalone apps, and both
have far more users than this:

| Project | What it is | Licence | Activity |
| --- | --- | --- | --- |
| [SUSFS-FOR-KERNELSU](https://github.com/sidex15/susfs4ksu-module) | An addon root hiding service for KernelSU | AGPL-3.0 | Actively maintained |
| [BRENE - SuSFS](https://github.com/rrr333nnn333/BRENE) | A SuSFS/KernelSU module for SuSFS-patched kernels | AGPL-3.0 | Actively maintained |

**This app is only for you if all of the following hold:**

- You already run [FinaSU](https://github.com/LyraVoid/FinaSU) and specifically want **its** SUSFS
  panel, not a different one.
- You want that panel as a **separate launcher entry** instead of a screen inside the manager, and
  you are willing to build it yourself.
- You accept the cost that comes with it: an APK that carries its own `ksud` payload, must be
  rebuilt whenever you rebuild ksud, and is only known to work on the two devices listed above.
- You are comfortable reading the source when something goes wrong, **because there is no support**.

If any of those do not hold, use one of the two projects above. That is not a consolation prize —
they are more complete and better maintained than this. Choosing them is the correct decision, not
a failure to find the right thing.

**Do not open issues expecting a fix.** You are welcome to fork it, and GPL-3.0 gives you every
right to; you are just not going to get a reply from here.

### What this is

A standalone Android app that exposes exactly one screen: the SUSFS management UI extracted from
[FinaSU](https://github.com/LyraVoid/FinaSU) `0.1.0-beta3` (package `me.yuki.folksu`, a KernelSU
derivative, itself GPL-3.0).

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

### How this was built

Stated plainly, because it changes how you should read everything else:

- The port was carried out by a **large language model acting as a coding agent**. The Kotlin,
  Gradle, resource and tooling changes in this repository were generated by that model, not typed
  out by hand from a design document.
- It was directed and reviewed at the level of *outcomes* — "does it build", "does the screen match
  upstream", "does the command reach ksud" — rather than line-by-line reading of every diff.
- Consequently the code may contain the failure modes that AI-generated code is prone to:
  plausible-looking API usage that does not compile, subtly wrong coroutine or lifecycle handling,
  copy-paste residue from upstream that no longer applies, and comments that describe an intent the
  code does not quite implement.
- It was **tested for usability on Redmi Note 11T Pro and Realme GT7 Pro**, and nowhere else.

If you find a bug, that is expected, not surprising. Issues and pull requests are welcome — please
be gentle about the AI part.

### Status of this build

| | |
| --- | --- |
| Authored by | An AI coding agent, from the FinaSU 0.1.0-beta3 source tree |
| Human review | None. No line-by-line review has been done |
| Compiles | Yes, on the author's setup |
| Runs on a device | **Only on Redmi Note 11T Pro and Realme GT7 Pro.** No other device has been tried |
| Unit tests | None added; upstream's `SusfsPresentationTest` / `SusfsStatusTest` port only if its test deps are wired up |
| Static checks | `tools/static_check.py` passes: unresolved imports, missing strings, manifest class references, leftover package names, stray `TODO(`/`GlobalScope` |
| Signature | None. `assembleRelease` produces an unsigned APK by design |

The static checker is a text checker, not a compiler. It cannot catch a type error, a Compose
recomposition mistake, or an API that moved between library versions. A real build on the two test
devices is the only evidence behind the table above.

### Known gaps

- **Verified on two devices only.** Redmi Note 11T Pro and Realme GT7 Pro. Any other device,
  ROM, kernel or SUSFS build is untested — the app may work, or it may not.
- **No guarantee of usability.** "Works on these two" is not "works".
- **The `abiFilters` + `useLegacyPackaging` pairing is the load-bearing assumption.** If Android
  stops extracting `libksud.so` to disk with the execute bit set, the app breaks at the first command.
- **Untested against a second root manager.** Coexistence with FinaSU, Magisk, or another KernelSU
  build at boot is unverified territory.
- **`Natives.isSafeMode` reads `persist.sys.safemode` / `ro.sys.safemode` via `getprop`.** If the
  kernel is in safe mode but neither property reflects it, the app may attempt an apply the kernel
  will silently ignore.
- **Only `arm64-v8a`.** Other ABIs will not install the payload.
- **AI-generated code.** No human has reviewed it line by line. Compose recomposition, coroutine
  cancellation and shell-quoting are the kinds of things a reviewer catches and a generator does not.
- **No installer, no module management, no update check, no boot receiver.** All intentionally
  removed from upstream.

### About, and where this came from

This app is **not** affiliated with, endorsed by, or supported by the FinaSU project. It is an
unofficial extraction of one screen, built by a third party. Bug reports about *this* app belong
here; bug reports about FinaSU belong upstream.

What was taken, and from where:

| Component | Origin |
| --- | --- |
| `ui/screen/SusfsScreen.kt`, `ui/screen/SusfsEditors.kt` | FinaSU `manager/`, SUSFS panel |
| `ui/viewmodel/SusfsViewModel.kt` | FinaSU |
| `data/susfs/SusfsRepository.kt`, `SusfsPresentation.kt` | FinaSU |
| `ui/component/material/*`, `ui/theme/*`, `wallpaper/*` | FinaSU / KernelSU Material components |
| The `susfs` control protocol (`status`/`export`/`import`/`apply`) | FinaSU `userspace/ksud/src/susfs/` |
| Kernel-side SUSFS interface and `/proc` nodes | FinaSU / KernelSU, after [SUSFS](https://gitlab.com/simonpunk/susfs4ksu) |
| Material 3 expressive list APIs the screen builds on | AndroidX `material3` |
| Root shell | [libsu](https://github.com/topjohnwu/libsu) by topjohnwu |

Written for this port: the Gradle project, `SusfsApplication.kt`, `Natives.kt`,
`ui/util/RootShell.kt`, `ui/navigation3/Navigator.kt`, `ui/theme/SusfsTheme.kt`, `ui/MainActivity.kt`,
the launcher icon, and `tools/`.

### Licence

**GPL-3.0.** The full text is in [`LICENSE`](LICENSE), copied verbatim from FinaSU's own `LICENSE`.

This is not a choice — it is an obligation. The screens, theme components, view model and validation
logic are FinaSU's code, and FinaSU is a KernelSU derivative, and KernelSU is GPL-3.0. A derivative
work of GPL-3.0 code must itself be GPL-3.0. The port keeps upstream's licence, keeps the full
licence text in the repository, and names its origin above.

What that means if you redistribute this app or a modified version:

- Ship the complete corresponding source under GPL-3.0, including your modifications.
- Keep the [`LICENSE`](LICENSE) file and this attribution intact.
- State prominently what you changed and when.
- Do not add restrictions that the GPL does not allow, and do not relicense it as something more
  permissive.
- Note that **`libksud.so` is not in this repository** — it is a separate GPL-3.0 build artifact you
  produce from the FinaSU tree. If you ship an APK with it bundled, you are conveying both together.

There is no warranty of any kind, which for this project is not a formality: see the status table
above.

#### How that obligation is met here

| GPL-3.0 requirement | How this repository satisfies it |
| --- | --- |
| §2–3, copyleft of derivative works | Repo is GPL-3.0, not a more permissive licence |
| §4, complete corresponding source | All source is committed; the only omitted artifact is `libksud.so`, a separate crate you build yourself |
| §4–5, convey under GPL with licence text | [`LICENSE`](LICENSE) is upstream's file, byte-for-byte (md5 `1ebbd3e34237af26da5dc08a4e440464`) |
| §5(a), prominent notice of modification | [What was changed from upstream](README.md#what-was-changed-from-upstream-and-why) lists every replacement |
| §7(b), preserve attribution notices | The origin table above, in both languages |
| §11, no warranty | Stated above, and in [`README.md`](README.md) |
| §6, no extra restrictions | No additional terms are imposed on downstream recipients |

Dependency licences were checked for compatibility: **GPL-3.0** (FinaSU/KernelSU code),
**Apache-2.0** (AndroidX Compose, `material3`), **Apache-2.0** (Kotlin, coroutines), **Apache-2.0**
(libsu), **Apache-2.0** (Gradle wrapper JAR — build tooling, not linked into the app). No dependency
imposes a term the GPL forbids.

---

## 中文

### 请先读：这个应用给谁用，不给谁用

**这是纯粹的作者自用项目，放出来只是顺手。不会进行维护。**

不修 bug、不接受功能请求、不跟进兼容性、不回复 issue。如果它在你的设备上、在新版 Android 上、
在新版 SUSFS 上、或在你自己打的另一种内核上跑不起来，那它就会一直跑不起来。这就是条件，早点知道
比折腾一晚上之后才知道要好。

**你几乎肯定有更好的选择。** 如果你想在 KernelSU 上管理 SUSFS，请改用下面这两个 —— 都在积极维护，
都是模块而不是独立 App，用户量也远多于本项目：

| 项目 | 说明 | 许可 | 活跃度 |
| --- | --- | --- | --- |
| [SUSFS-FOR-KERNELSU](https://github.com/sidex15/susfs4ksu-module) | KernelSU 的附加 root 隐藏服务 | AGPL-3.0 | 积极维护 |
| [BRENE - SuSFS](https://github.com/rrr333nnn333/BRENE) | 面向已打 SuSFS 补丁内核的 SuSFS/KernelSU 模块 | AGPL-3.0 | 积极维护 |

**只有以下条件全部成立，这个应用才是给你用的：**

- 你本来就在用 [FinaSU](https://github.com/LyraVoid/FinaSU)，并且要的就是**它那个** SUSFS 面板，
  而不是别的实现。
- 你希望把这个面板做成**独立的启动器入口**，而不是管理器里的一个页面，并且愿意自己构建。
- 你接受随之而来的代价：APK 自带 `ksud` 载荷，每次重编 ksud 都得重新构建 App，而且已知能用的设备
  只有上面列出的那两台。
- 出问题时你愿意自己读源码，**因为这里没有支持**。

上面任何一条不成立，就去用那两个项目。这不是退而求其次 —— 它们比本项目更完整、维护得更好。选它们
是正确决定，而不是没找到「本该找到的东西」。

**不要抱着「提了就会修」的期待去开 issue。** 欢迎你 fork，GPL-3.0 给了你完整的权利；只是这里不会
有人回复。

### 这是什么

一个只做一件事的独立安卓应用：把 [FinaSU](https://github.com/LyraVoid/FinaSU) `0.1.0-beta3`（包名
`me.yuki.folksu`，KernelSU 的一个衍生版本，本身也是 GPL-3.0）里的 SUSFS 管理界面单独抽出来。

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

### 这是怎么构建的

直说，因为这会影响你怎么看后面所有内容：

- 本次移植由**大语言模型作为编程智能体**完成。本仓库里的 Kotlin、Gradle、资源与工具改动都是该模型
  生成的，不是照着设计文档手工敲出来的。
- 审阅是在**结果层面**做的 —— 「能不能编译」「界面和上游是否一致」「命令有没有真的走到 ksud」——
  而不是逐行读每一处 diff。
- 因此代码可能带有 AI 生成代码常见的毛病：看起来对但根本编译不过的 API 用法、微妙的协程或生命周期
  处理错误、从上游抄来但已经不适用的残留、以及描述意图与代码实际行为不符的注释。
- 可用性**仅在 Redmi Note 11T Pro 与 Realme GT7 Pro 上测试过**，其他设备一律没有。

如果你发现了 bug，这是意料之中的事，不必惊讶。欢迎提 issue 和 PR —— 关于 AI 这件事还请轻喷。

### 当前版本的状态

| | |
| --- | --- |
| 作者 | AI 编程智能体，基于 FinaSU 0.1.0-beta3 源码树 |
| 人工审阅 | 无。没有做过逐行审阅 |
| 能编译 | 可以，在作者的环境下 |
| 真机运行 | **仅在 Redmi Note 11T Pro 与 Realme GT7 Pro 上验证可用。** 未尝试过其他设备 |
| 单元测试 | 未添加；上游的 `SusfsPresentationTest` / `SusfsStatusTest` 只有在测试依赖接好后才能移植 |
| 静态检查 | `tools/static_check.py` 通过：无法解析的 import、缺失字符串、清单类引用、残留包名、游离的 `TODO(`/`GlobalScope` |
| 签名 | 无。`assembleRelease` 按设计产出未签名 APK |

静态检查是文本检查，不是编译器。它抓不到类型错误、Compose 重组错误，也抓不到库版本之间挪走的
API。上表唯一的实证依据，是在那两台测试设备上真实构建并运行过。

### 已知缺口

- **仅在两台设备上验证。** Redmi Note 11T Pro 与 Realme GT7 Pro。其他设备、ROM、内核或 SUSFS
  构建都未测试 —— 可能能用，也可能不能。
- **不保证可用性。** 「这两台能用」不等于「能用」。
- **`abiFilters` + `useLegacyPackaging` 这个组合是关键假设。** 如果 Android 不再把
  `libksud.so` 解压到磁盘并带上执行位，应用会在第一条命令就崩掉。
- **未在存在第二个 root 管理器的设备上测试。** 与 FinaSU、Magisk 或另一个 KernelSU 构建在开机
  阶段的共存属于未知领域。
- **`Natives.isSafeMode` 读的是 `getprop persist.sys.safemode` / `ro.sys.safemode`。** 如果内核
  处于安全模式但这两个属性都没反映出来，应用可能发起一次会被内核静默忽略的 apply。
- **只支持 `arm64-v8a`。** 其他 ABI 不会带上载荷。
- **AI 生成的代码。** 没有任何人逐行审阅过。Compose 重组、协程取消、shell 引号转义这类问题，正是
  人类审阅者能抓到而生成器抓不到的东西。
- **没有安装器、没有模块管理、没有更新检查、没有开机广播接收器。** 都是从上游有意移除的。

### 关于，以及它的来处

本应用**与 FinaSU 项目无隶属关系**，未获其背书，也不由其提供支持。它是第三方对其中一个页面的非官方
抽取。关于**本**应用的 bug 反馈请提到这里；关于 FinaSU 的请提到上游。

取了什么、从哪取的：

| 组件 | 来源 |
| --- | --- |
| `ui/screen/SusfsScreen.kt`、`ui/screen/SusfsEditors.kt` | FinaSU `manager/` 的 SUSFS 面板 |
| `ui/viewmodel/SusfsViewModel.kt` | FinaSU |
| `data/susfs/SusfsRepository.kt`、`SusfsPresentation.kt` | FinaSU |
| `ui/component/material/*`、`ui/theme/*`、`wallpaper/*` | FinaSU / KernelSU 的 Material 组件 |
| `susfs` 控制协议（`status`/`export`/`import`/`apply`） | FinaSU `userspace/ksud/src/susfs/` |
| 内核侧 SUSFS 接口与 `/proc` 节点 | FinaSU / KernelSU，其源头是 [SUSFS](https://gitlab.com/simonpunk/susfs4ksu) |
| 页面依赖的 Material 3 expressive 列表 API | AndroidX `material3` |
| root shell | topjohnwu 的 [libsu](https://github.com/topjohnwu/libsu) |

本次移植自己写的部分：Gradle 工程、`SusfsApplication.kt`、`Natives.kt`、`ui/util/RootShell.kt`、
`ui/navigation3/Navigator.kt`、`ui/theme/SusfsTheme.kt`、`ui/MainActivity.kt`、应用图标，以及
`tools/`。

### 许可

**GPL-3.0。** 完整文本见 [`LICENSE`](LICENSE)，从 FinaSU 自己的 `LICENSE` 原样复制。

这不是可选项，是义务。页面、主题组件、ViewModel 与校验逻辑都是 FinaSU 的代码，FinaSU 是 KernelSU 的
衍生物，而 KernelSU 是 GPL-3.0。GPL-3.0 代码的衍生作品本身必须是 GPL-3.0。本移植保留上游许可、在
仓库中保留完整许可文本，并在上面写明了来处。

如果你要再分发本应用或其修改版，这意味着：

- 以 GPL-3.0 提供完整的对应源码，包括你的修改。
- 保留 [`LICENSE`](LICENSE) 文件与本文的署名信息。
- 显著说明你改了什么、什么时候改的。
- 不得附加 GPL 不允许的限制，也不得改以更宽松的许可发布。
- 注意 **`libksud.so` 不在本仓库中** —— 它是你从 FinaSU 源码树产出的独立 GPL-3.0 构建产物。如果你
  把 APK 连同它一起分发，就是在同时传递两者。

本项目不提供任何形式的担保 —— 对这个项目来说这不是套话，见上面的状态表。

#### 本仓库如何履行该义务

| GPL-3.0 要求 | 本仓库的满足方式 |
| --- | --- |
| §2–3 衍生作品的 copyleft | 仓库以 GPL-3.0 发布，未改为更宽松的许可 |
| §4 完整对应源码 | 所有源码均已提交；唯一未包含的是 `libksud.so`，那是你自己构建的独立 crate |
| §4–5 随附许可文本 | [`LICENSE`](LICENSE) 是上游文件逐字节复制（md5 `1ebbd3e34237af26da5dc08a4e440464`） |
| §5(a) 显著的修改声明 | [相对上游改了什么](README.md#what-was-changed-from-upstream-and-why) 逐条列出每处替换 |
| §7(b) 保留署名信息 | 上文的中英双语来源表 |
| §11 无担保 | 上文已声明，[`README.md`](README.md) 中也有 |
| §6 不附加额外限制 | 未对下游接收者施加任何额外条款 |

依赖许可已核对兼容性：**GPL-3.0**（FinaSU/KernelSU 代码）、**Apache-2.0**（AndroidX Compose、
`material3`）、**Apache-2.0**（Kotlin、coroutines）、**Apache-2.0**（libsu）、**Apache-2.0**
（Gradle wrapper JAR —— 构建工具，不链接进应用）。没有任何依赖施加 GPL 所禁止的条款。
