package com.grupo03.solea.ui.screens.voicenote

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grupo03.solea.R
import com.grupo03.solea.presentation.viewmodels.screens.AudioAnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioAnalysisScreen(
    audioAnalysisViewModel: AudioAnalysisViewModel,
    authViewModel: com.grupo03.solea.presentation.viewmodels.shared.AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val state = audioAnalysisViewModel.state.collectAsState()
    val authState = authViewModel.authState.collectAsState()
    val userId = authState.value.user?.uid ?: ""
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        audioAnalysisViewModel.initializeRecorder(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        audioAnalysisViewModel.setPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(state.value.analyzedVoiceNote) {
        if (state.value.analyzedVoiceNote != null && !state.value.isAnalyzing) {
            onNavigateToEdit()
        }
    }

    LaunchedEffect(state.value.error) {
        state.value.error?.let { error ->
            snackbarHostState.showSnackbar(context.getString(R.string.error_analyzing_audio))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.voice_note_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.value.isRecording) {
                                audioAnalysisViewModel.cancelRecording()
                            }
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !state.value.hasPermission -> {
                    PermissionRequiredContent()
                }
                state.value.isAnalyzing -> {
                    AnalyzingContent()
                }
                else -> {
                    RecordingContent(
                        isRecording = state.value.isRecording,
                        duration = state.value.recordingDuration,
                        onStartRecording = { audioAnalysisViewModel.startRecording() },
                        onStopRecording = { audioAnalysisViewModel.stopRecordingAndAnalyze(userId) },
                        onCancelRecording = { audioAnalysisViewModel.cancelRecording() }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequiredContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.audio_permission_required),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnalyzingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.analyzing_audio),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.analyzing_audio_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecordingContent(
    isRecording: Boolean,
    duration: Long,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isRecording) {
            Text(
                stringResource(R.string.voice_note_instructions),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.voice_note_example),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                stringResource(R.string.recording_in_progress),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (isRecording) {
            Text(
                formatDuration(duration),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        RecordButton(
            isRecording = isRecording,
            onStartRecording = onStartRecording,
            onStopRecording = onStopRecording
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isRecording) {
            TextButton(onClick = onCancelRecording) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
fun RecordButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
        }

        FloatingActionButton(
            onClick = {
                if (isRecording) {
                    onStopRecording()
                } else {
                    onStartRecording()
                }
            },
            modifier = Modifier.size(80.dp),
            containerColor = if (isRecording) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) {
                    stringResource(R.string.stop_recording)
                } else {
                    stringResource(R.string.start_recording)
                },
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
