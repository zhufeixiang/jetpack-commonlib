package com.zfx.commonlib.network.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import android.text.Html

/**
 * HTML 实体字符串类型适配器
 * 用于自动解码 JSON 字符串中的 HTML 实体编码（如 &mdash; → ——）
 * 
 * 支持的常见 HTML 实体：
 * - &mdash; → ——（长破折号）
 * - &ndash; → –（短破折号）
 * - &nbsp; → （不间断空格）
 * - &amp; → &
 * - &lt; → <
 * - &gt; → >
 * - &quot; → "
 * - &apos; → '
 * 
 * 使用方式：
 * ```kotlin
 * val gson = GsonBuilder()
 *     .registerTypeAdapter(String::class.java, HtmlEntityStringTypeAdapter())
 *     .create()
 * ```
 */
class HtmlEntityStringTypeAdapter : TypeAdapter<String>() {
    
    override fun write(out: JsonWriter, value: String?) {
        out.value(value)
    }
    
    override fun read(`in`: JsonReader): String {
        val value = `in`.nextString()
        return decodeHtmlEntities(value)
    }
    
    /**
     * 解码 HTML 实体
     * @param text 包含 HTML 实体的文本
     * @return 解码后的文本
     */
    private fun decodeHtmlEntities(text: String): String {
        if (text.isEmpty()) {
            return text
        }
        
        // 使用 Android 的 Html.fromHtml() 方法解码 HTML 实体
        // Html.FROM_HTML_MODE_LEGACY 表示使用传统模式（兼容旧版本）
        // 这会自动解码所有 HTML 实体，包括 &mdash;、&nbsp; 等
        return try {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        } catch (e: Exception) {
            // 如果解码失败，返回原始文本
            text
        }
    }
}

