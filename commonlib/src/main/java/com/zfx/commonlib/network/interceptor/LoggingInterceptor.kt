package com.zfx.commonlib.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException

/**
 * 自定义日志拦截器
 * 使用 Android Log 类打印网络请求日志，替代 logging-interceptor
 * 
 * 使用示例：
 * ```kotlin
 * val interceptor = LoggingInterceptor(
 *     tag = "OkHttp",
 *     level = LogLevel.BODY
 * )
 * ```
 */
class LoggingInterceptor(
    /**
     * Log 标签
     */
    private val tag: String = "OkHttp",
    /**
     * 日志级别
     */
    private val level: LogLevel = LogLevel.BODY,
    /**
     * 是否格式化 JSON（美化输出）
     */
    private val formatJson: Boolean = true,
    /**
     * 是否在响应体前后添加标记（方便复制）
     */
    private val addCopyMarkers: Boolean = true
) : Interceptor {
    
    /**
     * Android Log 的最大长度限制（约 4000 字符）
     */
    private val MAX_LOG_LENGTH = 4000

    /**
     * 日志级别枚举
     */
    enum class LogLevel {
        /**
         * 不打印日志
         */
        NONE,
        
        /**
         * 只打印请求和响应的基本信息（URL、方法、状态码）
         */
        BASIC,
        
        /**
         * 打印请求和响应的头部信息
         */
        HEADERS,
        
        /**
         * 打印请求和响应的完整信息（包括请求体、响应体）
         */
        BODY
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (level == LogLevel.NONE) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        val requestTime = System.currentTimeMillis()

        // 打印请求信息
        logRequest(request)

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(tag, "请求失败: ${e.message}")
            throw e
        }

        val responseTime = System.currentTimeMillis()
        val duration = responseTime - requestTime

        // 打印响应信息（如果需要读取响应体，需要重新创建响应）
        val loggedResponse = if (level == LogLevel.BODY) {
            logResponseWithBody(response, duration)
        } else {
            logResponse(response, duration)
            response
        }

        return loggedResponse
    }

    /**
     * 打印请求信息
     */
    private fun logRequest(request: Request) {
        if (level == LogLevel.NONE) return

        val method = request.method
        val url = request.url
        val headers = request.headers

        Log.d(tag, "┌────── Request ──────")
        Log.d(tag, "│ $method $url")

        if (level == LogLevel.HEADERS || level == LogLevel.BODY) {
            // 打印请求头
            if (headers.size > 0) {
                Log.d(tag, "│")
                Log.d(tag, "│ Request Headers:")
                headers.forEach { header ->
                    Log.d(tag, "│   ${header.first}: ${header.second}")
                }
            } else {
                Log.d(tag, "│")
                Log.d(tag, "│ Request Headers: (无)")
            }
        }

        if (level == LogLevel.BODY && request.body != null) {
            // 打印请求体
            val requestBody = requestBodyToString(request)
            if (requestBody.isNotEmpty()) {
                Log.d(tag, "│")
                Log.d(tag, "│ Request Body:")
                logLongMessage(requestBody, prefix = "│ ")
            }
        }

        Log.d(tag, "└────────────────────")
    }

    /**
     * 打印响应信息
     */
    private fun logResponse(response: Response, duration: Long) {
        if (level == LogLevel.NONE) return

        val code = response.code
        val message = response.message
        val url = response.request.url
        val headers = response.headers

        Log.d(tag, "┌────── Response ──────")
        Log.d(tag, "│ $code $message")
        Log.d(tag, "│ $url")
        Log.d(tag, "│ Duration: ${duration}ms")

        if (level == LogLevel.HEADERS || level == LogLevel.BODY) {
            // 打印响应头
            if (headers.size > 0) {
                Log.d(tag, "│")
                Log.d(tag, "│ Response Headers:")
                headers.forEach { header ->
                    Log.d(tag, "│   ${header.first}: ${header.second}")
                }
            } else {
                Log.d(tag, "│")
                Log.d(tag, "│ Response Headers: (无)")
            }
        }

        Log.d(tag, "└─────────────────────")
    }

    /**
     * 打印响应信息（包括响应体）
     * 注意：读取响应体后会重新创建响应，因为响应体只能读取一次
     */
    private fun logResponseWithBody(response: Response, duration: Long): Response {
        if (level == LogLevel.NONE) return response

        val code = response.code
        val message = response.message
        val url = response.request.url
        val headers = response.headers

        Log.d(tag, "┌────── Response ──────")
        Log.d(tag, "│ $code $message")
        Log.d(tag, "│ $url")
        Log.d(tag, "│ Duration: ${duration}ms")

        if (level == LogLevel.HEADERS || level == LogLevel.BODY) {
            // 打印响应头
            if (headers.size > 0) {
                Log.d(tag, "│")
                Log.d(tag, "│ Response Headers:")
                headers.forEach { header ->
                    Log.d(tag, "│   ${header.first}: ${header.second}")
                }
            } else {
                Log.d(tag, "│")
                Log.d(tag, "│ Response Headers: (无)")
            }
        }

        // 读取并打印响应体，然后重新创建响应
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // 请求所有数据
            val buffer = source.buffer
            
            val bodyString = buffer.clone().readUtf8()
            
            if (bodyString.isNotEmpty()) {
                Log.d(tag, "│")
                Log.d(tag, "│ Response Body:")
                
                // 格式化并打印响应体
                val formattedBody = if (formatJson) {
                    formatJsonIfPossible(bodyString)
                } else {
                    bodyString
                }
                
                // 添加复制标记
                if (addCopyMarkers) {
                    Log.d(tag, "│ ┌─────────────────────────────────────────────────────────")
                    Log.d(tag, "│ │ 开始复制 ↓")
                    Log.d(tag, "│ └─────────────────────────────────────────────────────────")
                }
                
                logLongMessage(formattedBody, prefix = "│ ")
                
                if (addCopyMarkers) {
                    Log.d(tag, "│ ┌─────────────────────────────────────────────────────────")
                    Log.d(tag, "│ │ 结束复制 ↑")
                    Log.d(tag, "│ └─────────────────────────────────────────────────────────")
                }
            }
            
            // 重新创建响应，因为响应体已经被读取了
            val contentType = responseBody.contentType()
            val newResponseBody = bodyString.toResponseBody(contentType)
            
            Log.d(tag, "└─────────────────────")
            
            return response.newBuilder()
                .body(newResponseBody)
                .build()
        }

        Log.d(tag, "└─────────────────────")
        return response
    }

    /**
     * 将请求体转换为字符串
     */
    private fun requestBodyToString(request: Request): String {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            val bodyString = buffer.readUtf8()
            // 如果是 JSON，尝试格式化
            if (formatJson) {
                formatJsonIfPossible(bodyString)
            } else {
                bodyString
            }
        } catch (e: Exception) {
            "无法读取请求体: ${e.message}"
        }
    }
    
    /**
     * 打印长消息（自动分段，避免 Log 截断）
     * 
     * @param message 要打印的消息
     * @param prefix 每行的前缀（如 "│ "）
     */
    private fun logLongMessage(message: String, prefix: String = "") {
        if (message.length <= MAX_LOG_LENGTH) {
            // 消息不长，直接打印
            message.split("\n").forEach { line ->
                Log.d(tag, "$prefix$line")
            }
            return
        }
        
        // 消息太长，需要分段打印
        var start = 0
        var segmentIndex = 0
        
        while (start < message.length) {
            var end = start + MAX_LOG_LENGTH
            
            // 如果还有更多内容，尝试在换行符处截断
            if (end < message.length) {
                // 在 [start, end) 范围内查找最后一个换行符
                val searchEnd = end.coerceAtMost(message.length)
                val lastNewline = message.lastIndexOf('\n', searchEnd - 1)
                if (lastNewline >= start) {
                    end = lastNewline + 1
                }
            }
            
            val segment = message.substring(start, end.coerceAtMost(message.length))
            
            // 直接打印分段内容
            segment.split("\n").forEach { line ->
                Log.d(tag, "$prefix$line")
            }
            
            start = end
            segmentIndex++
        }
    }
    
    /**
     * 计算消息需要分成多少段
     */
    private fun getSegmentCount(message: String): Int {
        return if (message.length <= MAX_LOG_LENGTH) {
            1
        } else {
            (message.length + MAX_LOG_LENGTH - 1) / MAX_LOG_LENGTH
        }
    }
    
    /**
     * 如果字符串是 JSON，则格式化它
     * 
     * @param jsonString 可能是 JSON 的字符串
     * @return 格式化后的 JSON 字符串，如果不是 JSON 则返回原字符串
     */
    private fun formatJsonIfPossible(jsonString: String): String {
        if (!formatJson) {
            return jsonString
        }
        
        return try {
            val trimmed = jsonString.trim()
            when {
                trimmed.startsWith("{") -> {
                    // 尝试解析为 JSONObject
                    val jsonObject = JSONObject(trimmed)
                    jsonObject.toString(2) // 缩进 2 个空格
                }
                trimmed.startsWith("[") -> {
                    // 尝试解析为 JSONArray
                    val jsonArray = JSONArray(trimmed)
                    jsonArray.toString(2) // 缩进 2 个空格
                }
                else -> {
                    // 不是 JSON，返回原字符串
                    jsonString
                }
            }
        } catch (e: Exception) {
            // 解析失败，不是有效的 JSON，返回原字符串
            jsonString
        }
    }

}

