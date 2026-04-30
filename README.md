# 产品雷达图对比工具 (Radar Chart App)

基于 Android 的产品六维度雷达图对比工具，用于真空泵产品性能可视化对比。

## 功能特性

- ✅ 六维度雷达图展示（节能、级别、抗腐蚀、低温性能、高温性能、抗压降）
- ✅ 多产品选择对比
- ✅ 搜索筛选
- ✅ 自动计算胜出者
- ✅ 彩虹色/线型双模式
- ✅ 内置产品数据库

## 内置数据

来自 `对比数据库Rev2.xlsx`，包含 45 个 Ebara/ESA 系列真空泵产品数据。

## 构建

使用 GitHub Actions 云端构建 APK：

1. Fork 本仓库
2. Push 到 main 分支
3. GitHub Actions 自动构建
4. 在 Actions 页面下载 APK

## 技术栈

- Kotlin
- Android SDK 34
- MPAndroidChart v3.1.0
- AndroidX