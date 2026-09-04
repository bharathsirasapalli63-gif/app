package com.example.data.repository

import com.example.data.local.CitizenReportDao
import com.example.data.local.CitizenReportEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TerraAlertRepository(
    private val citizenReportDao: CitizenReportDao,
    private val scope: CoroutineScope
) {
    val allDbReports: Flow<List<CitizenReportEntity>> = citizenReportDao.getAllReports()

    private val _locations = MutableStateFlow<List<LocationZone>>(initialLocationsList)
    val locations: StateFlow<List<LocationZone>> = _locations.asStateFlow()

    private val _sensors = MutableStateFlow<List<SensorNode>>(initialSensorsList)
    val sensors: StateFlow<List<SensorNode>> = _sensors.asStateFlow()

    private val _roads = MutableStateFlow<List<RoadCorridor>>(initialRoadsList)
    val roads: StateFlow<List<RoadCorridor>> = _roads.asStateFlow()

    private val _fieldTasks = MutableStateFlow<List<FieldTask>>(initialTasksList)
    val fieldTasks: StateFlow<List<FieldTask>> = _fieldTasks.asStateFlow()

    private val _alerts = MutableStateFlow<List<EmergencyAlert>>(initialAlertsList)
    val alerts: StateFlow<List<EmergencyAlert>> = _alerts.asStateFlow()

    private val _helplines = MutableStateFlow<List<EmergencyHelpline>>(initialHelplinesList)
    val helplines: StateFlow<List<EmergencyHelpline>> = _helplines.asStateFlow()

    private val _aiConfig = MutableStateFlow(AiModelWeights())
    val aiConfig: StateFlow<AiModelWeights> = _aiConfig.asStateFlow()

    private val _systemHealth = MutableStateFlow(initialHealthList)
    val systemHealth: StateFlow<List<SystemHealthNode>> = _systemHealth.asStateFlow()

    init {
        // Seed default citizen reports if DB is empty
        scope.launch(Dispatchers.IO) {
            val initialEntities = listOf(
                CitizenReportEntity(
                    id = "CR-10245",
                    hazardType = "Landslide",
                    description = "Huge mud and boulders fell across both lanes near the Mangan bridge. Mud is still trickling down the hill slope.",
                    locationName = "Near Chungthang Junction, North Sikkim",
                    latitude = 27.518,
                    longitude = 88.536,
                    timestamp = "08:35 AM",
                    status = "VERIFIED",
                    assignedOfficer = "Field Officer T. Dorjee (FO-402)",
                    photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
                    reporterName = "Dorji Tenzing (Local Citizen)"
                ),
                CitizenReportEntity(
                    id = "CR-10246",
                    hazardType = "Ground Crack",
                    description = "Deep tensile ground crack of width ~15cm opened behind 4 residential houses on the Zubza slope.",
                    locationName = "Zubza Village Ward 3, Kohima",
                    latitude = 25.675,
                    longitude = 94.109,
                    timestamp = "09:05 AM",
                    status = "PENDING",
                    assignedOfficer = null,
                    photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b",
                    reporterName = "Neiketuo Angami (Resident)"
                ),
                CitizenReportEntity(
                    id = "CR-10247",
                    hazardType = "Water Seepage",
                    description = "Heavy turbid spring water gushing from retaining wall weep holes onto the roadway.",
                    locationName = "Lower Haflong Railway Road",
                    latitude = 25.174,
                    longitude = 93.024,
                    timestamp = "07:50 AM",
                    status = "ASSIGNED",
                    assignedOfficer = "Field Officer Rajesh Das (FO-208)",
                    photoUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05",
                    reporterName = "Bishnu Prasad (Commuter)"
                )
            )
            citizenReportDao.insertAll(initialEntities)
        }
    }

    suspend fun submitReport(
        hazardType: String,
        description: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        photoUrl: String?
    ): String {
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val id = "CR-${(10250..99999).random()}"
        val entity = CitizenReportEntity(
            id = id,
            hazardType = hazardType,
            description = description,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            timestamp = timeStr,
            status = "PENDING",
            assignedOfficer = null,
            photoUrl = photoUrl,
            reporterName = "Citizen Field Reporter"
        )
        citizenReportDao.insertReport(entity)
        return id
    }

    suspend fun updateReportStatus(reportId: String, status: String) {
        citizenReportDao.updateStatus(reportId, status)
    }

    suspend fun assignReportOfficer(reportId: String, officerName: String) {
        citizenReportDao.assignOfficer(reportId, "ASSIGNED", officerName)
        // Add new field task
        val task = FieldTask(
            id = "TASK-${(810..990).random()}",
            title = "Verify Report $reportId: Hazard Assessment",
            sourceReportId = reportId,
            assignedTo = officerName,
            priority = TaskPriority.HIGH,
            status = WorkflowStep.ASSIGNED,
            locationName = "Field Location ($reportId)",
            latitude = 27.518,
            longitude = 88.536,
            taskType = "On-site Verification",
            assignedAt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
            deadline = "Within 2 hours",
            instructions = "Inspect reported site, take geotechnical measurements, and upload fracture analysis."
        )
        _fieldTasks.value = listOf(task) + _fieldTasks.value
    }

    fun updateTaskStep(taskId: String, step: WorkflowStep) {
        _fieldTasks.value = _fieldTasks.value.map { task ->
            if (task.id == taskId) task.copy(status = step) else task
        }
    }

    fun submitInspectionData(taskId: String, inspection: InspectionResult) {
        _fieldTasks.value = _fieldTasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(
                    status = WorkflowStep.REPORT_SUBMITTED,
                    inspectionData = inspection
                )
            } else task
        }
    }

    fun broadcastAlert(title: String, message: String, district: String, level: RiskLevel, action: String) {
        val newAlert = EmergencyAlert(
            id = "ALT-${(910..999).random()}",
            type = "EMERGENCY_BROADCAST",
            level = level,
            title = title,
            district = district,
            probability = if (level == RiskLevel.CRITICAL) 88 else 65,
            message = message,
            action = action,
            issuedAt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
            issuedBy = "State Disaster Management Authority EOC"
        )
        _alerts.value = listOf(newAlert) + _alerts.value
    }

    fun toggleRoadStatus(roadId: String, newStatus: RoadStatus) {
        _roads.value = _roads.value.map { road ->
            if (road.id == roadId) road.copy(status = newStatus) else road
        }
    }

    fun dispatchSensorRepair(sensorId: String) {
        _sensors.value = _sensors.value.map { sensor ->
            if (sensor.id == sensorId) {
                sensor.copy(
                    status = SensorStatus.ONLINE,
                    battery = 95,
                    faultDescription = null,
                    lastPing = "Just now"
                )
            } else sensor
        }
    }

    fun updateAiConfig(newWeights: AiModelWeights) {
        _aiConfig.value = newWeights
    }

    companion object {
        val initialLocationsList = listOf(
            LocationZone(
                id = "loc-mangan",
                name = "Mangan - Chungthang Highway Corridor",
                district = "North Sikkim",
                state = "Sikkim",
                latitude = 27.512,
                longitude = 88.534,
                riskPercentage = 86,
                riskLevel = RiskLevel.CRITICAL,
                rainfall = 112.0,
                soilMoisture = 91.0,
                groundMovement = 5.8,
                slopeAngle = 42.0,
                terrainType = "Fractured Gneiss & Colluvium",
                predictionWindow = "High landslide probability in next 4-6 hours.",
                whyElevated = "Monsoon rainfall has saturated overburden soil on a 42° slope. Micro-seismic geophones show progressive deep-seated shear displacement.",
                safetyPrecautions = listOf(
                    "Immediate evacuation from slope-toe dwellings to Chungthang Higher Secondary School shelter.",
                    "Total ban on non-emergency vehicular movement along North Sikkim Highway.",
                    "Stay strictly clear of mountain rivulets prone to sudden debris flows.",
                    "Maintain active communication via VHF/satellite phone stations."
                ),
                populationAtRisk = 14200,
                shelters = listOf(
                    EvacuationShelter("Chungthang Community Hall Relief Camp", 27.598, 88.647, 450, 180),
                    EvacuationShelter("Mangan District Sports Ground Shelter", 27.508, 88.528, 800, 320)
                ),
                weatherTemp = 18,
                weatherCondition = "Heavy Rain & Mist",
                weatherWind = "28 km/h NW",
                recentEarthquakes = listOf(
                    EarthquakeEvent(2025, "14 Oct", 4.8, "18 km NW", "Induced slope toe shear failure"),
                    EarthquakeEvent(2024, "08 Jul", 5.2, "34 km N", "Rockfall triggering on upper ridge")
                )
            ),
            LocationZone(
                id = "loc-haflong",
                name = "Haflong - Jatinga Valley Slopes",
                district = "Dima Hasao",
                state = "Assam",
                latitude = 25.176,
                longitude = 93.023,
                riskPercentage = 74,
                riskLevel = RiskLevel.HIGH,
                rainfall = 78.0,
                soilMoisture = 84.0,
                groundMovement = 3.2,
                slopeAngle = 36.0,
                terrainType = "Soft Tertiary Shale & Siltstone",
                predictionWindow = "Elevated landslide probability in next 12 hours.",
                whyElevated = "Persistent precipitation has liquefied fine-grained shale formations. Inclinometer S-104 reports ongoing tilt creep towards railway cutting.",
                safetyPrecautions = listOf(
                    "Lumding-Badarpur hill rail section under precautionary speed restriction.",
                    "Avoid traveling along NH-54E during nighttime rain spells.",
                    "Inspect hillside retaining walls for expanding tension cracks."
                ),
                populationAtRisk = 8600,
                shelters = listOf(
                    EvacuationShelter("Haflong Government College Shelter", 25.182, 93.029, 600, 110)
                ),
                weatherTemp = 24,
                weatherCondition = "Torrential Downpour",
                weatherWind = "22 km/h SE",
                recentEarthquakes = listOf(
                    EarthquakeEvent(2024, "19 May", 4.4, "22 km SE", "Shallow tremors recorded"),
                    EarthquakeEvent(2022, "02 Jun", 5.0, "45 km W", "Minor soil subsidence")
                )
            ),
            LocationZone(
                id = "loc-kohima",
                name = "Kohima - Zubza Bypass Sinking Zone",
                district = "Kohima",
                state = "Nagaland",
                latitude = 25.674,
                longitude = 94.108,
                riskPercentage = 88,
                riskLevel = RiskLevel.CRITICAL,
                rainfall = 94.0,
                soilMoisture = 88.0,
                groundMovement = 6.4,
                slopeAngle = 38.0,
                terrainType = "Dishkai Schist / Loose Regolith",
                predictionWindow = "Imminent landslide & road sinking hazard within 2 to 4 hours.",
                whyElevated = "Extensometers at Zubza report rapid tensile fracture widening (>12 mm/hr). Sub-surface water pressure is forcing translational debris slide.",
                safetyPrecautions = listOf(
                    "NH-29 closed at Zubza; traffic diverted via bypass route.",
                    "Residents within 200m contour line ordered to relocate to Zubza Community Center.",
                    "Heavy vehicles barred indefinitely."
                ),
                populationAtRisk = 19500,
                shelters = listOf(
                    EvacuationShelter("Zubza Indoor Stadium Emergency Center", 25.688, 94.062, 500, 290),
                    EvacuationShelter("Kohima Local Ground Evacuation Shelter", 25.669, 94.104, 1200, 410)
                ),
                weatherTemp = 20,
                weatherCondition = "Severe Storm & Lightning",
                weatherWind = "32 km/h E",
                recentEarthquakes = listOf(
                    EarthquakeEvent(2025, "22 Jan", 5.1, "25 km NE", "Widened active road subsidence"),
                    EarthquakeEvent(2023, "11 Nov", 4.7, "40 km S", "Cracks observed in culverts")
                )
            ),
            LocationZone(
                id = "loc-cherrapunji",
                name = "Sohra - Cherrapunji Gorges",
                district = "East Khasi Hills",
                state = "Meghalaya",
                latitude = 25.298,
                longitude = 91.732,
                riskPercentage = 48,
                riskLevel = RiskLevel.MODERATE,
                rainfall = 145.0,
                soilMoisture = 65.0,
                groundMovement = 1.1,
                slopeAngle = 45.0,
                terrainType = "Massive Sandstone Plateau Escarpment",
                predictionWindow = "Moderate rockfall hazard; high flash flood probability.",
                whyElevated = "Excessive rainfall volume drains rapidly over sheer sandstone cliffs. Cliff-edge rockfall is elevated.",
                safetyPrecautions = listOf(
                    "Avoid standing near rim viewpoints during dense cloud mist.",
                    "Watch for flash flooding in canyon riverbeds.",
                    "Follow District Disaster Management Meghalaya advisories."
                ),
                populationAtRisk = 5200,
                shelters = listOf(
                    EvacuationShelter("Sohra Civil Sub-Division Shelter", 25.302, 91.728, 350, 40)
                ),
                weatherTemp = 19,
                weatherCondition = "Torrential Rains & Fog",
                weatherWind = "26 km/h S",
                recentEarthquakes = listOf(
                    EarthquakeEvent(2024, "03 Aug", 4.2, "30 km W", "No major surface displacement")
                )
            ),
            LocationZone(
                id = "loc-aizawl",
                name = "Aizawl Western Slopes (Ramhlun / Chite)",
                district = "Aizawl",
                state = "Mizoram",
                latitude = 23.731,
                longitude = 92.717,
                riskPercentage = 68,
                riskLevel = RiskLevel.HIGH,
                rainfall = 62.0,
                soilMoisture = 78.0,
                groundMovement = 2.8,
                slopeAngle = 34.0,
                terrainType = "Steep Sandstone-Shale Interbedding",
                predictionWindow = "High risk of slope creep and retaining wall collapse in 8-12 hours.",
                whyElevated = "Unplanned slope cuts and poor storm drainage overburden steep ridge developments.",
                safetyPrecautions = listOf(
                    "Clear roof drainage pipes away from slope faces.",
                    "Report building foundation fissures immediately to Aizawl Municipal Corporation.",
                    "Prepare emergency grab-bags for quick evacuation."
                ),
                populationAtRisk = 22000,
                shelters = listOf(
                    EvacuationShelter("Ramhlun Indoor Stadium Relief Camp", 23.742, 92.725, 700, 150)
                ),
                weatherTemp = 23,
                weatherCondition = "Intermittent Heavy Showers",
                weatherWind = "16 km/h SW",
                recentEarthquakes = listOf(
                    EarthquakeEvent(2024, "15 Dec", 5.3, "38 km SE", "Widened ground cracks in Ramhlun")
                )
            ),
            LocationZone(
                id = "loc-guwahati",
                name = "Guwahati - Kamrup Foothills",
                district = "Kamrup Metropolitan",
                state = "Assam",
                latitude = 26.144,
                longitude = 91.736,
                riskPercentage = 22,
                riskLevel = RiskLevel.LOW,
                rainfall = 24.0,
                soilMoisture = 46.0,
                groundMovement = 0.4,
                slopeAngle = 14.0,
                terrainType = "Alluvial Plain & Granitic Inliers",
                predictionWindow = "Slope stability normal. Regular urban drainage monitoring active.",
                whyElevated = "Low slope inclination and moderate precipitation maintain healthy slope factors of safety.",
                safetyPrecautions = listOf(
                    "Regular municipal drainage clearance in low-lying sectors.",
                    "Monitor Brahmaputra river gauges for downstream water influx."
                ),
                populationAtRisk = 3000,
                shelters = listOf(
                    EvacuationShelter("Sarussajai Stadium Disaster Resource Center", 26.115, 91.758, 3000, 0)
                ),
                weatherTemp = 29,
                weatherCondition = "Partly Cloudy with Light Rain",
                weatherWind = "12 km/h E",
                recentEarthquakes = listOf(
                    EarthquakeEvent(2021, "28 Apr", 6.4, "85 km N", "Structural tremors felt throughout city")
                )
            )
        )

        val initialSensorsList = listOf(
            SensorNode("S-101", "Mangan Toe Geophone Sensor", SensorType.GROUND_VIBRATION, "Ground Movement / Seismic", "Mangan Pass, Sikkim", 27.514, 88.532, SensorStatus.CRITICAL, 5.8, "mm/s", 84, -68, "Just now", "2024-03-15", "Vibration threshold exceeded (limit: 3.5 mm/s)"),
            SensorNode("S-102", "Mangan Slope Crack Extensometer", SensorType.CRACK_DISPLACEMENT, "Crack Displacement", "Chungthang Road, Sikkim", 27.525, 88.541, SensorStatus.OFFLINE, 0.0, "mm", 12, -115, "38 mins ago", "2024-03-16", "No telemetry received >30 mins. Low battery / hardware fault."),
            SensorNode("S-103", "Haflong TDR Soil Moisture Probe", SensorType.SOIL_MOISTURE, "Soil Moisture", "Jatinga Hill, Assam", 25.178, 93.021, SensorStatus.WARNING, 84.2, "%", 79, -74, "2 mins ago", "2024-05-10", "Moisture approaching plastic limit (85%)"),
            SensorNode("S-104", "Haflong Biaxial Inclinometer", SensorType.TILT_INCLINOMETER, "Tilt / Inclinometer", "Haflong Railway Cutting, Assam", 25.172, 93.025, SensorStatus.ONLINE, 2.8, "° tilt", 92, -62, "1 min ago", "2024-05-11", null),
            SensorNode("S-105", "Zubza Deep Borehole Extensometer", SensorType.CRACK_DISPLACEMENT, "Crack Displacement", "NH-29 Zubza Ridge, Nagaland", 25.676, 94.106, SensorStatus.CRITICAL, 14.6, "mm", 76, -78, "Just now", "2023-11-20", "Rapid crack extension detected: +3.2mm in last 2 hours"),
            SensorNode("S-106", "Kohima Sinking Zone Piezo Sensor", SensorType.GROUND_VIBRATION, "Ground Movement / Seismic", "Zubza Bypass, Nagaland", 25.671, 94.112, SensorStatus.ONLINE, 6.4, "mm/s", 88, -65, "3 mins ago", "2023-11-22", null),
            SensorNode("S-107", "Sohra High-Capacity Rain Gauge", SensorType.RAIN_GAUGE, "Rainfall Gauge", "Cherrapunji Plateau, Meghalaya", 25.295, 91.735, SensorStatus.ONLINE, 145.0, "mm/24h", 95, -58, "1 min ago", "2024-01-08", null),
            SensorNode("S-108", "Aizawl Ramhlun Inclinometer", SensorType.TILT_INCLINOMETER, "Tilt / Inclinometer", "Ramhlun Vengthlang, Mizoram", 23.733, 92.715, SensorStatus.WARNING, 3.4, "° tilt", 64, -82, "4 mins ago", "2024-02-14", "Cumulative tilt displacement over 3°"),
            SensorNode("S-109", "Kamrup Alluvial Monitoring Node", SensorType.SOIL_MOISTURE, "Soil Moisture", "Guwahati Hills, Assam", 26.142, 91.738, SensorStatus.ONLINE, 46.0, "%", 99, -55, "Just now", "2024-06-01", null)
        )

        val initialRoadsList = listOf(
            RoadCorridor(
                id = "road-nh10",
                name = "NH-10 (Siliguri - Gangtok Highway)",
                status = RoadStatus.BLOCKED,
                riskLevel = RiskLevel.CRITICAL,
                riskPercentage = 92,
                reason = "Massive active rockslide at 29th Mile with debris blocking both carriageways.",
                blockedSection = "29th Mile to Teesta Bridge",
                alternativeRouteName = "Alternative Route via Lava - Reshi - Rhenock Pass",
                alternativeRouteRisk = RiskLevel.LOW,
                alternativeRouteRiskPercentage = 24
            ),
            RoadCorridor(
                id = "road-nh29",
                name = "NH-29 (Dimapur - Kohima Highway via Zubza)",
                status = RoadStatus.UNSAFE,
                riskLevel = RiskLevel.CRITICAL,
                riskPercentage = 88,
                reason = "Predicted severe slope failure; 14mm road tension crack actively propagating.",
                blockedSection = "Zubza Sinking Segment km 142-145",
                alternativeRouteName = "Safe Detour via Medziphema - Jakhama Ridge",
                alternativeRouteRisk = RiskLevel.MODERATE,
                alternativeRouteRiskPercentage = 35
            )
        )

        val initialTasksList = listOf(
            FieldTask(
                id = "TASK-801",
                title = "Emergency Slope & Debris Inspection at Mangan Bridge",
                sourceReportId = "CR-10245",
                assignedTo = "FO-402 (T. Dorjee)",
                priority = TaskPriority.CRITICAL,
                status = WorkflowStep.INSPECTION,
                locationName = "Mangan Bridge Corridor, Sikkim",
                latitude = 27.518,
                longitude = 88.536,
                taskType = "Hazard Verification & Geotechnical Telemetry",
                assignedAt = "08:45 AM",
                deadline = "Within 2 hours",
                instructions = "Measure crack extension, evaluate crown tension scarp, assess threat to downstream culvert, and submit AI vision photo analysis.",
                inspectionData = InspectionResult(
                    crackWidthMm = 18.0,
                    slopeTiltDeg = 43.0,
                    rockfallSeverity = "High",
                    waterSeepageRate = "Rapid Turbid Flow",
                    roadDamage = "Severe Structural Fissuring",
                    aiAnalysisResult = "AI Geotechnical Model: 91% match with retrogressive rotational slip failure. High probability of secondary slide within 3 hours.",
                    notes = "Slope toe saturated. Evacuation of 6 roadside houses strongly advised."
                )
            ),
            FieldTask(
                id = "TASK-802",
                title = "Investigate Hardware Failure: Extensometer S-102",
                sourceReportId = "ADMIN-SENSOR-S102",
                assignedTo = "FO-402 (T. Dorjee)",
                priority = TaskPriority.HIGH,
                status = WorkflowStep.ASSIGNED,
                locationName = "Chungthang Road Station S-102",
                latitude = 27.525,
                longitude = 88.541,
                taskType = "IoT Sensor Repair / Battery Replacement",
                assignedAt = "09:15 AM",
                deadline = "Immediate",
                instructions = "Inspect sensor battery pack, solar panel charge controller, and LoRaWAN antenna connection. Restore telemetry feed to central server."
            ),
            FieldTask(
                id = "TASK-803",
                title = "Retaining Wall Seepage Inspection",
                sourceReportId = "CR-10247",
                assignedTo = "FO-208 (Rajesh Das)",
                priority = TaskPriority.MEDIUM,
                status = WorkflowStep.ON_SITE,
                locationName = "Lower Haflong Railway Road",
                latitude = 25.174,
                longitude = 93.024,
                taskType = "Structural & Drainage Assessment",
                assignedAt = "08:00 AM",
                deadline = "Within 4 hours",
                instructions = "Check weep-hole discharge clarity and hydrostatic pressure behind retaining structure."
            )
        )

        val initialAlertsList = listOf(
            EmergencyAlert(
                id = "ALT-901",
                type = "CRITICAL_LANDSLIDE",
                level = RiskLevel.CRITICAL,
                title = "CRITICAL LANDSLIDE WARNING: Mangan-Chungthang Corridor",
                district = "North Sikkim",
                probability = 86,
                message = "Continuous torrential downpour (112mm) and active seismic ground vibration (5.8 mm/s) have created high landslide probability in the next 4-6 hours. Avoid NH-10. Follow evacuation orders.",
                action = "Avoid travel on NH-10. Relocate to designated Chungthang or Mangan relief shelters immediately.",
                issuedAt = "08:15 AM",
                issuedBy = "Sikkim State Disaster Management Authority (SSDMA)"
            ),
            EmergencyAlert(
                id = "ALT-902",
                type = "FLASH_FLOOD",
                level = RiskLevel.HIGH,
                title = "FLASH FLOOD & GORGE HAZARD: Cherrapunji Basin",
                district = "East Khasi Hills",
                probability = 78,
                message = "Rainfall exceeding 145mm/24h in Sohra canyon. Danger of sudden mountain flash floods and roaring debris flows in downstream gorges.",
                action = "Move to higher ground. Stay clear of riverbanks, low culverts, and natural drainage ravines.",
                issuedAt = "07:30 AM",
                issuedBy = "East Khasi Hills District Disaster Management Authority"
            ),
            EmergencyAlert(
                id = "ALT-903",
                type = "ROAD_BLOCKAGE",
                level = RiskLevel.CRITICAL,
                title = "ROAD BLOCKED: NH-10 at 29th Mile",
                district = "North Sikkim",
                probability = 92,
                message = "Active rockslide confirmed and verified. NH-10 completely blocked to vehicular traffic. Use alternative route via Lava - Reshi - Rhenock.",
                action = "Follow alternative green route. Do not attempt crossing blocked section.",
                issuedAt = "08:40 AM",
                issuedBy = "Border Roads Organisation (BRO) & Traffic Control"
            )
        )

        val initialHelplinesList = listOf(
            EmergencyHelpline("hl-1", "National Emergency", "National Disaster Management Helpline", "1078", "Toll-free 24x7 National Disaster Response Force (NDRF) control room"),
            EmergencyHelpline("hl-2", "Emergency Police/Fire/Medical", "Emergency Response Support System (ERSS)", "112", "Pan-India unified 24/7 emergency response"),
            EmergencyHelpline("hl-3", "Disaster Operations", "State Emergency Operations Centre (SEOC) Sikkim", "1070", "Sikkim Disaster Management Operations Room"),
            EmergencyHelpline("hl-4", "Disaster Operations", "SEOC Assam Disaster Control Room", "1079", "Assam State Disaster Management Authority (ASDMA)"),
            EmergencyHelpline("hl-5", "Medical Ambulance", "Mountain Emergency Medical & Trauma Ambulance", "108", "Direct 24/7 mountain ambulance & medical dispatch"),
            EmergencyHelpline("hl-6", "Highway Rescue & Clearance", "Border Roads Organisation (BRO) Control Room", "0361-2640232", "Highway clearance, snow & debris removal reports"),
            EmergencyHelpline("hl-7", "Highway Patrol", "National Highway Traffic Command", "1033", "National Highway safety and roadblock assistance")
        )

        val initialHealthList = listOf(
            SystemHealthNode("Central Reactive Telemetry Broker", "OPERATIONAL", "18ms", "99.98%"),
            SystemHealthNode("AI Landslide Ensemble Inference Engine", "OPERATIONAL", "42ms", "99.94%"),
            SystemHealthNode("GIS Spatial Engine & Tile Pipeline", "OPERATIONAL", "25ms", "99.99%"),
            SystemHealthNode("IoT Sensor Gateway (LoRa/Satellite)", "WARNING", "145ms", "99.10%", "1 sensor offline (S-102)"),
            SystemHealthNode("Emergency Siren & Push Broadcast Engine", "OPERATIONAL", "12ms", "100.0%")
        )
    }
}
