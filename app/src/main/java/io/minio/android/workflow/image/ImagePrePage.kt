package io.minio.android.workflow.image

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import io.minio.android.R
import io.minio.android.base.ui.BaseBackToolbar
import io.minio.android.base.ui.BaseScaffoldPage
import io.minio.android.base.ui.BaseTitleToolbar
import io.minio.android.base.ui.theme.colorBackground
import io.minio.android.base.ui.theme.colorPrimary

@Composable
fun ImagePreRouter(
    imagePreViewModel: ImagePreViewModel,
    navController: NavController
) {
    val uiState by imagePreViewModel.uiState.collectAsStateWithLifecycle()

    ImagePrePage(uiState = uiState, onPageBack = {
        navController.popBackStack()
    })
}

@Composable
fun ImagePrePage(
    uiState: ImagePreViewModel.ImagePreUiState,
    onPageBack: () -> Unit,
) {
    BaseScaffoldPage(toolbar = {
        BaseBackToolbar("Image PreView", R.drawable.baseline_arrow_back_24) {
            onPageBack()
        }
    }) {

        var showButtons by remember { mutableStateOf(true) }

        val lazyListState = rememberLazyListState()

        LaunchedEffect(lazyListState.isScrollInProgress) {
            showButtons = !lazyListState.isScrollInProgress
        }
        Box(
            modifier = Modifier
                .background(colorBackground())
                .padding(it)
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(Color.White)
                    .clickable { showButtons = true },
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyListState
            ) {
                items(uiState.comicList) { imageName ->
                    AsyncImage(
                        modifier = Modifier.fillMaxWidth(),
                        model = ImageRequest.Builder(LocalContext.current)
                            .placeholder(R.mipmap.image_placeholder)
                            .data(imageName).listener(
                                onError = { request, result ->
                                    println("onError request ${request.error} result ${result.throwable}")
                                }
                            )
                            .build(),
                        contentDescription = "",
                        imageLoader = LocalContext.current.imageLoader,
                        contentScale = ContentScale.Crop
                    )
                }
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = showButtons,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {

                    }) {
                        Text("上一页")
                    }

                    Button(onClick = {

                    }) {
                        Text("下一页")
                    }
                }
            }
        }
    }
}
