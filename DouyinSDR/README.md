# DouyinSDR —— 只针对抖音强制 SDR 的 LSPosed 模块

## 云端编译（GitHub Actions，无需本机 Android Studio）

1. 在 GitHub 新建一个仓库（Public 或 Private 都行，建议 Private），比如 `DouyinSDR`。
2. 在终端进入本工程目录，依次执行（把 `<你的用户名>` 和仓库名换成自己的）：
   ```bash
   git init
   git add .
   git commit -m "init DouyinSDR"
   git branch -M main
   git remote add origin https://github.com/<你的用户名>/DouyinSDR.git
   git push -u origin main
   ```
3. 到仓库页面 **Actions** 标签，等待 `Build DouyinSDR APK` 跑完（首次约 3~5 分钟）。
4. 跑完后点击该工作流，最下方 **Artifacts** 里下载 `DouyinSDR-apk`，解压得到 `app-release.apk`。

## 手机侧安装激活
1. 安装 `app-release.apk`。
2. 打开 LSPosed 管理，勾选模块，作用域勾选 `com.ss.android.ugc.aweme` 和 `android`。
3. 重启系统，永久生效。

## 局限性
本模块只 Hook Java 层 `Display.isHdrDisplay()` / `isHdrSdrRatioSupported()`。
抖音新版部分 HDR 短视频走 native 硬解码，在 SurfaceFlinger 直接输出 HDR，
Java 层 Hook 拦不住，这类源仍会触发峰值亮度——这是该方案的固有边界。
