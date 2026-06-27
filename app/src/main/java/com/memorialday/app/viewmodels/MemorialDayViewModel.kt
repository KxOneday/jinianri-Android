// 纪念日 - 核心视图模型
// 对应 iOS: MemorialDayViewModel.swift

package com.memorialday.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memorialday.app.MemorialDayApp
import com.memorialday.app.models.*
import com.memorialday.app.services.NotificationService
import com.memorialday.app.services.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class MemorialDayViewModel : ViewModel() {

    companion object {
        // 单例模式 - 通过 Application 获取
        @Volatile
        private var INSTANCE: MemorialDayViewModel? = null

        fun getInstance(): MemorialDayViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MemorialDayViewModel().also { INSTANCE = it }
            }
        }
    }

    // 数据源
    private val _days = MutableStateFlow<List<MemorialDay>>(emptyList())
    val days: StateFlow<List<MemorialDay>> = _days.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterCondition.ALL)
    val selectedFilter: StateFlow<FilterCondition> = _selectedFilter.asStateFlow()

    private val _selectedCategoryID = MutableStateFlow<UUID?>(null)
    val selectedCategoryID: StateFlow<UUID?> = _selectedCategoryID.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // 编辑状态
    private val _editingDay = MutableStateFlow<MemorialDay?>(null)
    val editingDay: StateFlow<MemorialDay?> = _editingDay.asStateFlow()

    private val _selectedDays = MutableStateFlow<Set<UUID>>(emptySet())
    val selectedDays: StateFlow<Set<UUID>> = _selectedDays.asStateFlow()

    // UI 状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _upcomingDays = MutableStateFlow<List<MemorialDay>>(emptyList())
    val upcomingDays: StateFlow<List<MemorialDay>> = _upcomingDays.asStateFlow()

    private val storage = StorageService

    init {
        loadData()
    }

    fun loadData() {
        _days.value = storage.allDays
        _categories.value = storage.categories
        updateUpcomingDays()
    }

    fun refreshDisplay() {
        var filtered = storage.filteredDays(_selectedFilter.value, _selectedCategoryID.value)

        val query = _searchText.value
        if (query.isNotEmpty()) {
            val q = query.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
            }
        }

        _days.value = filtered
        updateUpcomingDays()
    }

    private fun updateUpcomingDays() {
        _upcomingDays.value = storage.upcomingDays(30)
    }

    // CRUD

    fun addDay(day: MemorialDay) {
        storage.addDay(day)
        NotificationService.scheduleNotification(day, MemorialDayApp.instance)
        loadData()
        refreshDisplay()
        // 强制刷新：拷贝列表触发 StateFlow 发射新值
        _days.value = _days.value.toList()
    }

    fun updateDay(day: MemorialDay) {
        storage.updateDay(day)
        NotificationService.scheduleNotification(day, MemorialDayApp.instance)
        loadData()
        refreshDisplay()
        _days.value = _days.value.toList()
    }

    fun deleteDay(id: UUID) {
        NotificationService.cancelNotifications(id, MemorialDayApp.instance)
        storage.deleteDay(id)
        loadData()
        refreshDisplay()
    }

    fun deleteSelectedDays() {
        val ids = _selectedDays.value.toList()
        for (id in ids) {
            NotificationService.cancelNotifications(id, MemorialDayApp.instance)
        }
        storage.deleteDays(ids)
        _selectedDays.value = emptySet()
        loadData()
        refreshDisplay()
    }

    fun duplicateDay(id: UUID) {
        storage.duplicateDay(id)
        loadData()
        refreshDisplay()
    }

    fun togglePin(id: UUID) {
        storage.togglePin(id)
        loadData()
        refreshDisplay()
    }

    fun applyCategoryFilter(categoryID: UUID?) {
        _selectedCategoryID.value = categoryID
        refreshDisplay()
    }

    fun setSearchText(text: String) {
        _searchText.value = text
        refreshDisplay()
    }

    // 批量选择

    fun toggleSelection(id: UUID) {
        val current = _selectedDays.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedDays.value = current
    }

    fun selectAll() {
        _selectedDays.value = _days.value.map { it.id }.toSet()
    }

    fun deselectAll() {
        _selectedDays.value = emptySet()
    }

    // 分类管理

    fun addCategory(category: Category) {
        storage.addCategory(category)
        loadData()
    }

    fun deleteCategory(id: UUID) {
        storage.deleteCategory(id)
        loadData()
    }

    // 模板创建

    fun createFromTemplate(template: MemorialTemplate): MemorialDay {
        var day = MemorialDay()
        day = day.copy(title = template.defaultTitle, dayType = template.defaultDayType)

        val cat = _categories.value.find { it.name == template.categoryName }
        if (cat != null) {
            day = day.copy(categoryID = cat.id)
        }

        return day
    }
}
