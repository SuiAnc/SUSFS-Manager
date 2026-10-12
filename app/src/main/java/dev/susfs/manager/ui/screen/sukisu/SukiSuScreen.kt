package dev.susfs.manager.ui.screen.sukisu

import androidx.compose.runtime.Composable

/**
 * Entry point for the SukiSU-Ultra SUSFS page.
 *
 * The upstream app picks between its Material and Miuix variants at runtime;
 * only the Material variant was ported (the Miuix one needs a third-party
 * Compose design library this app does not depend on), so this is a plain
 * forwarder that keeps the call site symmetric with `SusfsScreen()`.
 */
@Composable
fun SukiSuScreen() {
    SuSFSMaterial()
}
