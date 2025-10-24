package me.developes.humming.sdui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.ErrorData
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.container.SDContainer

interface ThemeProvider {
    @Composable
    fun Content(content: @Composable () -> Unit)
}

class HummingSDUI {
    data class Config(
        val type: String,
        val properties: MutableMap<String, String?>,
        val placeholder: @Composable () -> Unit = {},
        val loadingHandler: @Composable () -> Unit = {},
        val errorHandler: @Composable (error: ErrorData, viewModel: SDUIFormViewModel) -> Unit = { _, _ -> },
        val theme: ThemeProvider = NoTheme,
        val block: SDUIFormViewModel.() -> Unit
    ) {
        private object NoTheme : ThemeProvider {
            @Composable
            override fun Content(content: @Composable () -> Unit) {
                content()
            }
        }
    }

    @Composable
    fun Content(config: Config) = config.theme.Content {
        val viewModel = viewModel {
            SDUIFormViewModel().apply {
                addLibrary(SDContainer())
                config.block(this)
            }
        }

        val isLoading by viewModel.isLoading.collectAsState()
        val errorDialogData by viewModel.errorState.collectAsState()
        val contentState = remember(config.type, config.properties) {
            mutableStateOf<ComposableContent?>(null)
        }
        LaunchedEffect(config.type, config.properties) {
            contentState.value = viewModel.loadComponent(config.type, config.properties)
        }

        when {
            isLoading -> config.loadingHandler()
            errorDialogData != null -> config.errorHandler(errorDialogData!!, viewModel)
        }
        contentState.value?.Content() ?: config.placeholder()
    }
}