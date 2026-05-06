package com.example.proyecto.data.history

import com.example.proyecto.HistorialItem

interface HistoryRepository {
    suspend fun getCurrentUserHistory(): List<HistorialItem>
}
