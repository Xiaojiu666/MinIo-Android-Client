package io.minio.android.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.minio.android.base.ui.base.MinIoLoading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface UiStateWrapper {
    object Loading : UiStateWrapper
    data class Error(val throwable: Throwable, val message: String?) : UiStateWrapper
}

inline fun <reified T : UiStateWrapper> MutableStateFlow<UiStateWrapper>.updateUiState(
    block: (T) -> T
) {
    this.update { uiStateWrapper ->
        if (uiStateWrapper is T) {
            block(uiStateWrapper)
        } else {
            uiStateWrapper
        }
    }
}

@Composable
inline fun <reified T : UiStateWrapper> UiStateWrapper.LoadingWrapper(
    content: @Composable (T) -> Unit,
    noinline error: @Composable (() -> Unit)? = null,
    noinline onRetryClick: () -> Unit
) {
    when (this) {
        is UiStateWrapper.Loading -> {
            DefaultLoading()
        }

        is UiStateWrapper.Error -> {
            error?.invoke() ?: DefaultError(onRetryClick)
        }

        is T -> {
            content(this)
        }
    }
}

@Composable
fun DefaultError(onRetryClick: () -> Unit) {
    Text(text = "DefaultError")
}

@Composable
fun DefaultLoading() {
    Box(modifier = Modifier.fillMaxSize()) {
        MinIoLoading()
    }
}
