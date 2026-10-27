# Phase 2 路线：GKD 引擎进程内运行（真正"无感知"的 LSP 版本）

> 现状（Phase 1，已完成）：GKD 保持无障碍开启，GKDHide 模块在目标应用里隐藏它的存在。
> Phase 2 的目标更进一步：**完全不注册无障碍服务**，把 GKD 的规则引擎以 Xposed 模块形式跑进目标应用进程内部——从系统设置到任何检测面，都不存在"无障碍服务"这一实体。

## 架构设想

```
┌─ 目标应用进程（LSPosed 注入）──────────────────────┐
│  HookEntry (Xposed)                                │
│   ├─ 触发器：Activity.onResume / TextView.setText  │
│   │         / ViewGroup.addView（替代无障碍事件）    │
│   ├─ 快照器：WindowManagerGlobal 取根 View 树       │
│   │         → 适配成 GKD selector 的 Node 抽象      │
│   ├─ 引擎：复用 gkd-kit/selector 规则匹配 + 订阅JSON │
│   └─ 执行器：View.performClick() / 合成 MotionEvent │
│              经 dispatchTouchEvent 派发             │
└────────────────────────────────────────────────────┘
```

## 关键技术点与难点

| 环节 | 无障碍版 GKD | 进程内版设想 | 难度 |
|---|---|---|---|
| 界面感知 | AccessibilityEvent 回调 | Hook `Activity.onResume`、`TextView.setText`、`ViewGroup.addView` 等作为"内容变化"信号 | 中 |
| 节点树 | 系统跨进程返回的 a11y 节点树 | `WindowManagerGlobal.getInstance().mViews` 拿根 View，按 selector 的 Node 接口适配 | 中 |
| 规则匹配 | gkd-kit/selector 引擎 | 直接复用（纯 Kotlin 库，与节点实现解耦） | 低 |
| 点击 | `AccessibilityService.gestureDispatch`（坐标级） | 有语义节点用 `performClick()`；坐标级用合成 `MotionEvent` 走 `dispatchTouchEvent` 派发到根 View | 中 |
| 订阅规则 | GKD 应用内管理 | 模块内置/经 LSPosed 数据目录读取同一份订阅 JSON | 低 |

**最大的硬伤——内容可见性差异**：

- 原生 View / Compose（有 semantics）/ WebView：进程内 View 树可见，WebView 的 DOM 文本**不可见**（它是渲染层，View 树里只有 `WebView` 本身）；而无障碍树反而能看到 WebView 的虚拟节点。GKD 很多电商/资讯类规则依赖 WebView 文本，进程内版会失效。
- Flutter / Unity：View 树几乎空白，文本在无障碍语义层（FlutterPlatformView 桥接），进程内版失效。
- 结论：**进程内版无法完全替代无障碍版**，两者是互补关系。

## 可行的折中方案（按性价比排序）

1. **Phase 1（已完成，推荐）**：GKD 无障碍保持开启 + 本模块隐藏。覆盖率 100%，维护成本几乎为零，GKD 官方更新不受影响。
2. **GKD 魔改版内置模块**：fork GKD 源码，把 HideHook 作为 Xposed 入口打进 GKD 本体（一个 APK 既是工具又是模块）。省掉单独安装模块这一步，代价是每次 GKD 更新要重新合并 fork。
3. **进程内引擎做"补充规则集"**：只处理无障碍覆盖不到/延迟高的场景（如进程内直接 hook 广告 SDK 的 `Activity` 生命周期跳过开屏页），不追求替代 GKD。技术上可行、工作量可控，适合作为独立小模块。
4. **完整 Phase 2**：工作量以周计，且 WebView/Flutter 场景仍需回退到无障碍，不建议作为首选。

## 结论

对"过检测环境"这个目标，Phase 1 的隐藏方案已经覆盖所有常见检测面且不损失 GKD 任何能力。Phase 2 仅在"目标环境强制要求无障碍功能完全关闭"这类极端场景才有必要，届时建议按方案 3 做窄场景补充，而不是整体重写。
