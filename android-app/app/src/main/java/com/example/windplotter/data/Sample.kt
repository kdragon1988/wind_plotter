package com.example.windplotter.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "samples",
    foreignKeys = [
        ForeignKey(
            entity = Mission::class,
            parentColumns = ["missionId"],
            childColumns = ["missionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["missionId"]), Index(value = ["isSynced"])]
)
data class Sample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val missionId: String,
    val timestamp: Long,
    val seq: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val windSpeed: Float,
    val windDirection: Float,
    val windWarningLevel: Int? = null,
    val isSynced: Boolean = false
)
