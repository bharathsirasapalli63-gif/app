package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppPortal
import com.example.ui.admin.AdminScreen
import com.example.ui.authority.AuthorityScreen
import com.example.ui.citizen.CitizenOnboardingView
import com.example.ui.citizen.CitizenPortalView
import com.example.ui.common.PortalSwitcherModal
import com.example.ui.common.SosEmergencyDialog
import com.example.ui.common.Terrain3dSheet
import com.example.ui.common.TerraHeader
import com.example.ui.fieldofficer.FieldOfficerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TerraAlertViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TerraAlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val activePortal by viewModel.activePortal.collectAsStateWithLifecycle()
                val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
                val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
                val citizenTab by viewModel.citizenActiveTab.collectAsStateWithLifecycle()
                val selectedZoneId by viewModel.selectedZoneId.collectAsStateWithLifecycle()
                val activeTaskId by viewModel.activeFieldTaskId.collectAsStateWithLifecycle()
                val isSosOpen by viewModel.isSosOpen.collectAsStateWithLifecycle()
                val isTerrain3DOpen by viewModel.isTerrain3DOpen.collectAsStateWithLifecycle()
                val isAuthModalOpen by viewModel.isAuthModalOpen.collectAsStateWithLifecycle()
                val pendingAuthPortal by viewModel.pendingAuthPortal.collectAsStateWithLifecycle()
                val authError by viewModel.authError.collectAsStateWithLifecycle()
                val mapLayers by viewModel.mapLayers.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

                val locations by viewModel.locations.collectAsStateWithLifecycle()
                val sensors by viewModel.sensors.collectAsStateWithLifecycle()
                val roads by viewModel.roads.collectAsStateWithLifecycle()
                val fieldTasks by viewModel.fieldTasks.collectAsStateWithLifecycle()
                val alerts by viewModel.alerts.collectAsStateWithLifecycle()
                val helplines by viewModel.helplines.collectAsStateWithLifecycle()
                val aiConfig by viewModel.aiConfig.collectAsStateWithLifecycle()
                val systemHealth by viewModel.systemHealth.collectAsStateWithLifecycle()
                val citizenReports by viewModel.citizenReports.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(toastMessage) {
                    toastMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        if (onboardingCompleted) {
                            TerraHeader(
                                activePortal = activePortal,
                                onOpenPortalSwitcher = {
                                    viewModel.requestSwitchPortal(
                                        when (activePortal) {
                                            AppPortal.CITIZEN -> AppPortal.AUTHORITY
                                            AppPortal.AUTHORITY -> AppPortal.FIELD_OFFICER
                                            AppPortal.FIELD_OFFICER -> AppPortal.ADMIN
                                            AppPortal.ADMIN -> AppPortal.CITIZEN
                                        }
                                    )
                                },
                                onOpenSos = { viewModel.toggleSosDialog(true) },
                                selectedLanguage = selectedLanguage,
                                onSelectLanguage = { viewModel.setLanguage(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (!onboardingCompleted) {
                            CitizenOnboardingView(
                                selectedLanguage = selectedLanguage,
                                onSelectLanguage = { viewModel.setLanguage(it) },
                                onFinishOnboarding = { viewModel.setOnboardingCompleted(true) }
                            )
                        } else {
                            when (activePortal) {
                                AppPortal.CITIZEN -> CitizenPortalView(
                                    activeTab = citizenTab,
                                    onTabSelect = { viewModel.setCitizenTab(it) },
                                    locations = locations,
                                    sensors = sensors,
                                    roads = roads,
                                    alerts = alerts,
                                    citizenReports = citizenReports,
                                    helplines = helplines,
                                    selectedZoneId = selectedZoneId,
                                    mapLayers = mapLayers,
                                    onSelectZone = { viewModel.selectZone(it) },
                                    onToggle3D = { viewModel.toggleTerrain3D(true) },
                                    onOpenSos = { viewModel.toggleSosDialog(true) },
                                    onSubmitReport = { cat, desc, loc, photo ->
                                        viewModel.submitReport(cat, desc, loc, photo)
                                    },
                                    onBasemapChange = { viewModel.setBasemapType(it) },
                                    selectedLanguage = selectedLanguage
                                )

                                AppPortal.AUTHORITY -> AuthorityScreen(
                                    locations = locations,
                                    sensors = sensors,
                                    roads = roads,
                                    reports = citizenReports,
                                    selectedZoneId = selectedZoneId,
                                    mapLayers = mapLayers,
                                    onSelectZone = { viewModel.selectZone(it) },
                                    onToggleMapLayer = { viewModel.toggleMapLayer(it) },
                                    onVerifyReport = { viewModel.verifyCitizenReport(it) },
                                    onRejectReport = { viewModel.rejectCitizenReport(it) },
                                    onAssignReport = { id, officer -> viewModel.assignReportToOfficer(id, officer) },
                                    onBroadcastAlert = { title, msg, dist, lvl, act ->
                                        viewModel.broadcastAlert(title, msg, dist, lvl, act)
                                    },
                                    onToggleRoadStatus = { id, status -> viewModel.toggleRoadStatus(id, status) },
                                    onBasemapChange = { viewModel.setBasemapType(it) }
                                )

                                AppPortal.FIELD_OFFICER -> FieldOfficerScreen(
                                    tasks = fieldTasks,
                                    activeTaskId = activeTaskId,
                                    onSelectTask = { viewModel.selectFieldTask(it) },
                                    onAdvanceWorkflow = { viewModel.advanceFieldTaskStep(it) },
                                    onSubmitInspection = { id, data -> viewModel.submitInspection(id, data) },
                                    onOpenSos = { viewModel.toggleSosDialog(true) }
                                )

                                AppPortal.ADMIN -> AdminScreen(
                                    sensors = sensors,
                                    healthNodes = systemHealth,
                                    aiConfig = aiConfig,
                                    onDispatchRepair = { viewModel.dispatchSensorRepair(it) },
                                    onUpdateAiConfig = { viewModel.updateAiConfig(it) }
                                )
                            }
                        }

                        // Shared Dialogs & Bottom Sheets
                        PortalSwitcherModal(
                            isOpen = isAuthModalOpen,
                            pendingPortal = pendingAuthPortal,
                            authError = authError,
                            onDismiss = { viewModel.dismissAuthModal() },
                            onSelectPortal = { viewModel.requestSwitchPortal(it) },
                            onSubmitAuth = { u, p -> viewModel.authenticatePortal(u, p) },
                            onAutoFillAndLogin = { viewModel.autoFillAndLogin(it) }
                        )

                        SosEmergencyDialog(
                            isOpen = isSosOpen,
                            onDismiss = { viewModel.toggleSosDialog(false) }
                        )

                        val currentZone = locations.find { it.id == selectedZoneId } ?: locations.firstOrNull()
                        Terrain3dSheet(
                            isOpen = isTerrain3DOpen,
                            zone = currentZone,
                            onDismiss = { viewModel.toggleTerrain3D(false) }
                        )
                    }
                }
            }
        }
    }
}
