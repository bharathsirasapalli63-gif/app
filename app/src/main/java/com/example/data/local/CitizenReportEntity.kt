package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citizen_reports")
data class CitizenReportEntity(
    @PrimaryKey val id: String,
    val hazardType: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val status: String, // PENDING, VERIFIED, REJECTED, ASSIGNED, RESOLVED
    val assignedOfficer: String?,
    val photoUrl: String?,
    val reporterName: String
)
