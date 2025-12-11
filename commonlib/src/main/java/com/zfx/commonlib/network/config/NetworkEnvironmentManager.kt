package com.zfx.commonlib.network.config

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * 网络环境管理器
 * 职责单一：管理多环境配置和当前环境切换
 * 
 * 支持：
 * 1. 配置多个环境的参数
 * 2. 运行时切换环境
 * 3. 持久化当前环境选择
 */
class NetworkEnvironmentManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: NetworkEnvironmentManager? = null
        
        private const val PREFS_NAME = "network_environment_prefs"
        private const val KEY_CURRENT_ENVIRONMENT = "current_environment"
        
        /**
         * 获取单例实例
         */
        fun getInstance(): NetworkEnvironmentManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkEnvironmentManager().also { INSTANCE = it }
            }
        }
    }
    
    // 环境配置映射
    private val environmentConfigs = mutableMapOf<NetworkEnvironment, EnvironmentConfig>()
    
    // 当前环境
    private var currentEnvironment: NetworkEnvironment = NetworkEnvironment.DEVELOPMENT
    
    
    // SharedPreferences（可选，用于持久化）
    private var sharedPreferences: SharedPreferences? = null
    
    /**
     * 初始化环境管理器
     * @param sharedPreferences 用于持久化环境选择的 SharedPreferences（可选）
     */
    fun init(sharedPreferences: SharedPreferences? = null) {
        this.sharedPreferences = sharedPreferences
        
        // 从 SharedPreferences 恢复上次选择的环境
        sharedPreferences?.getString(KEY_CURRENT_ENVIRONMENT, null)?.let { envName ->
            try {
                currentEnvironment = NetworkEnvironment.valueOf(envName)
            } catch (e: IllegalArgumentException) {
                // 如果保存的环境不存在，使用默认环境
                currentEnvironment = NetworkEnvironment.DEVELOPMENT
            }
        }
    }
    
    /**
     * 配置开发环境
     */
    fun configureDevelopment(config: EnvironmentConfig) {
        if (config.environment != NetworkEnvironment.DEVELOPMENT) {
            throw IllegalArgumentException("配置的环境类型必须为 DEVELOPMENT")
        }
        environmentConfigs[NetworkEnvironment.DEVELOPMENT] = config
    }
    
    /**
     * 配置预发布环境
     */
    fun configurePreRelease(config: EnvironmentConfig) {
        if (config.environment != NetworkEnvironment.PRE_RELEASE) {
            throw IllegalArgumentException("配置的环境类型必须为 PRE_RELEASE")
        }
        environmentConfigs[NetworkEnvironment.PRE_RELEASE] = config
    }
    
    /**
     * 配置生产环境
     */
    fun configureProduction(config: EnvironmentConfig) {
        if (config.environment != NetworkEnvironment.PRODUCTION) {
            throw IllegalArgumentException("配置的环境类型必须为 PRODUCTION")
        }
        environmentConfigs[NetworkEnvironment.PRODUCTION] = config
    }
    
    /**
     * 配置环境（通用方法）
     */
    fun configureEnvironment(config: EnvironmentConfig) {
        environmentConfigs[config.environment] = config
    }
    
    /**
     * 批量配置环境
     */
    fun configureEnvironments(vararg configs: EnvironmentConfig) {
        configs.forEach { config ->
            environmentConfigs[config.environment] = config
        }
    }
    
    /**
     * 切换环境
     * @param environment 目标环境
     * @return 是否切换成功
     */
    fun switchEnvironment(environment: NetworkEnvironment): Boolean {
        if (!environmentConfigs.containsKey(environment)) {
            return false
        }
        
        currentEnvironment = environment
        
        // 持久化当前环境选择
        sharedPreferences?.edit {
            putString(KEY_CURRENT_ENVIRONMENT, environment.name)
        }
        
        return true
    }
    
    /**
     * 获取当前环境
     */
    fun getCurrentEnvironment(): NetworkEnvironment {
        return currentEnvironment
    }
    
    /**
     * 获取当前环境的配置
     */
    fun getCurrentEnvironmentConfig(): EnvironmentConfig? {
        return environmentConfigs[currentEnvironment]
    }
    
    /**
     * 获取指定环境的配置
     */
    fun getEnvironmentConfig(environment: NetworkEnvironment): EnvironmentConfig? {
        return environmentConfigs[environment]
    }
    
    /**
     * 获取当前环境的 NetworkConfig
     */
    fun getCurrentNetworkConfig(): NetworkConfig? {
        return getCurrentEnvironmentConfig()?.toNetworkConfig()
    }
    
    /**
     * 检查环境是否已配置
     */
    fun isEnvironmentConfigured(environment: NetworkEnvironment): Boolean {
        return environmentConfigs.containsKey(environment)
    }
    
    /**
     * 获取所有已配置的环境
     */
    fun getConfiguredEnvironments(): Set<NetworkEnvironment> {
        return environmentConfigs.keys.toSet()
    }
    
    /**
     * 清除所有环境配置
     */
    fun clearAllConfigurations() {
        environmentConfigs.clear()
    }
    
    /**
     * 重置环境管理器（用于测试）
     */
    fun reset() {
        environmentConfigs.clear()
        currentEnvironment = NetworkEnvironment.DEVELOPMENT
        sharedPreferences = null
        INSTANCE = null
    }
}

