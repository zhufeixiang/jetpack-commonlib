package com.zfx.jetpacklib.data

/**
 * 知识体系数据模型
 * 支持树形结构，包含子节点
 */
data class KnowledgeItem(
    /** 文章列表 */
    val articleList: List<Article> = emptyList(),
    
    /** 作者 */
    val author: String = "",
    
    /** 子节点列表 */
    val children: List<KnowledgeItem> = emptyList(),
    
    /** 课程ID */
    val courseId: Int = 0,
    
    /** 封面图片 */
    val cover: String = "",
    
    /** 描述 */
    val desc: String = "",
    
    /** 唯一标识ID */
    val id: Int = 0,
    
    /** 许可证 */
    val lisense: String = "",
    
    /** 许可证链接 */
    val lisenseLink: String = "",
    
    /** 名称 */
    val name: String = "",
    
    /** 排序顺序 */
    val order: Int = 0,
    
    /** 父章节ID，0表示顶级节点 */
    val parentChapterId: Int = 0,
    
    /** 类型 */
    val type: Int = 0,
    
    /** 用户控制置顶 */
    val userControlSetTop: Boolean = false,
    
    /** 是否可见，1表示可见，0表示不可见 */
    val visible: Int = 1
) {
    /**
     * 是否有子节点
     */
    fun hasChildren(): Boolean = children.isNotEmpty()
    
    /**
     * 是否为顶级节点（父章节ID为0）
     */
    fun isTopLevel(): Boolean = parentChapterId == 0
    
    /**
     * 是否可见
     */
    fun isVisible(): Boolean = visible == 1
}

