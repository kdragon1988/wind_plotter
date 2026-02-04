package com.example.windplotter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Insert
    suspend fun insert(mission: Mission)

    @Update
    suspend fun update(mission: Mission)

    @Query("SELECT * FROM missions ORDER BY createdAt DESC")
    fun getAllMissions(): Flow<List<Mission>>

    @Query("SELECT * FROM missions WHERE missionId = :id")
    suspend fun getMissionById(id: String): Mission?

    @Query("SELECT * FROM missions WHERE status = 'RECORDING'")
    suspend fun getActiveMission(): Mission?
}
