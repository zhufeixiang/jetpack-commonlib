# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

  #############################################
  #
  # 对于一些基本指令的添加
  #
  #############################################
  # 设置混淆的压缩比率 0 ~ 7
  -optimizationpasses 5
  # 混淆时不使用大小写混合，混淆后的类名为小写
  -dontusemixedcaseclassnames
  # 指定不去忽略非公共库的类
  -dontskipnonpubliclibraryclasses
  # 指定不去忽略非公共库的成员
  -dontskipnonpubliclibraryclassmembers
  # 混淆时不做预校验
  -dontpreverify
  # 混淆时不记录日志
  -verbose
  # 代码优化
  -dontshrink
  # 不优化输入的类文件
  -dontoptimize
  # 保留注解不混淆
  -keepattributes *Annotation*,InnerClasses
  # 避免混淆泛型
  -keepattributes Signature
  # 保留代码行号，方便异常信息的追踪
  -keepattributes SourceFile,LineNumberTable
  # 混淆采用的算法
  -optimizations !code/simplification/cast,!field/*,!class/merging/*

  # dump.txt文件列出apk包内所有class的内部结构
  -dump class_files.txt
  # seeds.txt文件列出未混淆的类和成员
  -printseeds seeds.txt
  # usage.txt文件列出从apk中删除的代码
  -printusage unused.txt
  # mapping.txt文件列出混淆前后的映射
  # -printmapping mapping.txt


  #############################################
  #
  # Android开发中一些需要保留的公共部分
  #
  #############################################
  #不需混淆的Android类
  -keep public class * extends android.app.Fragment
  -keep public class * extends android.app.Activity
  -keep public class * extends android.app.Application
  -keep public class * extends android.app.Service
  -keep public class * extends android.content.BroadcastReceiver
  -keep public class * extends android.preference.Preference
  -keep public class * extends android.content.ContentProvider
  -keep public class * extends android.app.backup.BackupAgentHelper
  -keep public class * extends android.preference.Preference
  -keep public class * extends android.view.View
  -keep public class com.android.vending.licensing.ILicensingService

  #support下的所有类及其内部类
  -keep class android.support.** {*;}
  -dontwarn android.support.**
  -keep interface android.support.** { *; }

  #androidx
  -keep class androidx.** {*;}
  -keep interface androidx.** {*;}
  -keep public class * extends androidx.**
  -dontwarn androidx.**

  #support v4/7库
  -keep public class * extends android.support.v4.**
  -keep public class * extends android.support.v7.**
  -keep public class * extends androidx.annotation.**

  #避免混淆自定义控件类的 get/set 方法和构造函数
  -keep public class * extends android.view.View{
      *** get*();
      void set*(***);
      public <init>(android.content.Context);
      public <init>(android.content.Context, android.util.AttributeSet);
      public <init>(android.content.Context, android.util.AttributeSet, int);
  }
  #关闭 Log日志
  -assumenosideeffects class android.util.Log {
      public static boolean isLoggable(java.lang.String, int);
      public static int v(...);
      public static int i(...);
      public static int w(...);
      public static int d(...);
      public static int e(...);
  }
  #避免资源混淆
  -keep class **.R$* {*;}
  #避免layout中onclick方法（android:onclick="onClick"）混淆
  -keepclassmembers class * extends android.app.Activity{
      public void *(android.view.View);
  }

  # 保留本地native方法不被混淆
  -keepclasseswithmembernames class * {
      native <methods>;
  }

  # 保留在Activity中的方法参数是view的方法，
  # 这样以来我们在layout中写的onClick就不会被影响
  -keepclassmembers class * extends android.app.Activity{
      public void *(android.view.View);
  }

  # 保留枚举类不被混淆
  -keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
  }

  # 保留我们自定义控件（继承自View）不被混淆
  -keep public class * extends android.view.View{
      *** get*();
      void set*(***);
      public <init>(android.content.Context);
      public <init>(android.content.Context, android.util.AttributeSet);
      public <init>(android.content.Context, android.util.AttributeSet, int);
  }

  # 保留Parcelable序列化类不被混淆
  -keep class * implements android.os.Parcelable {
      public static final android.os.Parcelable$Creator *;
  }

  # 保留Serializable序列化的类不被混淆
  -keepclassmembers class * implements java.io.Serializable {
      static final long serialVersionUID;
      private static final java.io.ObjectStreamField[] serialPersistentFields;
      !static !transient <fields>;
      !private <fields>;
      !private <methods>;
      private void writeObject(java.io.ObjectOutputStream);
      private void readObject(java.io.ObjectInputStream);
      java.lang.Object writeReplace();
      java.lang.Object readResolve();
  }

  # 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
  -keepclassmembers class * {
      void *(**On*Event);
      void *(**On*Listener);
  }


#   immersionbar
    -keep class com.gyf.immersionbar.* {*;}
    -dontwarn com.gyf.immersionbar.**

    #   xpopup
       -dontwarn com.lxj.xpopup.widget.**
       -keep class com.lxj.xpopup.widget.**{*;}


# gson混淆配置
  -keep class com.google.gson.** {*;}
  -keep class com.google.**{*;}
  -keep class sun.misc.Unsafe { *; }
  -keep class com.google.gson.stream.** { *; }
  -keep class com.google.gson.examples.android.model.** { *; }

  # OkHttp3混淆配置
  -dontwarn com.squareup.okhttp3.**
  -keep class com.squareup.okhttp3.** { *;}
  -dontwarn okio.**

  # Retrofit2混淆配置
  -dontwarn retrofit2.**
  -keep class retrofit2.** { *; }
  -keepattributes Signature
  -keepattributes Exceptions


  # eventbus混淆配置
                  -keepattributes *Annotation*
                  -keepclassmembers class * {
                      @org.greenrobot.eventbus.Subscribe <methods>;
                  }
                  -keep enum org.greenrobot.eventbus.ThreadMode { *; }

   #   xpopup
         -dontwarn com.lxj.xpopup.widget.**
         -keep class com.lxj.xpopup.widget.**{*;}

################ ViewBinding & DataBinding ###############
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
  public static * inflate(android.view.LayoutInflater);
  public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
  public static * bind(android.view.View);
}