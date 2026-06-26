// 纪念日 - 分类模型
// 对应 iOS: Category.swift

package com.memorialday.app.models

import java.util.*

data class Category(
    var id: UUID = UUID.randomUUID(),
    var name: String = "",
    var iconName: String = "favorite",
    var colorHex: String = "#8B9DC3",
    var sortOrder: Int = 0,
    var isDefault: Boolean = false
) {
    companion object {
        /** 内置预设分类列表 */
        val defaultCategories: List<Category>
            get() = listOf(
                Category(name = "生日", iconName = "card_giftcard", colorHex = "#FF6B6B", sortOrder = 0, isDefault = true),
                Category(name = "恋爱", iconName = "favorite", colorHex = "#FF69B4", sortOrder = 1, isDefault = true),
                Category(name = "纪念日", iconName = "star", colorHex = "#FFD700", sortOrder = 2, isDefault = true),
                Category(name = "工作", iconName = "work", colorHex = "#4A90D9", sortOrder = 3, isDefault = true),
                Category(name = "考试", iconName = "menu_book", colorHex = "#9B59B6", sortOrder = 4, isDefault = true),
                Category(name = "还款", iconName = "credit_card", colorHex = "#E74C3C", sortOrder = 5, isDefault = true),
                Category(name = "节日", iconName = "celebration", colorHex = "#FFA500", sortOrder = 6, isDefault = true),
                Category(name = "旅行", iconName = "flight", colorHex = "#1ABC9C", sortOrder = 7, isDefault = true),
                Category(name = "健康", iconName = "favorite_border", colorHex = "#2ECC71", sortOrder = 8, isDefault = true),
                Category(name = "其他", iconName = "more_horiz", colorHex = "#95A5A6", sortOrder = 9, isDefault = true)
            )
    }
}

/** 筛选条件 */
enum class FilterCondition(val rawValue: String, val iconName: String) {
    ALL("全部", "grid_view"),
    COUNTDOWN("倒数日", "arrow_downward"),
    COUNTUP("正数日", "arrow_upward"),
    PINNED("置顶", "push_pin"),
    THIS_MONTH("本月纪念日", "date_range");
}
