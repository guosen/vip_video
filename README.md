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

**预编译手机包（GitHub Releases）**：[VIPVideo-mobile-debug.apk](https://github.com/guosen/vip_video/releases/download/v1.0.0-debug/VIPVideo-mobile-debug.apk)

TV 包使用 `LEANBACK_LAUNCHER`，需在 Android TV 或 TV 模拟器上安装。

## 内容数据从哪来？

与 [88lin/video_vip](https://github.com/88lin/video_vip) 脚本里「无损云」一致，全部走 **第三方 MacCMS 采集 JSON**，不是爱奇艺/腾讯官方 API。

| 用途 | 接口 | 说明 |
|------|------|------|
| 首页列表 / 分类 / 详情选集 | `https://api.wsyzy.net/api.php/provide/vod/` | `ac=list`（分页 `pg`，分类 `t`） / `ac=detail&ids=` |
| 搜索联想 | `https://wsyzy.cc/index.php/ajax/suggest?mid=1&wd=` | 支持 `limit=50`；**`list` 的 `wd` 全文搜索在该源已关闭** |
| 播放地址 | 详情里的 `vod_play_url` | 解析为 m3u8 后用 ExoPlayer 播放 |

库内总量约 **12 万+** 条（`ac=list` 的 `total`），但接口 **每页最多 20 条**。旧版 App 首页只拉各分类第 1 页，且「今日更新」用了 `h=24`（仅近 24 小时），所以看起来很少。当前版已改为首页多页聚合（最近更新约 80 条、每分类约 60 条），分类页支持滚动加载更多；搜索单次最多约 50 条联想结果。

## 与 video_vip 脚本的对应关系

| 脚本能力 | Android 实现 |
|----------|----------------|
| 无损云 suggest + detail API | `VodRepository` + `wsyzy.cc` / `api.wsyzy.net` |
| 选集 `vod_play_url` 解析 | `EpisodeParser` |
| 多解析源 iframe | `ParseWebActivity` + `ParseSources` |
| 自动匹配剧名 / 选集 | 应用内直接搜索选片（链接模式粘贴 URL） |

## 免责声明

本工具不拥有第三方解析接口与视频内容版权。请勿用于商业用途；试用后请购买各平台正版会员支持创作者。
