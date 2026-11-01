# Starry Notes

一款基于 **Material Design 3** 的本地 Markdown 笔记应用。所有数据保存在设备本地，无网络、无广告、无统计 SDK。

- 应用名：Starry Notes
- 包名：`com.starryweaver.notes`
- 当前版本：1.8（versionCode 9）
- 最低支持：Android 8.0（API 26）

---

## 功能

### 笔记
- 新建 / 编辑 / 删除笔记，输入自动保存（防抖落盘），离开页面时刷新保存
- 标题、`#标签`、置顶
- 搜索：标题、正文、标签
- 标签筛选、排序（最近更新 / 创建时间 / 标题）
- 单栏 / 双栏卡片布局，可切换并记忆
- 卡片菜单：置顶、分享、导出 Markdown、移到回收站
- 新建后若标题与正文皆为空，返回时自动回收该空笔记

### Markdown
- 编辑 / 分栏实时预览 / 纯预览三种模式
- 工具栏：H1–H3、粗体、斜体、删除线、行内代码、代码块、引用、无序列表、有序列表、待办、链接、分隔线
- 按选区包裹或按行加前缀，重复点击可取消
- 正文为全屏无边框输入，工具栏常驻在输入法上方
- 自研轻量渲染：标题、段落、围栏代码块、引用、列表、分隔线，行内粗体/斜体/删除线/行内代码/链接

### 回收站
- 软删除、恢复、彻底删除、清空

### 导入导出
- 全量导出为 JSON，导入时兼容完整备份与裸数组，按 id 合并（保留较新者）
- 单条导出 Markdown（带 frontmatter：title / tags / created / updated）
- 系统分享单条笔记，并可从其他应用接收文本直接新建笔记
- 全部通过系统文件选择器完成，不申请存储权限

### 外观
- 深浅色：跟随系统 / 浅色 / 深色（三栏分段按钮切换）
- 6 套内置主题色 + 自定义颜色（RGB 滑块，用 HSL 从种子色推导整套强调色）
- 动态取色（Android 12+，开启时优先于内置主题色）
- 日夜切换时以触点为圆心做圆形扩散揭示

### 其他
- 首次启动的隐私政策确认（不可跳过）
- 多页开屏引导
- 关于页：技术栈、开发者、开源许可（附 GitHub 链接）

---

## 技术栈

| 项 | 版本 / 说明 |
| --- | --- |
| 语言 | Kotlin 2.4.20 |
| 构建 | Gradle 9.7.1 + AGP 9.4.1 |
| UI | Jetpack Compose（BOM 2026.09.00）+ Material 3 |
| 导航 | Navigation Compose 2.10.0 |
| 序列化 | kotlinx.serialization 1.7.x/1.9.x（JSON） |
| 存储 | 应用私有目录 JSON 文件 + SharedPreferences |
| 架构 | MVVM + Repository，手动依赖注入 |
| SDK | compileSdk / targetSdk 37，minSdk 26 |

无第三方 UI / 网络 / 统计 / 广告 SDK。

---

## 构建

环境要求：

- JDK 17 或更高
- Android SDK Platform 37、Build-Tools 37.0.0

```bash
# 1. 配置 SDK 路径（或让 Android Studio 自动生成）
echo "sdk.dir=/path/to/android/sdk" > local.properties

# 2. 编译 Debug APK
./gradlew :app:assembleDebug

# 产物：app/build/outputs/apk/debug/app-debug.apk

# 3. 运行单元测试
./gradlew :app:testDebugUnitTest
```

首次构建需下载依赖，耗时较长；之后为增量构建。

---

## 项目结构

```
app/src/main/java/com/starryweaver/notes/
├── MainActivity.kt              # 入口，隐私/引导门禁与主题包装
├── NotesApplication.kt          # 应用级单例（仓储 + 长生命周期作用域）
├── data/                        # 数据层
│   ├── Note.kt                  # 笔记模型（kotlinx.serialization）
│   ├── NoteRepository.kt        # JSON 文件持久化、增删改查、回收站
│   ├── SettingsRepository.kt    # 设置项（主题、排序、布局、引导/隐私标记）
│   ├── ImportExport.kt          # JSON / Markdown 导入导出（纯逻辑，可单测）
│   └── WelcomeNote.kt           # 首次安装种入的使用说明笔记
├── util/
│   ├── MarkdownParser.kt        # 块级 Markdown 解析（纯逻辑，可单测）
│   └── MarkdownEdit.kt          # 工具栏插入/包裹（纯逻辑，可单测）
└── ui/
    ├── theme/                   # 配色、主题、圆形揭示动画
    ├── components/              # 笔记卡片、Markdown 渲染
    ├── notes/                   # 笔记列表（单/双栏、搜索、筛选、排序）
    ├── edit/                    # 编辑器（三态、工具栏、全屏输入）
    ├── trash/                   # 回收站
    ├── settings/                # 设置页与外观页
    ├── about/                   # 关于页
    ├── onboarding/              # 开屏引导
    ├── privacy/                 # 隐私政策
    └── navigation/              # 路由与底部导航
```

测试：`app/src/test/` 下 26 个 JVM 单元测试，覆盖导入导出、Markdown 解析与编辑操作、路由、布局枚举。

---

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源，Copyright 2026 Nx430。

依据 Apache-2.0 第 4 条，再分发时请保留本许可证副本；本项目未附带 NOTICE 文件。

依赖的开源组件（均为 Apache License 2.0）：

- AndroidX（Core、Lifecycle、Activity、Navigation）
- Jetpack Compose
- Material Components for Android
- Kotlin 标准库
- kotlinx.serialization
