package ir.arminniromandi.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,

    timePickerState: TimePickerState
) {


    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val selectedMinute =
                    timePickerState.hour * 60 +
                            timePickerState.minute
                onConfirm(selectedMinute)
            }) {
                Text("OK")
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
            )

        }
    )


}