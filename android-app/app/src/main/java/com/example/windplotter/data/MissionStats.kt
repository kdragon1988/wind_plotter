package com.example.windplotter.data

data class MissionStats(
    val maxWindSpeed: Float,
    val avgWindSpeed: Float,
    val avgAltitude: Double,
    val startTime: Long,
    val endTime: Long
)
