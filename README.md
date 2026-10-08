# VIP影视 Android

基于开源项目 [88lin/video_vip](https://github.com/88lin/video_vip) 的能力，实现的 **Android 双端视频客户端**（手机版 + Android TV 版）。核心对接与浏览器脚本相同的 **无损云资源库 / MacCMS 接口** 与 **第三方解析源列表**，提供接近主流视频 App 的浏览与播放体验。

> **说明**：解析接口均来自第三方公开网络，质量与可用性不受本仓库控制。项目仅供学习交流，请尊重版权并支持正版。上游脚本采用 GPL-3.0，本仓库衍生代码同样遵循 GPL-3.0。

## 功能概览

| 模块 | 手机版 | TV 版 |
|------|--------|-------|
| 首页分类 / 今日更新 | ✅ | ✅ |
| 搜索（联想 + 结果） | ✅ | 简化（最新列表，可扩展 Leanback 搜索） |
| 详情 / 选集 | ✅ | ✅ |
| HLS 播放（Media3 ExoPlayer） | ✅ | ✅ |
| 观看历史 / 收藏 | ✅ | ✅ |
| 粘贴 VIP 页链接解析播放 | ✅ | 设置中可选解析源（链接页以手机为主） |
| 解析源切换（与 userscript 同源列表） | ✅ | ✅ |

## 工程结构

```
android/
  app/          # UI、播放器、WebView 解析页（productFlavors: mobile / tv）
  core/         # 接口、仓库、Room 本地库、解析源配置
```

## 构建与安装

环境：JDK 17+、Android SDK 34。

```bash
cd android
./gradlew :app:assembleMobileDebug   # 手机 Debug APK
./gradlew :app:assembleTvDebug       # TV Debug APK
```

产物路径：

- `android/app/build/outputs/apk/mobile/debug/app-mobile-debug.apk`
- `android/app/build/outputs/apk/tv/debug/app-tv-debug.apk`

TV 包使用 `LEANBACK_LAUNCHER`，需在 Android TV 或 TV 模拟器上安装。

## 与 video_vip 脚本的对应关系

| 脚本能力 | Android 实现 |
|----------|----------------|
| 无损云 suggest + detail API | `VodRepository` + `wsyzy.cc` / `api.wsyzy.net` |
| 选集 `vod_play_url` 解析 | `EpisodeParser` |
| 多解析源 iframe | `ParseWebActivity` + `ParseSources` |
| 自动匹配剧名 / 选集 | 应用内直接搜索选片（链接模式粘贴 URL） |

## 免责声明

本工具不拥有第三方解析接口与视频内容版权。请勿用于商业用途；试用后请购买各平台正版会员支持创作者。
