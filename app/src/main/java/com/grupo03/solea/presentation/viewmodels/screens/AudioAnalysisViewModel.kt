package com.grupo03.solea.presentation.viewmodels.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo03.solea.data.models.Category
import com.grupo03.solea.data.models.EditableVoiceNote
import com.grupo03.solea.data.repositories.interfaces.CategoryRepository
import com.grupo03.solea.data.services.interfaces.AudioAnalyzerService
import com.grupo03.solea.presentation.states.screens.VoiceNoteState
import com.grupo03.solea.utils.AudioRecorderManager
import com.grupo03.solea.utils.CurrencyUtils
import com.grupo03.solea.utils.MovementError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel for the audio analysis screen.
 *
 * Manages the AI-powered voice note recording and analysis workflow.
 * Uses the Gemini API via AudioAnalyzerService to extract financial data from voice recordings
 * including amount, description, movement type, and suggested categories.
 *
 * @property audioAnalyzerService Service for AI-powered audio analysis
 * @property categoryRepository Repository for fetching categories for AI categorization
 */
class AudioAnalysisViewModel(
    private val audioAnalyzerService: AudioAnalyzerService,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    /** Voice note analysis state including recording status, duration, and extracted data */
    private val _state = MutableStateFlow(VoiceNoteState())
    val state: StateFlow<VoiceNoteState> = _state.asStateFlow()

    private var audioRecorder: AudioRecorderManager? = null
    private var recordingTimerJob: Job? = null
    private var currentRecordingFile: File? = null

    /**
     * Initializes the audio recorder with the provided context.
     *
     * Must be called before starting recording.
     *
     * @param context Android context for AudioRecorderManager
     */
    fun initializeRecorder(context: Context) {
        if (audioRecorder == null) {
            audioRecorder = AudioRecorderManager(context)
        }
    }

    /**
     * Updates the permission status.
     *
     * @param granted Whether audio recording permission has been granted
     */
    fun setPermissionGranted(granted: Boolean) {
        _state.value = _state.value.copy(hasPermission = granted)
    }

    /**
     * Starts audio recording.
     *
     * Clears any previous analysis data and starts recording audio.
     * Also starts a timer to track recording duration.
     */
    fun startRecording() {
        if (_state.value.isRecording) return

        audioRecorder?.let { recorder ->
            val result = recorder.startRecording()

            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    isRecording = true,
                    recordingDuration = 0L,
                    analyzedVoiceNote = null,
                    error = null
                )

                // Start recording timer
                startRecordingTimer()
            } else {
                _state.value = _state.value.copy(
                    error = MovementError.UNKNOWN_ERROR
                )
            }
        }
    }

    /**
     * Stops audio recording and triggers analysis.
     *
     * Stops the recording, cancels the timer, and starts the AI analysis process.
     *
     * @param userId User ID for fetching user-specific categories
     */
    fun stopRecordingAndAnalyze(userId: String) {
        if (!_state.value.isRecording) return

        stopRecordingTimer()

        audioRecorder?.let { recorder ->
            val result = recorder.stopRecording()

            if (result.isSuccess) {
                currentRecordingFile = result.getOrNull()
                _state.value = _state.value.copy(isRecording = false)

                // Trigger analysis
                currentRecordingFile?.let { file ->
                    analyzeAudio(file, userId)
                }
            } else {
                _state.value = _state.value.copy(
                    isRecording = false,
                    error = MovementError.UNKNOWN_ERROR
                )
            }
        }
    }

    /**
     * Cancels the current recording.
     *
     * Stops recording and discards the audio file without analyzing it.
     */
    fun cancelRecording() {
        if (!_state.value.isRecording) return

        stopRecordingTimer()

        audioRecorder?.let { recorder ->
            recorder.stopRecording()
            recorder.deleteCurrentRecording()
        }

        _state.value = _state.value.copy(
            isRecording = false,
            recordingDuration = 0L,
            analyzedVoiceNote = null,
            error = null
        )
    }

    /**
     * Analyzes an audio file using AI.
     *
     * Fetches available categories, sends the audio to the AudioAnalyzerService for AI processing,
     * and converts the result to editable format for user review.
     *
     * @param audioFile The audio file to analyze
     * @param userId User ID for fetching user-specific categories
     */
    private fun analyzeAudio(audioFile: File, userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isAnalyzing = true, error = null)

            try {
                // Fetch categories (default + user categories)
                val categories = mutableListOf<Category>()

                val defaultCategoriesResult = categoryRepository.getDefaultCategories()
                if (defaultCategoriesResult.isSuccess) {
                    categories.addAll(defaultCategoriesResult.getOrNull() ?: emptyList())
                }

                val userCategoriesResult = categoryRepository.getCategoriesByUser(userId)
                if (userCategoriesResult.isSuccess) {
                    categories.addAll(userCategoriesResult.getOrNull() ?: emptyList())
                }

                // Get device currency
                val deviceCurrency = CurrencyUtils.getCurrencyByCountry()

                // Call analyzer service with categories and device currency
                val result = audioAnalyzerService.analyzeAudio(audioFile, categories, deviceCurrency)

                if (result.isSuccess) {
                    val analyzedData = result.getOrNull()!!.voiceNote

                    // Convert to editable format
                    val editableVoiceNote = EditableVoiceNote(
                        transcription = analyzedData.transcription,
                        amount = analyzedData.amount.toString(),
                        description = analyzedData.description,
                        movementType = analyzedData.movementType,
                        date = parseDateTime(analyzedData.date),
                        currency = analyzedData.currency,
                        suggestedCategory = analyzedData.suggestedCategory,
                        confidence = analyzedData.confidence
                    )

                    _state.value = _state.value.copy(
                        analyzedVoiceNote = editableVoiceNote,
                        isAnalyzing = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isAnalyzing = false,
                        error = MovementError.UNKNOWN_ERROR
                    )
                }

                // Clean up temp file
                audioFile.delete()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isAnalyzing = false,
                    error = MovementError.UNKNOWN_ERROR
                )
            }
        }
    }

    /**
     * Starts a timer that increments the recording duration every second.
     */
    private fun startRecordingTimer() {
        recordingTimerJob = viewModelScope.launch {
            while (_state.value.isRecording) {
                delay(1000)
                _state.value = _state.value.copy(
                    recordingDuration = _state.value.recordingDuration + 1
                )
            }
        }
    }

    /**
     * Stops the recording timer.
     */
    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
    }

    /**
     * Parses a date-time string to LocalDateTime.
     *
     * @param dateString ISO 8601 date-time string
     * @return Parsed LocalDateTime, or current date-time if parsing fails or string is blank
     */
    private fun parseDateTime(dateString: String?): LocalDateTime? {
        if (dateString.isNullOrBlank()) return LocalDateTime.now()

        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    /**
     * Clears the analysis state, resetting all fields to default values.
     */
    fun clearState() {
        audioRecorder?.release()
        audioRecorder = null
        stopRecordingTimer()
        _state.value = VoiceNoteState()
    }

    /**
     * Releases resources when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        audioRecorder?.release()
        stopRecordingTimer()
    }
}
