package com.zfx.commonlib.util

import android.util.TypedValue
import android.widget.ImageView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.File

/**
 * author : zhufeixiang
 * date : 2022/7/28
 * des : glide 工具类
 */
object GlideUtil {


    /**
     * 加载本地文件图片
     * */
    fun loadFile(file: File, view: ImageView) {
        Glide.with(view.context)
            .load(file)
//            .placeholder(R.mipmap.placeholder)
//            .error(R.mipmap.placeholder)
            .into(view)
    }

    /**
     * 不使用缓存加载本地文件图片
     * */
    fun loadFileNoCache(file: File, view: ImageView) {
        Glide.with(view.context)
            .load(file)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(view)
    }

    /**
     * 加载正常在线图片
     * **/
    fun loadCommonPicNet(targetView: ImageView,imageUrl: String) {
        Glide.with(targetView.context)
            .load(imageUrl)
//            .placeholder(R.mipmap.placeholder)
//            .error(R.mipmap.placeholder)
            .into(targetView)
    }

    /**
     * 加载正常本地资源图片
     * **/
    fun loadCommonPicLocal(targetView: ImageView,resId: Int,) {
        Glide.with(targetView.context)
            .load(resId)
//            .placeholder(R.mipmap.placeholder)
//            .error(R.mipmap.placeholder)
            .into(targetView)
    }


    /**
     * 加载视频的第一帧图片
     * @param videoUrl 视频地址
     * @param targetView 目标view
     * @param cornerRadius 圆角大小
     * */
    fun loadVideoFirstFrame(
        targetView: ImageView,
        videoUrl: String,
        cornerRadius: Int,
    ) {
        val spToInt = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            cornerRadius.toFloat(),
            targetView.context.resources.displayMetrics
        ).toInt()

        //Glide设置图片圆角角度
        val roundedCorners = RoundedCorners(spToInt)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        // RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(20, 20);
        val options = RequestOptions.bitmapTransform(roundedCorners).frame(1000000).centerCrop()
        Glide.with(targetView.context)
            .load(videoUrl)
//            .placeholder(R.drawable.ic_default_image)
//            .error(R.mipmap.placeholder)
            .apply(options)
            .into(targetView)

    }

    /**
     * 圆角图片 在线 四个角一样大
     * @param imageUrl 图片的链接地址
     * @param targetView 目标view
     * @param cornerRadius 圆角大小
     * */
    fun loadRoundedCornerNet (
        targetView: ImageView,
        imageUrl: String,
        cornerRadius: Int
    ) {
        val spToInt = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            cornerRadius.toFloat(),
            targetView.context.resources.displayMetrics
        ).toInt()

        //Glide设置图片圆角角度
        val roundedCorners = RoundedCorners(spToInt)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        // RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(20, 20);
        val options = RequestOptions.bitmapTransform(roundedCorners)
        Glide.with(targetView.context)
            .load(imageUrl)
//            .placeholder(R.drawable.ic_default_image)
//            .error(R.mipmap.placeholder)
            .apply(options)
            .into(targetView)

    }

    /**
     * 圆角图片 本地 四个角一样大
     * @param imageUrl 图片的链接地址
     * @param targetView 目标view
     * @param cornerRadius 圆角大小
     * */
    fun loadRoundedCornerLocal (
        targetView: ImageView,
        resId: Int,
        cornerRadius: Int
    ) {
        val spToInt = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            cornerRadius.toFloat(),
            targetView.context.resources.displayMetrics
        ).toInt()

        //Glide设置图片圆角角度
        val roundedCorners = RoundedCorners(spToInt)
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        // RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(20, 20);
        val options = RequestOptions.bitmapTransform(roundedCorners)
        Glide.with(targetView.context)
            .load(resId)
//            .placeholder(R.drawable.ic_default_image)
//            .error(R.mipmap.placeholder)
            .apply(options)
            .into(targetView)

    }

    /**
     * 显示圆角图片  - 在线
     * @param imageUrl 图片的链接地址
     * @param targetView 目标view
     * @param topLeft 左上圆角大小
     * @param topRight 右上圆角大小
     * @param bottomLeft 左下圆角大小
     * @param bottomRight 右下圆角大小
     * */
    fun loadRoundedCornerNet(
        targetView: ImageView,
        imageUrl: String,
        topLeft: Int,
        topRight: Int,
        bottomLeft: Int,
        bottomRight: Int
    ) {
        val br = RoundedCornersTransformation(
            SizeUtils.dp2px(bottomRight.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.BOTTOM_RIGHT
        )
        val bl = RoundedCornersTransformation(
            SizeUtils.dp2px(bottomLeft.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.BOTTOM_LEFT
        )
        val tr = RoundedCornersTransformation(
            SizeUtils.dp2px(topRight.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.TOP_RIGHT
        )
        val tl = RoundedCornersTransformation(
            SizeUtils.dp2px(topLeft.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.TOP_LEFT
        )

        //组合各种Transformation,
        val mation =
            MultiTransformation( //Glide设置圆角图片后设置ImageVIew的scanType="centerCrop"无效解决办法,将new CenterCrop()添加至此
                CenterCrop(), br, bl, tr, tl
            )

        Glide.with(targetView.context)
            .load(imageUrl)
//            .placeholder()
//            .error(R.mipmap.placeholder)
            .apply(RequestOptions.bitmapTransform(mation))
            .into(targetView)
    }



    /**
     * 显示圆角图片 四个角不一样大 -本地
     * */
    fun loadRoundedCornerLocal(
        targetView: ImageView,
        resId: Int,
        topLeft: Int,
        topRight: Int,
        bottomLeft: Int,
        bottomRight: Int
    ) {
        val br = RoundedCornersTransformation(
            SizeUtils.dp2px(bottomRight.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.BOTTOM_RIGHT
        )
        val bl = RoundedCornersTransformation(
            SizeUtils.dp2px(bottomLeft.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.BOTTOM_LEFT
        )
        val tr = RoundedCornersTransformation(
            SizeUtils.dp2px(topRight.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.TOP_RIGHT
        )
        val tl = RoundedCornersTransformation(
            SizeUtils.dp2px(topLeft.toFloat()),
            0,
            RoundedCornersTransformation.CornerType.TOP_LEFT
        )

        //组合各种Transformation,
        val mation =
            MultiTransformation( //Glide设置圆角图片后设置ImageVIew的scanType="centerCrop"无效解决办法,将new CenterCrop()添加至此
                CenterCrop(), br, bl, tr, tl
            )

        Glide.with(targetView.context)
            .load(resId)
//            .placeholder(R.mipmap.placeholder)
//            .error(R.mipmap.placeholder)
            .apply(RequestOptions.bitmapTransform(mation))
            .into(targetView)
    }


    /**
     * 加载gif - 在线
     * */
    fun loadGifNet(targetView: ImageView,gifUrl: String) {
        Glide.with(targetView.context)
            .asGif()
            .load(gifUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(targetView)

    }

    /**
     * 加载gif - 本地
     * */
    fun loadGifLocal(targetView: ImageView,resId: Int) {
        Glide.with(targetView.context)
            .asGif()
            .load(resId)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(targetView)

    }

    /**
     * 显示圆形 网络图片
     * */
    fun loadCircleNet(targetView: ImageView,viewUrl: String) {

        Glide.with(targetView.context)
            .load(viewUrl)
//            .placeholder()
//            .error(R.mipmap.placeholder)
            .apply(RequestOptions.circleCropTransform())
            .into(targetView)
    }

    /**
     * 显示圆形 本地资源
     * */
    fun loadCircleLocal(targetView: ImageView,resId: Int) {

        Glide.with(targetView.context)
            .load(resId)
//            .placeholder()
//            .error(R.mipmap.placeholder)
            .apply(RequestOptions.circleCropTransform())
            .into(targetView)
    }

}