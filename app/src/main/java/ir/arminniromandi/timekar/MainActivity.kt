package ir.arminniromandi.timekar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.util.TableInfo
import ir.arminniromandi.timekar.ui.MainScreen
import ir.arminniromandi.timekar.ui.theme.ChronosTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appContainer = (application as ChronosApplication).container
    setContent {
      MainScreen(container = appContainer)
    }
  }
}

