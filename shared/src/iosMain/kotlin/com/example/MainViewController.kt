package com.example

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSProcessInfo

/** Entry point used by the Swift iOS app. */
@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController {
    // "-netsetScreen <name>" launch argument opens a screen directly (used for simulator screenshots)
    val args = NSProcessInfo.processInfo.arguments.map { it.toString() }
    val screen = args.indexOf("-netsetScreen").takeIf { it >= 0 }?.let { args.getOrNull(it + 1) }
    App(startScreen = screen)
}
