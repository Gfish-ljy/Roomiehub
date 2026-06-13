package com.dorm.health

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.dorm.health.data.model.SensorMode
import com.dorm.health.ui.navigation.DormHealthNavHost
import com.dorm.health.ui.theme.DormHealthTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs = DormHealthApp.instance.preferencesManager
            val darkOverride by prefs.darkModeOverrideFlow.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val permissionState = rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )

            val isOnline by DormHealthApp.instance.networkMonitor.isOnline
                .collectAsState(initial = true)

            if (!permissionState.allPermissionsGranted && !permissionState.shouldShowRationale) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    permissionState.launchMultiplePermissionRequest()
                }
            }

            androidx.compose.runtime.LaunchedEffect(isOnline) {
                if (!isOnline && prefs.sensorMode == SensorMode.SERVER) {
                    Toast.makeText(
                        this@MainActivity,
                        "网络不可用，请确认手机与电脑处于同一局域网",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            key(darkOverride, systemDark) {
                DormHealthTheme(darkThemeOverride = darkOverride) {
                    DormHealthNavHost()
                }
            }
        }
    }
}
