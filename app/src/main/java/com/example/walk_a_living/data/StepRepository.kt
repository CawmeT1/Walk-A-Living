package com.example.walk_a_living.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepRepository(context: Context) {

    val healthConnectManager = HealthConnectManager(context)

    private val _healthConnectStatus = MutableStateFlow(healthConnectManager.checkAvailability())
    val healthConnectStatus: StateFlow<HealthConnectStatus> = _healthConnectStatus.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    fun checkStatus() {
        _healthConnectStatus.value = healthConnectManager.checkAvailability()
    }

    suspend fun updatePermissionState() {
        _hasPermission.value = healthConnectManager.hasAllPermissions()
    }

    suspend fun syncSteps(): Long {
        updatePermissionState()
        return healthConnectManager.fetchTodaySteps()
    }
}
