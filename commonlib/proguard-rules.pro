# ============================================
# CommonLib 混淆规则
# ============================================
# 注意：这是 Android Library 的混淆规则
# 库本身不会被混淆（minifyEnabled false），但这些规则会在使用库的项目进行混淆时自动合并
# 
# 库的混淆规则应该只包含：
# 1. 库中使用的第三方库的混淆规则
# 2. 库中公共 API 的保护规则
# 3. 基本的属性保留规则
# 
# 其他规则（如 Activity、Fragment、资源混淆等）应该在使用库的项目中配置

# ============================================
# 基本属性保留（必需）
# ============================================
# 保留注解，用于反射和注解处理器
-keepattributes *Annotation*,InnerClasses
# 保留泛型信息，用于 Gson、Retrofit 等库的类型转换
-keepattributes Signature
# 保留异常信息，用于调试
-keepattributes Exceptions
# 保留代码行号，方便异常信息追踪
-keepattributes SourceFile,LineNumberTable

# ============================================
# 库中使用的第三方库混淆规则
# ============================================

# ImmersionBar：沉浸式状态栏库
-keep class com.gyf.immersionbar.* {*;}
-dontwarn com.gyf.immersionbar.**

# Gson：JSON 序列化/反序列化库
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# 保留使用 Gson 序列化的数据类（由使用库的项目配置具体的数据类）

# OkHttp3：HTTP 客户端
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

# Retrofit2：HTTP 客户端框架
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
# Retrofit 需要保留泛型和异常信息（已在上面配置）

# EventBus：事件总线框架
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# ============================================
# ViewBinding & DataBinding（库启用了这些功能）
# ============================================
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater);
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static * bind(android.view.View);
}

# ============================================
# 库公共 API 保护（必需）
# ============================================
# 保护库的公共 API，防止在使用库的项目进行混淆时被混淆
# 这些基类和接口是供项目继承和使用的，必须保持不被混淆

# Base 模块：保护所有基类和接口
# 包括 BaseViewModel、BaseVmActivity、BaseVmFragment、BaseVbActivity 等
-keep class com.zfx.commonlib.base.** { *; }

# Network 模块：保护网络相关的公共 API
# 包括 BaseResponse、IBaseResponse、BaseRepository、NetworkManager 等
-keep class com.zfx.commonlib.network.** { *; }

# MVI 模块：保护 MVI 相关的公共 API
# 包括 MviViewModel、MviActivity、MviFragment、ViewIntent、ViewState 等
-keep class com.zfx.commonlib.mvi.** { *; }

# 扩展函数和工具类：保护公共工具类
-keep class com.zfx.commonlib.ext.** { *; }
-keep class com.zfx.commonlib.util.** { *; }

# ============================================
# 注意：以下规则应该在使用库的项目中配置，而不是在库中
# ============================================
# - Activity、Fragment、Service 等 Android 组件的混淆规则
# - 资源混淆规则（-keep class **.R$* {*;}）
# - Log 删除规则（-assumenosideeffects class android.util.Log）
# - 优化相关规则（-optimizationpasses、-dontshrink、-dontoptimize 等）
# - 自定义 View 的混淆规则
# - Parcelable、Serializable 的混淆规则（应该在使用库的项目中配置）
