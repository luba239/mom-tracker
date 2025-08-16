package org.luba239.mom_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TimerState(
    val isRunning: Boolean = false,
    val totalSeconds: Long = 0L
)

class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    private var timerJob: kotlinx.coroutines.Job? = null

    fun startTimer() {
        if (_timerState.value.isRunning) return
        
        // Cancel any existing timer job
        timerJob?.cancel()
        
        // Reset timer and start
        _timerState.value = TimerState(isRunning = true, totalSeconds = 0L)
        
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_timerState.value.isRunning) {
                    _timerState.value = _timerState.value.copy(
                        totalSeconds = _timerState.value.totalSeconds + 1
                    )
                } else {
                    break
                }
            }
        }
    }

    fun stopTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        _timerState.value = TimerState()
        timerJob?.cancel()
        timerJob = null
    }

    fun formatTime(): String {
        val totalSeconds = _timerState.value.totalSeconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
