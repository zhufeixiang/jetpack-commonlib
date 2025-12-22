package com.zfx.jetpacklib.data

/**
 * Banner 数据模型
 */
data class BannerItem(
    /** 描述信息 */
    val desc: String? = null,
    /** Banner 的唯一标识 ID */
    val id: Int? = null,
    /** 图片路径 URL */
    val imagePath: String? = null,
    /** 是否可见，1 表示可见，0 表示不可见 */
    val isVisible: Int? = null,
    /** 排序顺序 */
    val order: Int? = null,
    /** 标题 */
    val title: String? = null,
    /** 类型 */
    val type: Int? = null,
    /** 点击后跳转的链接地址 */
    val url: String? = null
)

