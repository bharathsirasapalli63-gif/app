package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CitizenReportDao {
    @Query("SELECT * FROM citizen_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<CitizenReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CitizenReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<CitizenReportEntity>)

    @Update
    suspend fun updateReport(report: CitizenReportEntity)

    @Query("UPDATE citizen_reports SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE citizen_reports SET status = :status, assignedOfficer = :officer WHERE id = :id")
    suspend fun assignOfficer(id: String, status: String, officer: String)

    @Query("DELETE FROM citizen_reports WHERE id = :id")
    suspend fun deleteById(id: String)
}
