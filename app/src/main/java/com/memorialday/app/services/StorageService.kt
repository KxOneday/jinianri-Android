// 纪念日 - 本地存储服务
// 对应 iOS: StorageService.swift

package com.memorialday.app.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.memorialday.app.models.*
import java.io.File
import java.util.*

/** 存储服务 - 核心数据管理层 */
object StorageService {

    private lateinit var appContext: Context
    private val gson = Gson()

    var allDays: MutableList<MemorialDay> = mutableListOf()
        private set
    var categories: MutableList<Category> = mutableListOf()
        private set
    var settings: AppSettings = AppSettings()
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        EncryptionService.init(appContext)
        loadAll()
    }

    private val documentsDir: File
        get() = appContext.filesDir

    private val dataFile: File
        get() = File(documentsDir, "memorial_days_encrypted.dat")

    private val categoriesFile: File
        get() = File(documentsDir, "categories.json")

    private val settingsFile: File
        get() = File(documentsDir, "settings.json")

    // MARK: - 完整加载

    fun loadAll() {
        loadDays()
        loadCategories()
        loadSettings()
    }

    // MARK: - 纪念日数据

    private fun loadDays() {
        if (!dataFile.exists()) {
            allDays = mutableListOf()
            return
        }
        try {
            val encryptedData = dataFile.readBytes()
            val decrypted = EncryptionService.decrypt(encryptedData)
            if (decrypted != null) {
                val type = object : TypeToken<List<MemorialDay>>() {}.type
                val days: List<MemorialDay> = gson.fromJson(String(decrypted), type)
                allDays = days.sortedBy { it.sortOrder }.toMutableList()
                return
            }
            // 降级：尝试未加密读取
            val type = object : TypeToken<List<MemorialDay>>() {}.type
            val days: List<MemorialDay> = gson.fromJson(String(encryptedData), type)
            allDays = days.sortedBy { it.sortOrder }.toMutableList()
            saveDays() // 重新加密
        } catch (e: Exception) {
            allDays = mutableListOf()
        }
    }

    fun saveDays() {
        try {
            val json = gson.toJson(allDays)
            val encrypted = EncryptionService.encrypt(json.toByteArray())
            if (encrypted != null) {
                dataFile.writeBytes(encrypted)
            } else {
                dataFile.writeText(json)
            }
        } catch (_: Exception) {}
    }

    // MARK: - CRUD

    fun addDay(day: MemorialDay) {
        val newDay = day.copy(sortOrder = allDays.size)
        allDays.add(newDay)
        saveDays()
    }

    fun updateDay(day: MemorialDay) {
        val index = allDays.indexOfFirst { it.id == day.id }
        if (index >= 0) {
            allDays[index] = day
            saveDays()
        }
    }

    fun deleteDay(id: UUID) {
        allDays.removeAll { it.id == id }
        saveDays()
    }

    fun deleteDays(ids: List<UUID>) {
        allDays.removeAll { ids.contains(it.id) }
        saveDays()
    }

    fun clearAllDays() {
        allDays.clear()
        saveDays()
    }

    fun duplicateDay(id: UUID): MemorialDay? {
        val original = allDays.find { it.id == id } ?: return null
        val copy = original.copy(
            id = UUID.randomUUID(),
            title = "${original.title} (副本)",
            createdDate = Date(),
            sortOrder = allDays.size
        )
        allDays.add(copy)
        saveDays()
        return copy
    }

    fun togglePin(id: UUID) {
        val index = allDays.indexOfFirst { it.id == id }
        if (index >= 0) {
            allDays[index] = allDays[index].copy(isPinned = !allDays[index].isPinned)
            saveDays()
        }
    }

    // MARK: - 分类管理

    private fun loadCategories() {
        if (!categoriesFile.exists()) {
            categories = Category.defaultCategories.toMutableList()
            saveCategories()
            return
        }
        try {
            val json = categoriesFile.readText()
            val type = object : TypeToken<List<Category>>() {}.type
            categories = gson.fromJson(json, type)
        } catch (e: Exception) {
            categories = Category.defaultCategories.toMutableList()
        }
    }

    fun saveCategories() {
        try {
            categoriesFile.writeText(gson.toJson(categories))
        } catch (_: Exception) {}
    }

    fun addCategory(category: Category) {
        categories.add(category)
        saveCategories()
    }

    fun deleteCategory(id: UUID) {
        for (i in allDays.indices) {
            if (allDays[i].categoryID == id) {
                allDays[i] = allDays[i].copy(categoryID = null)
            }
        }
        categories.removeAll { it.id == id }
        saveCategories()
        saveDays()
    }

    // MARK: - 设置管理

    private fun loadSettings() {
        if (!settingsFile.exists()) return
        try {
            val json = settingsFile.readText()
            settings = gson.fromJson(json, AppSettings::class.java)
        } catch (_: Exception) {}
    }

    fun saveSettings() {
        try {
            settingsFile.writeText(gson.toJson(settings))
        } catch (_: Exception) {}
    }

    // MARK: - 搜索

    fun search(query: String): List<MemorialDay> {
        if (query.trim().isEmpty()) return allDays
        val q = query.lowercase()
        return allDays.filter { day ->
            day.title.lowercase().contains(q) ||
            day.notes.lowercase().contains(q) ||
            day.tags.any { it.lowercase().contains(q) }
        }
    }

    // MARK: - 筛选

    fun filteredDays(condition: FilterCondition, categoryID: UUID? = null): List<MemorialDay> {
        var days = allDays.toList()

        if (categoryID != null) {
            days = days.filter { it.categoryID == categoryID }
        }

        days = when (condition) {
            FilterCondition.ALL -> days
            FilterCondition.COUNTDOWN -> days.filter { it.dayType == DayType.COUNTDOWN }
            FilterCondition.COUNTUP -> days.filter { it.dayType == DayType.COUNTUP }
            FilterCondition.PINNED -> days.filter { it.isPinned }
            FilterCondition.THIS_MONTH -> {
                val cal = Calendar.getInstance()
                val currentMonth = cal.get(Calendar.MONTH)
                val currentYear = cal.get(Calendar.YEAR)
                days.filter {
                    cal.time = it.targetDate
                    cal.get(Calendar.MONTH) == currentMonth &&
                    cal.get(Calendar.YEAR) == currentYear
                }
            }
        }

        return days.sortedWith(compareByDescending<MemorialDay> { it.isPinned }.thenBy { it.sortOrder })
    }

    // MARK: - 即将到期

    fun upcomingDays(within: Int = 30): List<MemorialDay> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val today = cal.time

        cal.add(Calendar.DAY_OF_MONTH, within)
        val future = cal.time

        return allDays.filter { day ->
            day.dayType == DayType.COUNTDOWN &&
            day.targetDate >= today && day.targetDate <= future
        }.sortedBy { it.targetDate }
    }

    // MARK: - 导入

    fun importData(file: File): Boolean {
        try {
            val json = file.readText()
            val type = object : TypeToken<List<MemorialDay>>() {}.type
            val imported: List<MemorialDay> = gson.fromJson(json, type)
            for (day in imported) {
                if (allDays.none { it.id == day.id }) {
                    allDays.add(day.copy(sortOrder = allDays.size))
                }
            }
            saveDays()
            return true
        } catch (_: Exception) {
            return false
        }
    }
}
