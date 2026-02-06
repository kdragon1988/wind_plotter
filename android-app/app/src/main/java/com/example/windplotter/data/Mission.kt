package com.example.windplotter.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "missions")
data class Mission(
    @PrimaryKey
    val missionId: String = UUID.randomUUID().toString(),
    val name: String,
    val assignee: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "RECORDING", // RECORDING, FINISHED, SYNCED
    val sessionCount: Int = 0,
    val lastMeasuredAt: Long? = null
)
