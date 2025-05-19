package io.availe

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() = application {
    FileKit.init(appId = "io.availe")

    Window(
        onCloseRequest = ::exitApplication,
        title = "availe",
    ) {
        App()
    }
}