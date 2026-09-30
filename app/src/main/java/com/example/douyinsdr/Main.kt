package com.example.douyinsdr

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * DouyinSDR —— 极简 LSPosed 模块
 *
 * 目标：只针对抖音（com.ss.android.ugc.aweme）强制 SDR，
 *       让 Display.isHdrDisplay() / isHdrSdrRatioSupported() 对抖音返回 false，
 *       从而不输出 HDR、不触发屏幕峰值亮度；其他 APP 保持系统原生结果，完全不受影响。
 *
 * ⚠️ 已知短板（native 硬解）：
 *   本模块只 Hook Java 层 API。抖音部分新版 HDR 短视频走底层 native 硬解码，
 *   直接在 SurfaceFlinger 侧以 HDR Surface 输出，Java 层 Hook 拦不住，
 *   这类源依然会触发 HDR 峰值亮度。这是该方案的固有边界。
 */
class Main : IXposedHookLoadPackage {

    companion object {
        // 抖音包名
        private const val TARGET_PACKAGE = "com.ss.android.ugc.aweme"
        private const val DISPLAY_CLASS = "android.view.Display"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 只处理抖音进程与系统框架进程；其余一律跳过
        if (lpparam.packageName != TARGET_PACKAGE && lpparam.packageName != "android") return

        val displayClass = try {
            XposedHelpers.findClass(DISPLAY_CLASS, lpparam.classLoader)
        } catch (t: Throwable) {
            return // 找不到框架类，直接放弃
        }

        // API 29+：isHdrDisplay() -> boolean
        hookNoArgBoolean(displayClass, lpparam, "isHdrDisplay")
        // Android 12 / API 31+：isHdrSdrRatioSupported() -> boolean
        hookNoArgBoolean(displayClass, lpparam, "isHdrSdrRatioSupported")
    }

    /**
     * 把 Display 的某个无参 boolean 方法在「抖音进程」内强制返回 false，
     * 其他进程/APP 保持原生结果不变。
     */
    private fun hookNoArgBoolean(
        displayClass: Class<*>,
        lpparam: XC_LoadPackage.LoadPackageParam,
        method: String
    ) {
        try {
            XposedHelpers.findAndHookMethod(
                displayClass,
                method,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // 仅当调用方是抖音时才改写返回值
                        if (lpparam.packageName == TARGET_PACKAGE) {
                            param.result = false
                        }
                    }
                }
            )
        } catch (_: Throwable) {
            // 该 API 在低版本系统上不存在，静默跳过，不影响其余 Hook
        }
    }
}
