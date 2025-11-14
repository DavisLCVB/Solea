package com.grupo03.solea.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Manages audio recording using MediaRecorder.
 *
 * Handles the lifecycle of audio recording including starting, stopping,
 * and releasing resources. Creates temporary audio files in the app's cache directory.
 *
 * @property context Android context for accessing cache directory
 */
class AudioRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var isRecordingActive = false

    /**
     * Starts audio recording.
     *
     * Creates a new MediaRecorder instance and starts recording to a temporary file.
     * The audio is encoded in MP3 format (or AAC if MP3 is not available).
     *
     * @return Result containing the output file path on success, or an error if recording fails to start
     */
    fun startRecording(): Result<String> {
        return try {
            // Create output file
            val outputFile = File.createTempFile("voice_note_", ".mp3", context.cacheDir)
            currentOutputFile = outputFile

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            val recorder = mediaRecorder
            if (recorder == null) {
                return Result.failure(Exception("Failed to initialize MediaRecorder"))
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(outputFile.absolutePath)

            try {
                recorder.prepare()
                recorder.start()
                isRecordingActive = true
                Log.d("AudioRecorder", "Recording started: ${outputFile.absolutePath}")
                Result.success(outputFile.absolutePath)
            } catch (e: IOException) {
                Log.e("AudioRecorder", "Failed to start recording: ${e.message}")
                recorder.release()
                mediaRecorder = null
                Result.failure(Exception("Failed to start recording: ${e.message}"))
            }

        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error starting recording: ${e.message}")
            Result.failure(Exception("Error starting recording: ${e.message}"))
        }
    }

    /**
     * Stops audio recording.
     *
     * Stops the MediaRecorder and releases its resources. Returns the recorded file.
     *
     * @return Result containing the recorded File on success, or an error if stopping fails
     */
    fun stopRecording(): Result<File> {
        return try {
            if (!isRecordingActive) {
                return Result.failure(Exception("No active recording to stop"))
            }

            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                    isRecordingActive = false
                    Log.d("AudioRecorder", "Recording stopped")
                } catch (e: Exception) {
                    Log.e("AudioRecorder", "Error stopping recording: ${e.message}")
                    release()
                    isRecordingActive = false
                    return Result.failure(Exception("Error stopping recording: ${e.message}"))
                }
            }
            mediaRecorder = null

            currentOutputFile?.let {
                if (it.exists() && it.length() > 0) {
                    Log.d("AudioRecorder", "Recording saved: ${it.absolutePath} (${it.length()} bytes)")
                    Result.success(it)
                } else {
                    Result.failure(Exception("Recording file is empty or does not exist"))
                }
            } ?: Result.failure(Exception("No output file found"))

        } catch (e: Exception) {
            Log.e("AudioRecorder", "Unexpected error stopping recording: ${e.message}")
            release()
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    /**
     * Releases all resources held by the MediaRecorder.
     *
     * Should be called when the recorder is no longer needed or when
     * an error occurs during recording.
     */
    fun release() {
        try {
            mediaRecorder?.apply {
                if (isRecordingActive) {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e("AudioRecorder", "Error stopping recorder during release: ${e.message}")
                    }
                }
                release()
            }
            mediaRecorder = null
            isRecordingActive = false
            Log.d("AudioRecorder", "MediaRecorder released")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error releasing MediaRecorder: ${e.message}")
        }
    }

    /**
     * Checks if recording is currently active.
     *
     * @return true if recording is in progress, false otherwise
     */
    fun isRecording(): Boolean = isRecordingActive

    /**
     * Deletes the current output file if it exists.
     *
     * Useful for cleaning up after canceling a recording or when an error occurs.
     */
    fun deleteCurrentRecording() {
        currentOutputFile?.let { file ->
            if (file.exists()) {
                val deleted = file.delete()
                Log.d("AudioRecorder", "Current recording deleted: $deleted")
            }
        }
        currentOutputFile = null
    }
}
