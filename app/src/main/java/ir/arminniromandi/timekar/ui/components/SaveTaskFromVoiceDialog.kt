package ir.arminniromandi.timekar.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Preview
@Composable
fun SaveTaskFromVoiceDialog(modifier: Modifier = Modifier) {

    var isListening by rememberSaveable {
        mutableStateOf(false)
    }

    VoiceButton(
        isActive = isListening,
        onClick = {
            isListening = !isListening
        }
    )



}

@Composable
fun VoiceButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .size(110.dp)
            .clickable(
                indication = null,
                interactionSource = remember {
                    MutableInteractionSource()
                },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        if (isActive) {

            val transition = rememberInfiniteTransition(
                label = "voice_pulse"
            )

            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1600,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulse_progress"
            )

            VoicePulse(
                progress = progress,
                primary = primary
            )
        }

        // دایره اصلی
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    primary.copy(alpha = 0.14f)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice input",
                tint = primary,
                modifier = Modifier.size(size * 0.42f)
            )
        }
    }
}


@Composable
private fun VoicePulse(
    progress: Float,
    primary: Color
) {
    Canvas(
        modifier = Modifier.size(110.dp)
    ) {

        val center = this.center

        /*
         * حلقه اول
         *
         * از نزدیک دکمه شروع می‌شود
         * و به آرامی به بیرون می‌رود.
         */
        val radius1 = size.minDimension * (
                0.29f + (0.20f * progress)
                )

        val alpha1 = 0.22f * (1f - progress)

        drawCircle(
            color = primary.copy(alpha = alpha1),
            radius = radius1,
            center = center
        )

        /*
         * حلقه دوم
         *
         * کمی با تأخیر نسبت به حلقه اول حرکت می‌کند.
         */
        val delayedProgress = (progress + 0.5f) % 1f

        val radius2 = size.minDimension * (
                0.29f + (0.20f * delayedProgress)
                )

        val alpha2 = 0.22f * (1f - delayedProgress)

        drawCircle(
            color = primary.copy(alpha = alpha2),
            radius = radius2,
            center = center
        )
    }
}

