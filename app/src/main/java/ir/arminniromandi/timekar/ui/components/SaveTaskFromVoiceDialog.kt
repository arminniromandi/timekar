package ir.arminniromandi.timekar.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.arminniromandi.timekar.data.remote.NetworkResult
import ir.arminniromandi.timekar.data.voice.VoiceRecognitionState
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.ui.strings.AppStrings

/**
 * Dialog for capturing task via voice input and processing with AI.
 */
@Composable
fun SaveTaskFromVoiceDialog(
    voiceState: VoiceRecognitionState,
    spokenText: String = "",
    aiApiReq: NetworkResult<TaskItem>?,
    strings: AppStrings,
    onDismissRequest: () -> Unit,
    onPauseListening: () -> Unit,
    onResumeListening: () -> Unit,
    onStopListening: () -> Unit,
    onSaveVoiceText: (String) -> Unit
) {
    var isListening by rememberSaveable { mutableStateOf(true) }

    // Sync state from VoiceRecognitionState
    LaunchedEffect(voiceState) {
        when (voiceState) {
            is VoiceRecognitionState.Idle -> {
                if (spokenText.isNotBlank()) {
                    onSaveVoiceText(spokenText)
                }
            }
            is VoiceRecognitionState.Listening -> {
                isListening = true
            }
            is VoiceRecognitionState.Paused -> {
                isListening = false
            }
            else -> {}
        }
    }

    // When AI request succeeds, automatically dismiss dialog
    LaunchedEffect(aiApiReq) {
        if (aiApiReq is NetworkResult.Success) {
            onDismissRequest()
        }
    }

    Dialog(
        onDismissRequest = {
            onStopListening()
            onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            when (aiApiReq) {
                is NetworkResult.Loading -> {
                    AiProcessingView(strings = strings)
                }

                is NetworkResult.Error -> {
                    DialogBody(
                        isListening = isListening,
                        spokenText = spokenText,
                        errorMessage = aiApiReq.message,
                        strings = strings,
                        onDismissRequest = {
                            onStopListening()
                            onDismissRequest()
                        },
                        onPauseListening = onPauseListening,
                        onResumeListening = onResumeListening,
                        onStopListening = onStopListening,
                        onSaveVoiceText = onSaveVoiceText
                    )
                }

                else -> {
                    DialogBody(
                        isListening = isListening,
                        spokenText = spokenText,
                        errorMessage = null,
                        strings = strings,
                        onDismissRequest = {
                            onStopListening()
                            onDismissRequest()
                        },
                        onPauseListening = onPauseListening,
                        onResumeListening = onResumeListening,
                        onStopListening = onStopListening,
                        onSaveVoiceText = onSaveVoiceText
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogBody(
    isListening: Boolean,
    spokenText: String,
    errorMessage: String?,
    strings: AppStrings,
    onDismissRequest: () -> Unit,
    onPauseListening: () -> Unit,
    onResumeListening: () -> Unit,
    onStopListening: () -> Unit,
    onSaveVoiceText: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AI Error Banner (if any)
        if (errorMessage != null) {
            ErrorBanner(errorMessage = errorMessage)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Guide / Example Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = strings.example,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.exampleMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Microphone Button with Pulse
        VoiceButton(
            isActive = isListening,
            onClick = {
                if (isListening) {
                    onPauseListening()
                } else {
                    onResumeListening()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recording Status / Spoken Text Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp, max = 120.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = spokenText.isNotBlank() to isListening,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "dialog_voice_status"
            ) { (hasText, listening) ->
                when {
                    hasText -> {
                        Text(
                            text = spokenText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    listening -> {
                        Text(
                            text = strings.listening,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    else -> {
                        Text(
                            text = strings.recordingPaused,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel / Close Button
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(strings.close)
            }

            // Pause / Resume Button
            val buttonColor by animateColorAsState(
                targetValue = if (isListening) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                label = "btn_color_anim"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isListening) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
                label = "btn_content_color_anim"
            )

            Button(
                onClick = {
                    if (isListening) {
                        onPauseListening()
                    } else {
                        onResumeListening()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = contentColor
                )
            ) {
                Text(if (isListening) strings.pause else strings.resume)
            }

            // Save Button
            Button(
                onClick = {
                    onStopListening()
                    if (spokenText.isNotBlank()) {
                        onSaveVoiceText(spokenText)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = spokenText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(strings.save)
            }
        }
    }
}

/**
 * View displayed when AI is analyzing/processing the voice input.
 */
@Composable
private fun AiProcessingView(
    strings: AppStrings
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (strings.isPersian) "در حال پردازش ..." else "Processing...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (strings.isPersian) "استخراج مشخصات کار از گفتار شما" else "Extracting task details from your speech",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Banner shown when AI processing fails.
 */
@Composable
private fun ErrorBanner(
    errorMessage: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VoiceButton(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    onClick: () -> Unit = {}
) {
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            val transition = rememberInfiniteTransition(label = "voice_pulse")
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse_progress"
            )

            VoicePulse(
                progress = progress,
                primary = primary
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(primary.copy(alpha = if (isActive) 0.16f else 0.08f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isActive) "در حال ضبط" else "میکروفون غیرفعال",
                tint = if (isActive) primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(size * 0.44f)
            )
        }
    }
}

@Composable
private fun VoicePulse(
    progress: Float,
    primary: Color
) {
    Canvas(modifier = Modifier.size(110.dp)) {
        val center = this.center

        // First pulse ring
        val radius1 = size.minDimension * (0.29f + (0.20f * progress))
        val alpha1 = 0.24f * (1f - progress)
        drawCircle(
            color = primary.copy(alpha = alpha1),
            radius = radius1,
            center = center
        )

        // Second pulse ring with phase delay
        val delayedProgress = (progress + 0.5f) % 1f
        val radius2 = size.minDimension * (0.29f + (0.20f * delayedProgress))
        val alpha2 = 0.24f * (1f - delayedProgress)
        drawCircle(
            color = primary.copy(alpha = alpha2),
            radius = radius2,
            center = center
        )
    }
}
