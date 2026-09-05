package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CitizenReportEntity
import com.example.data.model.*
import com.example.data.repository.TerraAlertRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class GisBasemapType(val displayName: String, val iconLabel: String) {
    TOPOGRAPHY("Topography", "⛰️ Topo"),
    SATELLITE("Satellite", "🛰️ Satellite"),
    RADAR("Radar GIS", "📡 Radar")
}

data class MapLayerConfig(
    val riskZones: Boolean = true,
    val sensors: Boolean = true,
    val roads: Boolean = true,
    val shelters: Boolean = true,
    val rainRadar: Boolean = true,
    val weatherOverlay: Boolean = true,
    val basemapType: GisBasemapType = GisBasemapType.TOPOGRAPHY
)

class TerraAlertViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TerraAlertRepository(database.citizenReportDao(), viewModelScope)

    // Data streams from repository
    val locations: StateFlow<List<LocationZone>> = repository.locations
    val sensors: StateFlow<List<SensorNode>> = repository.sensors
    val roads: StateFlow<List<RoadCorridor>> = repository.roads
    val fieldTasks: StateFlow<List<FieldTask>> = repository.fieldTasks
    val alerts: StateFlow<List<EmergencyAlert>> = repository.alerts
    val helplines: StateFlow<List<EmergencyHelpline>> = repository.helplines
    val aiConfig: StateFlow<AiModelWeights> = repository.aiConfig
    val systemHealth: StateFlow<List<SystemHealthNode>> = repository.systemHealth
    val citizenReports: StateFlow<List<CitizenReportEntity>> = repository.allDbReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _activePortal = MutableStateFlow(AppPortal.CITIZEN)
    val activePortal: StateFlow<AppPortal> = _activePortal.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _citizenActiveTab = MutableStateFlow("home")
    val citizenActiveTab: StateFlow<String> = _citizenActiveTab.asStateFlow()

    private val _authorityActiveTab = MutableStateFlow("dashboard")
    val authorityActiveTab: StateFlow<String> = _authorityActiveTab.asStateFlow()

    private val _adminActiveTab = MutableStateFlow("sensors")
    val adminActiveTab: StateFlow<String> = _adminActiveTab.asStateFlow()

    private val _selectedZoneId = MutableStateFlow("loc-mangan")
    val selectedZoneId: StateFlow<String> = _selectedZoneId.asStateFlow()

    private val _activeFieldTaskId = MutableStateFlow("TASK-801")
    val activeFieldTaskId: StateFlow<String> = _activeFieldTaskId.asStateFlow()

    private val _isSosOpen = MutableStateFlow(false)
    val isSosOpen: StateFlow<Boolean> = _isSosOpen.asStateFlow()

    private val _isTerrain3DOpen = MutableStateFlow(false)
    val isTerrain3DOpen: StateFlow<Boolean> = _isTerrain3DOpen.asStateFlow()

    private val _isAuthModalOpen = MutableStateFlow(false)
    val isAuthModalOpen: StateFlow<Boolean> = _isAuthModalOpen.asStateFlow()

    private val _pendingAuthPortal = MutableStateFlow<AppPortal?>(null)
    val pendingAuthPortal: StateFlow<AppPortal?> = _pendingAuthPortal.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _mapLayers = MutableStateFlow(MapLayerConfig())
    val mapLayers: StateFlow<MapLayerConfig> = _mapLayers.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Authenticated role records (session based)
    private val authenticatedPortals = mutableSetOf<AppPortal>(AppPortal.CITIZEN)

    fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setCitizenTab(tab: String) {
        _citizenActiveTab.value = tab
    }

    fun setAuthorityTab(tab: String) {
        _authorityActiveTab.value = tab
    }

    fun setAdminTab(tab: String) {
        _adminActiveTab.value = tab
    }

    fun selectZone(zoneId: String) {
        _selectedZoneId.value = zoneId
    }

    fun selectFieldTask(taskId: String) {
        _activeFieldTaskId.value = taskId
    }

    fun toggleSosDialog(isOpen: Boolean) {
        _isSosOpen.value = isOpen
    }

    fun toggleTerrain3D(isOpen: Boolean) {
        _isTerrain3DOpen.value = isOpen
    }

    fun toggleMapLayer(layerName: String) {
        val current = _mapLayers.value
        _mapLayers.value = when (layerName) {
            "riskZones" -> current.copy(riskZones = !current.riskZones)
            "sensors" -> current.copy(sensors = !current.sensors)
            "roads" -> current.copy(roads = !current.roads)
            "shelters" -> current.copy(shelters = !current.shelters)
            "rainRadar" -> current.copy(rainRadar = !current.rainRadar)
            "weatherOverlay" -> current.copy(weatherOverlay = !current.weatherOverlay)
            else -> current
        }
    }

    fun setBasemapType(type: GisBasemapType) {
        _mapLayers.value = _mapLayers.value.copy(basemapType = type)
        showToast("Switched map basemap to ${type.displayName}")
    }

    fun requestSwitchPortal(target: AppPortal) {
        if (target == AppPortal.CITIZEN || authenticatedPortals.contains(target)) {
            _activePortal.value = target
        } else {
            _pendingAuthPortal.value = target
            _authError.value = null
            _isAuthModalOpen.value = true
        }
    }

    fun dismissAuthModal() {
        _isAuthModalOpen.value = false
        _pendingAuthPortal.value = null
        _authError.value = null
    }

    fun authenticatePortal(username: String, pass: String): Boolean {
        val target = _pendingAuthPortal.value ?: return false
        val valid = when (target) {
            AppPortal.AUTHORITY -> username.trim().lowercase() == "authority" && pass == "disaster2026"
            AppPortal.FIELD_OFFICER -> username.trim().lowercase() == "officer" && pass == "field2026"
            AppPortal.ADMIN -> username.trim().lowercase() == "admin" && pass == "admin2026"
            AppPortal.CITIZEN -> true
        }

        if (valid) {
            authenticatedPortals.add(target)
            _activePortal.value = target
            _isAuthModalOpen.value = false
            _pendingAuthPortal.value = null
            _authError.value = null
            showToast("Switched to ${target.displayName}")
            return true
        } else {
            _authError.value = "Invalid credentials. Use demo autofill to test quickly."
            return false
        }
    }

    fun autoFillAndLogin(portal: AppPortal) {
        authenticatedPortals.add(portal)
        _activePortal.value = portal
        _isAuthModalOpen.value = false
        _pendingAuthPortal.value = null
        _authError.value = null
        showToast("Switched to ${portal.displayName} (Demo Mode)")
    }

    fun submitReport(hazardType: String, description: String, locationName: String, photoUrl: String?) {
        viewModelScope.launch {
            val id = repository.submitReport(
                hazardType = hazardType,
                description = description,
                locationName = locationName,
                latitude = 27.518,
                longitude = 88.536,
                photoUrl = photoUrl
            )
            showToast("Report #$id dispatched to Emergency Command Queue")
        }
    }

    fun verifyCitizenReport(reportId: String) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, "VERIFIED")
            showToast("Report #$reportId verified by Authority")
        }
    }

    fun rejectCitizenReport(reportId: String) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, "REJECTED")
            showToast("Report #$reportId marked as invalid")
        }
    }

    fun assignReportToOfficer(reportId: String, officerName: String) {
        viewModelScope.launch {
            repository.assignReportOfficer(reportId, officerName)
            showToast("Report #$reportId dispatched to $officerName")
        }
    }

    fun advanceFieldTaskStep(taskId: String) {
        val task = fieldTasks.value.find { it.id == taskId } ?: return
        val steps = WorkflowStep.values()
        val currentIndex = steps.indexOf(task.status)
        if (currentIndex < steps.size - 1) {
            val nextStep = steps[currentIndex + 1]
            repository.updateTaskStep(taskId, nextStep)
            showToast("Task updated to: ${nextStep.label}")
        }
    }

    fun submitInspection(taskId: String, inspection: InspectionResult) {
        repository.submitInspectionData(taskId, inspection)
        showToast("Field geotechnical telemetry uploaded successfully")
    }

    fun broadcastAlert(title: String, message: String, district: String, level: RiskLevel, action: String) {
        repository.broadcastAlert(title, message, district, level, action)
        showToast("Emergency alert transmitted to $district")
    }

    fun toggleRoadStatus(roadId: String, newStatus: RoadStatus) {
        repository.toggleRoadStatus(roadId, newStatus)
        showToast("Highway traffic status updated to ${newStatus.name}")
    }

    fun dispatchSensorRepair(sensorId: String) {
        repository.dispatchSensorRepair(sensorId)
        showToast("Repair order issued for Sensor $sensorId. Station restored.")
    }

    fun updateAiConfig(newWeights: AiModelWeights) {
        repository.updateAiConfig(newWeights)
        showToast("Ensemble weight parameters synchronized")
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
