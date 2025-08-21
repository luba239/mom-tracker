package org.luba239.mom_tracker

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.luba239.mom_tracker.ui.theme.MomTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MomTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Timer(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Timer(modifier: Modifier = Modifier) {
    val viewModel: TimerViewModel = viewModel(
        factory = TimerViewModelFactory(
            (LocalContext.current.applicationContext as Application)
        )
    )
    val timerState by viewModel.timerState.collectAsState()
    val timeSinceLastSession by viewModel.timeSinceLastSession.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Time display
        Text(
            text = viewModel.formatTime(),
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Last session info (only show when not running and has sessions)
        if (!timerState.isRunning && timerState.sessions.isNotEmpty()) {
            val lastSession = timerState.sessions.last()
            val durationFormatted = viewModel.formatDuration(lastSession.duration)
            val timeSinceFormatted = viewModel.formatDuration(timeSinceLastSession)

            Text(
                text = "Последняя сессия: $durationFormatted\nПрошло времени: $timeSinceFormatted",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // Add spacing when no session info
            Spacer(
                modifier = Modifier.height(32.dp)
            )
        }

        // Timer control button
        Button(
            onClick = {
                if (timerState.isRunning) {
                    viewModel.stopTimer()
                } else {
                    viewModel.startTimer()
                }
            }, colors = ButtonDefaults.buttonColors(
                containerColor = if (timerState.isRunning) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Text(
                text = if (timerState.isRunning) "Stop" else "Start", fontSize = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerPreview() {
    MomTrackerTheme {
        Timer()
    }
}