package com.example.carpetneed.data

import android.content.Context
import android.content.SharedPreferences
import com.example.carpetneed.model.HistoryItem
import org.json.JSONArray
import org.json.JSONObject

class HistoryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("carpetneed_history", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY = "saved_calculations"
        const val MAX_HISTORY_ITEMS = 10
    }

    fun getHistory(): List<HistoryItem> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<HistoryItem>()
        return try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.getString("id"),
                        roomName = obj.getString("roomName"),
                        timestamp = obj.getLong("timestamp"),
                        lengthMm = obj.getDouble("lengthMm"),
                        widthMm = obj.getDouble("widthMm"),
                        floorAreaM2 = obj.getDouble("floorAreaM2"),
                        rollWidthMm = obj.getDouble("rollWidthMm"),
                        purchaseLengthM = obj.getDouble("purchaseLengthM"),
                        purchaseAreaM2 = obj.getDouble("purchaseAreaM2"),
                        excessM2 = obj.getDouble("excessM2"),
                        wastePercent = obj.getDouble("wastePercent"),
                        piecesCount = obj.getInt("piecesCount"),
                        seamsCount = obj.getInt("seamsCount"),
                        orientationName = obj.getString("orientationName"),
                        unitSymbol = obj.optString("unitSymbol", "cm")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCalculation(item: HistoryItem): List<HistoryItem> {
        val current = getHistory().toMutableList()
        // Add new item at the beginning
        current.add(0, item)

        // When saving 11th, remove the oldest (at end of list)
        while (current.size > MAX_HISTORY_ITEMS) {
            current.removeAt(current.size - 1)
        }

        saveList(current)
        return current
    }

    fun deleteItem(id: String): List<HistoryItem> {
        val current = getHistory().toMutableList()
        current.removeAll { it.id == id }
        saveList(current)
        return current
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveList(items: List<HistoryItem>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("roomName", item.roomName)
                put("timestamp", item.timestamp)
                put("lengthMm", item.lengthMm)
                put("widthMm", item.widthMm)
                put("floorAreaM2", item.floorAreaM2)
                put("rollWidthMm", item.rollWidthMm)
                put("purchaseLengthM", item.purchaseLengthM)
                put("purchaseAreaM2", item.purchaseAreaM2)
                put("excessM2", item.excessM2)
                put("wastePercent", item.wastePercent)
                put("piecesCount", item.piecesCount)
                put("seamsCount", item.seamsCount)
                put("orientationName", item.orientationName)
                put("unitSymbol", item.unitSymbol)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }
}
