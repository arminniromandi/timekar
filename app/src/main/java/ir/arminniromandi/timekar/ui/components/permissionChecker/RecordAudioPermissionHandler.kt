package ir.arminniromandi.timekar.ui.components.permissionChecker

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class RecordAudioPermission(
    private val checkAndRequest: () -> Unit
) {
    fun checkAndRequestAudioPermission() {
        checkAndRequest()
    }
}

@Composable
fun rememberRecordAudioPermission(
    onGranted: () -> Unit
): RecordAudioPermission {

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            onGranted()
        } else {
            Toast.makeText(
                context,
                "برای ثبت صوتی وظایف، دسترسی به میکروفون الزامی است",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    return remember(context, launcher, onGranted) {

        RecordAudioPermission {

            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                onGranted()
            } else {
                launcher.launch(
                    Manifest.permission.RECORD_AUDIO
                )
            }
        }
    }
}