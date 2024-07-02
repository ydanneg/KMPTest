import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    val viewModel = AppViewModel(DatabaseFactory().createDatabase().getDao())
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMPTest",
            state = rememberWindowState(width = 412.dp, height = 915.dp)
        ) {
            App(viewModel)
        }
    }
}