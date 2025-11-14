package com.grupo03.solea.presentation.states.screens

import com.grupo03.solea.data.models.EditableVoiceNote
import com.grupo03.solea.utils.AppError

/**
 * UI state for the voice note analysis screen.
 *
 * Manages the state of the audio recording and analysis workflow, from recording
 * the voice note to displaying the AI-extracted financial data for editing.
 *
 * @property isRecording Whether audio recording is currently in progress
 * @property recordingDuration Duration of the current recording in seconds
 * @property isAnalyzing Whether the AI analysis/transcription process is in progress
 * @property analyzedVoiceNote AI-extracted financial data ready for user review/editing, null if not analyzed yet
 * @property error Error that occurred during recording or analysis, null if no error
 * @property hasPermission Whether the app has permission to record audio
 */
data class VoiceNoteState(
    val isRecording: Boolean = false,
    val recordingDuration: Long = 0L,
    val isAnalyzing: Boolean = false,
    val analyzedVoiceNote: EditableVoiceNote? = null,
    val error: AppError? = null,
    val hasPermission: Boolean = false
)
