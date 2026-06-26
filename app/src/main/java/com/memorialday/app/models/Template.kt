// 纪念日 - 纪念日模板模型
// 对应 iOS: Template.swift

package com.memorialday.app.models

import java.util.*

data class MemorialTemplate(
    var id: UUID = UUID.randomUUID(),
    var name: String = "",
    var categoryName: String = "",
    var iconName: String = "favorite",
    var defaultTitle: String = "",
    var defaultDayType: DayType = DayType.COUNTDOWN,
    var defaultCategoryIndex: Int = 0,
    var suggestedDateOffset: Pair<Int, Int>? = null  // (value, Calendar.FIELD)
) {
    companion object {
        val builtInTemplates: List<MemorialTemplate>
            get() = listOf(
                MemorialTemplate(name = "生日", categoryName = "生日", iconName = "card_giftcard",
                    defaultTitle = "我的生日", defaultCategoryIndex = 0),
                MemorialTemplate(name = "恋爱纪念日", categoryName = "恋爱", iconName = "favorite",
                    defaultTitle = "恋爱纪念日", defaultDayType = DayType.COUNTUP, defaultCategoryIndex = 1),
                MemorialTemplate(name = "结婚纪念日", categoryName = "恋爱", iconName = "auto_awesome",
                    defaultTitle = "结婚纪念日", defaultDayType = DayType.COUNTUP, defaultCategoryIndex = 1),
                MemorialTemplate(name = "考试倒计时", categoryName = "考试", iconName = "menu_book",
                    defaultTitle = "考试倒计时", defaultCategoryIndex = 4),
                MemorialTemplate(name = "还款提醒", categoryName = "还款", iconName = "credit_card",
                    defaultTitle = "信用卡还款", defaultCategoryIndex = 5),
                MemorialTemplate(name = "法定节日", categoryName = "节日", iconName = "celebration",
                    defaultTitle = "元旦", defaultCategoryIndex = 6),
                MemorialTemplate(name = "工作DDL", categoryName = "工作", iconName = "work",
                    defaultTitle = "项目截止日", defaultCategoryIndex = 3),
                MemorialTemplate(name = "旅行倒计时", categoryName = "旅行", iconName = "flight",
                    defaultTitle = "旅行出发日", defaultCategoryIndex = 7),
                MemorialTemplate(name = "纪念日（自定义）", categoryName = "其他", iconName = "star",
                    defaultTitle = "重要日子", defaultCategoryIndex = 9),
                MemorialTemplate(name = "健康管理", categoryName = "健康", iconName = "favorite_border",
                    defaultTitle = "体检日", defaultCategoryIndex = 8),
                MemorialTemplate(name = "发薪日", categoryName = "工作", iconName = "attach_money",
                    defaultTitle = "发薪日", defaultCategoryIndex = 3),
            )
    }
}
