package com.zfx.commonlib.base.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable

/**
 * Compose Activity 基类（无 ViewModel）
 * 
 * 适用于：
 * - 纯 UI 展示页面，不需要复杂的状态管理
 * - 简单的静态页面
 * - 不需要网络请求或数据处理的页面
 * 
 * 使用示例：
 * ```kotlin
 * class SplashActivity : BaseComposeActivity() {
 *     override fun Render() {
 *         SplashScreen(
 *             onFinish = { finish() }
 *         )
 *     }
 * }
 * ```
 */
abstract class BaseComposeActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Render()
        }
    }

    /**
     * 渲染 UI
     * 
     * 子类必须实现此方法来构建 UI
     * 此方法中可以直接使用 Compose 组件，无需 ViewModel
     * 
     * 如果需要状态管理，可以使用：
     * - remember { mutableStateOf() } 用于本地状态
     * - LaunchedEffect 用于副作用
     */
    @Composable
    protected abstract fun Render()
}

