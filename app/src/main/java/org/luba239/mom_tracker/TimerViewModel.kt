package org.luba239.mom_tracker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.luba239.mom_tracker.database.TimerDatabase
import org.luba239.mom_tracker.database.TimerStateEntity
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

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val timerStateDao = TimerDatabase.getDatabase(application).timerStateDao()
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _timeSinceLastSession = MutableStateFlow(0L)
    val timeSinceLastSession: StateFlow<Long> = _timeSinceLastSession.asStateFlow()
    
    private var timerJob: kotlinx.coroutines.Job? = null
    private var updateJob: kotlinx.coroutines.Job? = null

    init {
        loadState()
    }

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
        saveState()
        
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
        saveState()
        
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

    private fun saveState() {
        viewModelScope.launch {
            val state = _timerState.value
            val entity = TimerStateEntity(
                isRunning = state.isRunning,
                currentSessionStart = state.currentSessionStart,
                totalSeconds = state.totalSeconds,
                sessions = state.sessions
            )
            timerStateDao.saveTimerState(entity)
            Log.d("TimerViewModel", "State saved")
        }
    }

    private fun loadState() {
        viewModelScope.launch {
            val entity = timerStateDao.getTimerState()
            if (entity != null) {
                val state = TimerState(
                    isRunning = entity.isRunning,
                    currentSessionStart = entity.currentSessionStart,
                    totalSeconds = entity.totalSeconds,
                    sessions = entity.sessions
                )
                _timerState.value = state
                Log.d("TimerViewModel", "State loaded")

                if (state.isRunning) {
                    // Recalculate totalSeconds based on the start time
                    val elapsed = (System.currentTimeMillis() - state.currentSessionStart) / 1000
                    _timerState.value = _timerState.value.copy(totalSeconds = elapsed)
                    
                    // Restart the timer job
                    timerJob?.cancel()
                    timerJob = viewModelScope.launch {
                        while (isActive) {
                            delay(1000)
                            if (_timerState.value.isRunning) {
                                val currentElapsed = (System.currentTimeMillis() - _timerState.value.currentSessionStart) / 1000
                                _timerState.value = _timerState.value.copy(totalSeconds = currentElapsed)
                            } else {
                                break
                            }
                        }
                    }
                } else {
                     updateTimeSinceLastSession()
                     startUpdatingTimeSinceLastSession()
                }
            } else {
                Log.d("TimerViewModel", "No state found in database. Initializing with default state.")
                _timerState.value = TimerState()
            }
        }
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
