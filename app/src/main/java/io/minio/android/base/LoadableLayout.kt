package io.minio.android.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed class LoadableState<T> private constructor(open val data: T?) {

    class Loading<T> : LoadableState<T>(null)

    class Error<T> : LoadableState<T>(null)

    class Success<T>(override val data: T) : LoadableState<T>(data)
    class Empty<T> : LoadableState<T>(null)
}

fun <T> MutableStateFlow<LoadableState<T>>.updateUiState(updater: (T) -> T) {
    this.update { loadableState ->
        if (loadableState is LoadableState.Success) {
            LoadableState.Success(updater(loadableState.data))
        } else {
            loadableState
        }
    }
}

@Composable
inline fun <reified ContentType> LoadableLayout(
    modifier: Modifier = Modifier,
    loadableState: LoadableState<ContentType>,
    noinline onRetryClick: () -> Unit,
    noinline loading: (@Composable BoxScope.() -> Unit)? = null,
    noinline error: (@Composable BoxScope.() -> Unit)? = null,
    noinline emptyLayout: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.(ContentType) -> Unit,
) {
    Box(modifier) {
        when (loadableState) {
            is LoadableState.Loading -> {
                loading?.invoke(this) ?: DefaultLoading()
            }

            is LoadableState.Error -> {
                error?.invoke(this) ?: DefaultError(onRetryClick)
            }

            is LoadableState.Success -> {
                content(loadableState.data)
            }
            is LoadableState.Empty -> {
                emptyLayout?.invoke(this) ?: DefaultEmpty(onRetryClick)
            }
        }
    }
}

@Composable
inline fun <reified ContentType> LoadableLayout(
    modifier: Modifier = Modifier,
    loadableState: UiStateWrapper,
    noinline  onRetryClick: () -> Unit,
    noinline loading: (@Composable BoxScope.() -> Unit)? = null,
    noinline error: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.(ContentType) -> Unit,
) {
    Box(modifier) {
        when (loadableState) {
            is UiStateWrapper.Loading -> {
                loading?.invoke(this) ?: DefaultLoading()
            }

            is UiStateWrapper.Error -> {
                error?.invoke(this) ?: DefaultError(onRetryClick)
            }

            is ContentType -> {
                content(loadableState)
            }
        }
    }
}

inline fun <reified T> MutableStateFlow<LoadableState<T>>.valueOrNull(): T? {
    return if (value is LoadableState.Success) value.data else null
}

@Composable
inline fun BoxScope.DefaultEmpty(crossinline onRetryClick: () -> Unit) {
    Box(modifier = Modifier.align(Alignment.Center).clickable {onRetryClick()  }){
        Text(text = "", color = Color.Black)
    }
}