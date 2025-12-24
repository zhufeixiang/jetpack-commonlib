package com.zfx.commonlib.ext.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 类似 Flutter 的 Center 组件，用于将子组件居中显示
 * 
 * 使用示例：
 * ```kotlin
 * Center {
 *     Text("居中文本")
 * }
 * ```
 * 
 * 或者带修饰符：
 * ```kotlin
 * Center(modifier = Modifier.fillMaxSize()) {
 *     Text("居中文本")
 * }
 * ```
 * 
 * @param modifier 修饰符，默认使用 fillMaxSize() 填充整个可用空间
 * @param content 子组件内容
 * 
 * @author zhufeixiang
 * @date 2025/01/XX
 */
@Composable
fun Center(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

