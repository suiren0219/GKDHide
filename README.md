# GKD 隐身 (GKDHide)

> **2026-10 更新**：本模块的无障碍隐藏能力已并入 [GKDX](https://github.com/suiren0219/GKDX)（订阅引擎 + 无障碍隐藏二合一）。本仓库继续保留，适合只想要"隐藏"单一功能、继续用 GKD 本体跑订阅的场景。

一个 LSPosed 模块：**在目标应用中隐藏 GKD 的无障碍服务状态**，让"检测到无障碍服务开启，禁止使用"的应用正常工作。

GKD 本体（[gkd-kit/gkd](https://github.com/gkd-kit/gkd)）不受任何影响，无障碍服务保持开启，跳广告功能照常运行。

## 原理

应用检测无障碍服务常见有三种手段，本模块在目标应用进程内全部拦截：

| 检测手段 | 模块处理 |
|---|---|
| `AccessibilityManager.getEnabledAccessibilityServiceList()` 查已启用服务列表 | 从返回列表中过滤掉 GKD 的服务 |
| `AccessibilityManager.isEnabled()` / `isTouchExplorationEnabled()` 查全局开关 | strict 模式下返回 `false` |
| `Settings.Secure.getString/int` 查 `enabled_accessibility_services`、`accessibility_enabled` 等设置表 | strict 模式下返回空串 / `"0"` |

可选开启"隐藏已安装服务列表"（`getInstalledAccessibilityServiceList`），对付"装了自动化工具就拒绝"的检测，代价是可能影响目标应用自身的无障碍辅助功能。

## 使用方法

1. 安装 APK，在 **LSPosed 管理器**中启用本模块；
2. 在模块作用域中勾选**需要过检测的应用**（不要勾选 GKD 本体和 LSPosed，模块会自动跳过它们）；
3. 强制停止目标应用后重新打开；
4. 打开模块的"GKD 隐身"应用可调整配置（总开关、strict 模式、隐藏目标、排除应用），配置约 3 秒内热生效，改完若没效果就重启目标应用。

> GKD 建议使用 **Root 模式**授权（LSPosed 环境下本来就有 root），完全绕开 Shizuku/ADB 调试这个额外的检测面。

## 默认隐藏目标

默认隐藏 GKD（包名 `li.songe.gkd`，按包名前缀匹配，覆盖其全部无障碍服务组件），且始终包含、不受配置影响。

如需同时隐藏其他自动化工具（李跳跳、UZAutomate 等），在配置界面"额外要隐藏的服务"里每行添加一个包名或组件 ID（如 `com.example.app/com.example.app.MyService`）。

模块自动跳过的进程：GKD 本体、模块自身、系统设置、系统界面（SystemUI）、system_server，避免影响这些场景下无障碍状态的正常显示。

## 已知局限

- **其他无障碍服务**：如果除了 GKD 还开启了别的无障碍服务（如屏幕朗读类工具），非 strict 模式下应用仍能检测到它们；strict 模式会整体伪装成无障碍关闭，但那样目标应用里也不能用其他无障碍功能。可用"排除的应用"精细化处理。
- **本模块自身与 LSPosed 的检测**：有些应用直接检测 LSPosed/.root 环境，这属于 [Shamiko](https://github.com/LSPosed/LSPosed.github.io/releases)（隐藏 root + DenyList）的领域，不在本模块范围内。
- **包列表检测**：应用若检测"GKD 是否安装"，需配合 Hide My Applist 类模块。
- 无障碍检测手段可能随系统版本/厂商定制变化，遇到未覆盖的检测点欢迎提 issue 补充 Hook。

## 构建

- 本地：`gradle assembleDebug`（需 JDK 17+ 与 Android SDK 34）
- CI：推送到 GitHub 后 Actions 自动构建，产物在 Actions Artifacts

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`（debug 签名，可直接被 LSPosed 使用）。

## 致谢与参考

- [AoEiuV020/IAmNotDisabled](https://github.com/AoEiuV020/IAmNotDisabled) —— 同思路的先行项目（伪装无障碍未启用），本项目在其基础上扩展了 Settings.Secure 层拦截与可配置化
- [gkd-kit/gkd](https://github.com/gkd-kit/gkd) —— 被保护的开屏广告跳过工具

本项目仅供个人设备上合法软件的正常使用场景，请勿用于违反目标应用服务条款以外的用途。
