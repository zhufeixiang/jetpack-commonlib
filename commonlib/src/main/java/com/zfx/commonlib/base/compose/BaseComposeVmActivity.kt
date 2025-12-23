package com.zfx.commonlib.base.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import com.zfx.commonlib.base.viewmodel.BaseViewModel
import com.zfx.commonlib.ext.getVmClazz

/**
 * Compose + MVVM Activity 基类
 * 
 * MVVM 架构特点：
 * - 双向数据绑定，多个独立的 StateFlow
 * - ViewModel 管理业务逻辑和状态
 * - 使用 Compose 渲染 UI
 * 
 * 使用示例：
 * ```kotlin
 * class HomeActivity : BaseComposeVmActivity<HomeViewModel>() {
 *     override fun Render(viewModel: HomeViewModel) {
 *         val bannerList by viewModel.bannerList.collectAsState()
 *         val articleList by viewModel.articleList.collectAsState()
 *         
 *         HomeScreen(
 *             bannerList = bannerList,
 *             articleList = articleList
 *         )
 *     }
 * }
 * ```
 * 
 * @param VM ViewModel 类型，必须继承 BaseViewModel
 */
abstract class BaseComposeVmActivity<VM : BaseViewModel> : ComponentActivity() {

    protected lateinit var viewModel: VM
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel()
        setContent {
            Render(viewModel = viewModel)
        }
    }
    
    /**
     * 创建 ViewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this)[getVmClazz(this)]
    }

    /**
     * 渲染 UI
     * 
     * 子类必须实现此方法来构建 UI
     * 在此方法中可以使用 viewModel 的 StateFlow 来收集状态
     * 
     * @param viewModel ViewModel 实例，用于访问状态和方法
     */
    @Composable
    protected abstract fun Render(viewModel: VM)
}

