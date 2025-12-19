package com.zfx.jetpacklib.data

/**
 * 文章分页数据
 * 
 * 用于封装分页列表的响应数据
 * 
 * 使用示例：
 * ```kotlin
 * // API 响应
 * ApiResponse<ArticlePageData>
 * 
 * // 获取数据
 * val pageData = response.data
 * val articles = pageData?.datas ?: emptyList()
 * val hasMore = !pageData?.over ?: false
 * ```
 */
data class ArticlePageData(
    /**
     * 当前页码
     */
    val curPage: Int = 0,
    
    /**
     * 文章列表
     */
    val datas: List<Article> = emptyList(),
    
    /**
     * 偏移量
     */
    val offset: Int = 0,
    
    /**
     * 是否已加载完所有数据
     */
    val over: Boolean = false,
    
    /**
     * 总页数
     */
    val pageCount: Int = 0,
    
    /**
     * 每页大小
     */
    val size: Int = 0,
    
    /**
     * 总数据量
     */
    val total: Int = 0
) {
    /**
     * 是否还有更多数据
     */
    fun hasMore(): Boolean = !over
    
    /**
     * 是否为空
     */
    fun isEmpty(): Boolean = datas.isEmpty()
    
    /**
     * 获取下一页页码
     */
    fun getNextPage(): Int = if (hasMore()) curPage + 1 else curPage
}

