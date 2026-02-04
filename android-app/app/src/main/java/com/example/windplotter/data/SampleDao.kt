package com.example.windplotter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {
    @Insert
    suspend fun insert(sample: Sample)

    @Insert
    suspend fun insertAll(samples: List<Sample>)

    @Query("SELECT * FROM samples WHERE missionId = :missionId ORDER BY seq ASC")
    fun getSamplesForMission(missionId: String): Flow<List<Sample>>

    @Query("SELECT * FROM samples WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsyncedSamples(limit: Int = 50): List<Sample>

    @Query("UPDATE samples SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
    
    @Query("SELECT COUNT(*) FROM samples WHERE missionId = :missionId")
    fun getSampleCount(missionId: String): Flow<Int>
    
    @Query("SELECT * FROM samples WHERE missionId = :missionId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSample(missionId: String): Flow<Sample?>
    
    @Query("SELECT * FROM samples WHERE missionId = :missionId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSamples(missionId: String, limit: Int = 10): Flow<List<Sample>>

    @Query("SELECT COUNT(*) FROM samples WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("""
        SELECT 
            MAX(windSpeed) as maxWindSpeed, 
            AVG(windSpeed) as avgWindSpeed, 
            AVG(altitude) as avgAltitude, 
            MIN(timestamp) as startTime, 
            MAX(timestamp) as endTime 
        FROM samples 
        WHERE missionId = :missionId
    """)
    fun getMissionStats(missionId: String): Flow<MissionStats?>
}
