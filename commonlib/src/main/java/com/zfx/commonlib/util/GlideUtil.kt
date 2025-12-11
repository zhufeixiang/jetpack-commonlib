package com.zfx.commonlib.util

import android.util.TypedValue
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

/**
 * author: zhufeixiang
 * date: 2025/12/11
 * des: Glide 工具方法，封装常用加载（本地/网络/视频首帧/圆角/圆形/GIF）
 */
object GlideUtil {

    /**
     * 通用加载入口
     */
    @JvmStatic
    @JvmOverloads
    fun load(
        target: ImageView,
        model: Any?,
        requestOptions: RequestOptions? = null,
        skipMemoryCache: Boolean = false,
        diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC
    ) {
        Glide.with(target.context)
            .load(model)
            .apply {
                requestOptions?.let { apply(it) }
            }
            .skipMemoryCache(skipMemoryCache)
            .diskCacheStrategy(diskCacheStrategy)
            .into(target)
    }

    /**
     * 加载圆角图片（四角统一半径，单位 dp）
     */
    @JvmStatic
    fun loadRounded(target: ImageView, model: Any?, radiusDp: Int) {
        val radiusPx = radiusDp.dpToPx(target)
        val options = RequestOptions.bitmapTransform(RoundedCorners(radiusPx))
        load(target, model, options)
    }

    /**
     * 加载自定义四角圆角（单位 dp）
     */
    @JvmStatic
    fun loadRounded(
        target: ImageView,
        model: Any?,
        topLeftDp: Int,
        topRightDp: Int,
        bottomLeftDp: Int,
        bottomRightDp: Int
    ) {
        val br = RoundedCornersTransformation(
            bottomRightDp.dpToPx(target),
            0,
            RoundedCornersTransformation.CornerType.BOTTOM_RIGHT
        )
        val bl = RoundedCornersTransformation(
            bottomLeftDp.dpToPx(target),
            0,
            RoundedCornersTransformation.CornerType.BOTTOM_LEFT
        )
        val tr = RoundedCornersTransformation(
            topRightDp.dpToPx(target),
            0,
            RoundedCornersTransformation.CornerType.TOP_RIGHT
        )
        val tl = RoundedCornersTransformation(
            topLeftDp.dpToPx(target),
            0,
            RoundedCornersTransformation.CornerType.TOP_LEFT
        )
        val mation = MultiTransformation(CenterCrop(), br, bl, tr, tl)
        val options = RequestOptions.bitmapTransform(mation)
        load(target, model, options)
    }

    /**
     * 加载圆形图片
     */
    @JvmStatic
    fun loadCircle(target: ImageView, model: Any?) {
        val options = RequestOptions.circleCropTransform()
            .priority(Priority.NORMAL)
        load(target, model, options)
    }

    /**
     * 加载 GIF（可选跳过缓存）
     */
    @JvmStatic
    fun loadGif(
        target: ImageView,
        model: Any?,
        skipMemoryCache: Boolean = false,
        diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC
    ) {
        Glide.with(target.context)
            .asGif()
            .load(model)
            .skipMemoryCache(skipMemoryCache)
            .diskCacheStrategy(diskCacheStrategy)
            .into(target)
    }

    /**
     * 加载视频首帧，支持圆角（radiusDp 可为 0 表示不处理）
     * @param frameMicros 默认取 1s 处帧
     */
    @JvmStatic
    @JvmOverloads
    fun loadVideoFirstFrame(
        target: ImageView,
        videoUrl: String,
        radiusDp: Int = 0,
        frameMicros: Long = 1_000_000L
    ) {
        val options = if (radiusDp > 0) {
            val radiusPx = radiusDp.dpToPx(target)
            RequestOptions.bitmapTransform(RoundedCorners(radiusPx))
                .frame(frameMicros)
                .centerCrop()
        } else {
            RequestOptions.frameOf(frameMicros).centerCrop()
        }
        load(target, videoUrl, options)
    }

    private fun Int.dpToPx(target: ImageView): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            target.context.resources.displayMetrics
        ).toInt()
}