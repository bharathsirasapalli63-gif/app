package com.example.data.model

data class LocationZone(
    val id: String,
    val name: String,
    val district: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val riskPercentage: Int,
    val riskLevel: RiskLevel,
    val rainfall: Double, // mm/24h
    val soilMoisture: Double, // %
    val groundMovement: Double, // mm/s
    val slopeAngle: Double, // degrees
    val terrainType: String,
    val predictionWindow: String,
    val whyElevated: String,
    val safetyPrecautions: List<String>,
    val populationAtRisk: Int,
    val shelters: List<EvacuationShelter>,
    val weatherTemp: Int,
    val weatherCondition: String,
    val weatherWind: String,
    val recentEarthquakes: List<EarthquakeEvent>
)

enum class RiskLevel(val label: String) {
    LOW("Safe"),
    MODERATE("Caution"),
    HIGH("Warning"),
    CRITICAL("Critical Danger")
}

data class EvacuationShelter(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    val occupied: Int
)

data class EarthquakeEvent(
    val year: Int,
    val date: String,
    val magnitude: Double,
    val distance: String,
    val impact: String
)

data class SensorNode(
    val id: String,
    val name: String,
    val type: SensorType,
    val typeLabel: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    var status: SensorStatus,
    var value: Double,
    val unit: String,
    var battery: Int,
    val signalDbm: Int,
    var lastPing: String,
    val installationDate: String,
    var faultDescription: String? = null
)

enum class SensorType {
    GROUND_VIBRATION,
    CRACK_DISPLACEMENT,
    SOIL_MOISTURE,
    TILT_INCLINOMETER,
    RAIN_GAUGE
}

enum class SensorStatus {
    ONLINE,
    WARNING,
    CRITICAL,
    OFFLINE
}

data class RoadCorridor(
    val id: String,
    val name: String,
    var status: RoadStatus,
    val riskLevel: RiskLevel,
    val riskPercentage: Int,
    val reason: String,
    val blockedSection: String,
    val alternativeRouteName: String,
    val alternativeRouteRisk: RiskLevel,
    val alternativeRouteRiskPercentage: Int
)

enum class RoadStatus {
    OPEN,
    UNSAFE,
    BLOCKED
}

enum class ReportStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    ASSIGNED,
    RESOLVED
}

data class FieldTask(
    val id: String,
    val title: String,
    val sourceReportId: String,
    val assignedTo: String,
    val priority: TaskPriority,
    var status: WorkflowStep,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val taskType: String,
    val assignedAt: String,
    val deadline: String,
    val instructions: String,
    var inspectionData: InspectionResult? = null
)

enum class TaskPriority {
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class WorkflowStep(val label: String) {
    ASSIGNED("Assigned"),
    ACCEPTED("Accepted"),
    TRAVELLING("Travelling"),
    ON_SITE("On Site"),
    INSPECTION("Inspection"),
    REPORT_SUBMITTED("Reported"),
    RESOLVED("Resolved")
}

data class InspectionResult(
    val crackWidthMm: Double,
    val slopeTiltDeg: Double,
    val rockfallSeverity: String,
    val waterSeepageRate: String,
    val roadDamage: String,
    val aiAnalysisResult: String,
    val notes: String
)

data class EmergencyAlert(
    val id: String,
    val type: String,
    val level: RiskLevel,
    val title: String,
    val district: String,
    val probability: Int,
    val message: String,
    val action: String,
    val issuedAt: String,
    val issuedBy: String
)

data class EmergencyHelpline(
    val id: String,
    val category: String,
    val name: String,
    val number: String,
    val description: String
)

data class AiModelWeights(
    var rainfall: Float = 0.30f,
    var soilMoisture: Float = 0.25f,
    var groundMovement: Float = 0.20f,
    var slopeAngle: Float = 0.15f,
    var terrainGeology: Float = 0.10f,
    var autoAlertTriggerThreshold: Int = 75,
    var predictiveHorizonHours: Int = 6
)

data class SystemHealthNode(
    val component: String,
    var status: String,
    var latency: String,
    val uptime: String,
    var note: String? = null
)

enum class AppPortal(val displayName: String, val roleKey: String) {
    CITIZEN("Resident Portal", "citizen"),
    AUTHORITY("Command Authority", "authority"),
    FIELD_OFFICER("Field Unit", "fieldOfficer"),
    ADMIN("Super Admin", "admin")
}
