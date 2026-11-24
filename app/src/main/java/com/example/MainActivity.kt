package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DataStoreDailyHealthRepository
import com.example.data.dailyHealthDataStore
import com.example.ui.SeniorCareScreen
import com.example.ui.SeniorCareViewModel
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.SeniorCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = IvoryBackground.toArgb(),
                darkScrim = IvoryBackground.toArgb(),
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = IvoryBackground.toArgb(),
                darkScrim = IvoryBackground.toArgb(),
            ),
        )

        val repository = DataStoreDailyHealthRepository(applicationContext.dailyHealthDataStore)

        setContent {
            val viewModel: SeniorCareViewModel = viewModel(
                factory = SeniorCareViewModel.Factory(repository),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val lifecycleOwner = LocalLifecycleOwner.current
            val context = LocalContext.current

            DisposableEffect(lifecycleOwner, context) {
                val lifecycleObserver = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.refreshDate()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

                val dateChangeReceiver = object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context?, intent: Intent?) {
                        viewModel.refreshDate()
                    }
                }
                val dateFilter = IntentFilter().apply {
                    addAction(Intent.ACTION_DATE_CHANGED)
                    addAction(Intent.ACTION_TIME_CHANGED)
                    addAction(Intent.ACTION_TIMEZONE_CHANGED)
                }
                ContextCompat.registerReceiver(
                    context,
                    dateChangeReceiver,
                    dateFilter,
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                    context.unregisterReceiver(dateChangeReceiver)
                }
            }

            SeniorCareTheme {
                SeniorCareScreen(
                    uiState = uiState,
                    onToggleMedication = viewModel::toggleMedication,
                    onIncrementWater = viewModel::incrementWater,
                    onDecrementWater = viewModel::decrementWater,
                )
            }
        }
    }
}
