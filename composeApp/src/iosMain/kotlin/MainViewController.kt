import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val viewModel = remember {
        AppViewModel(DatabaseFactory().createDatabase().getDao())
    }
    App(viewModel)
}