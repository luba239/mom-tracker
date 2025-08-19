package org.luba239.mom_tracker

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TimerSession(
    val startTime: Long,
    val endTime: Long,
    val duration: Long
)

data class TimerState(
    val isRunning: Boolean = false,
    val currentSessionStart: Long = 0L,
    val totalSeconds: Long = 0L,
    val sessions: List<TimerSession> = emptyList()
)

class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private val _timeSinceLastSession = MutableStateFlow(0L)
    val timeSinceLastSession: StateFlow<Long> = _timeSinceLastSession.asStateFlow()
    
    private var timerJob: kotlinx.coroutines.Job? = null
    private var updateJob: kotlinx.coroutines.Job? = null

    fun startTimer() {
        Log.d("TimerViewModel", "startTimer called. State: ${_timerState.value}")
        if (_timerState.value.isRunning) return
        
        // Cancel any existing timer job
        timerJob?.cancel()
        updateJob?.cancel()  // Stop updating time since last session
        
        val currentTime = System.currentTimeMillis()
        
        // Reset time since last session immediately
        _timeSinceLastSession.value = 0L
        
        // Start new session
        _timerState.value = _timerState.value.copy(
            isRunning = true,
            currentSessionStart = currentTime,
            totalSeconds = 0L
        )
        
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_timerState.value.isRunning) {
                    val elapsed = (System.currentTimeMillis() - _timerState.value.currentSessionStart) / 1000
                    _timerState.value = _timerState.value.copy(
                        totalSeconds = elapsed
                    )
                } else {
                    break
                }
            }
        }
    }

    fun stopTimer() {
        if (!_timerState.value.isRunning) return

        Log.d("TimerViewModel", "stopTimer called. State: ${_timerState.value}")
        
        val currentTime = System.currentTimeMillis()
        val duration = (currentTime - _timerState.value.currentSessionStart) / 1000
        val currentSession = TimerSession(
            startTime = _timerState.value.currentSessionStart,
            endTime = currentTime,
            duration = duration
        )
        
        // Add session to history and stop timer
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            sessions = _timerState.value.sessions + currentSession
        )
        
        timerJob?.cancel()
        timerJob = null
        
        // Start updating time since last session
        startUpdatingTimeSinceLastSession()
    }
    
    private fun startUpdatingTimeSinceLastSession() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                updateTimeSinceLastSession()
            }
        }
    }
    
    private fun updateTimeSinceLastSession() {
        val sessions = _timerState.value.sessions
        if (sessions.isNotEmpty()) {
            val lastSession = sessions.last()
            val timeSince = (System.currentTimeMillis() - lastSession.endTime) / 1000
            _timeSinceLastSession.value = timeSince
        }
    }

    fun resetTimer() {
        _timerState.value = TimerState()
        _timeSinceLastSession.value = 0L
        timerJob?.cancel()
        timerJob = null
        updateJob?.cancel()
    }

    fun formatTime(): String {
        val totalSeconds = if (_timerState.value.isRunning) {
            (System.currentTimeMillis() - _timerState.value.currentSessionStart) / 1000
        } else {
            _timerState.value.totalSeconds
        }
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
    
    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return when {
            hours > 0 -> String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
            else -> String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
    }
}
