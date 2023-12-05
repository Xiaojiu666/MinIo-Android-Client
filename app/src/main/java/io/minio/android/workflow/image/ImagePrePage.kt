package io.minio.android.workflow.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.minio.android.base.ui.BaseScaffoldPage
import io.minio.android.base.ui.BaseTitleToolbar
import io.minio.android.base.ui.theme.colorBackground
import io.minio.android.base.ui.theme.colorPrimary

@Composable
fun ImagePrePage(
    imagePreViewModel: ImagePreViewModel,
    navController: NavController ,
) {
    val uiState by imagePreViewModel.uiState.collectAsStateWithLifecycle()
    BaseScaffoldPage(toolbar = {
        BaseTitleToolbar("预览页面")
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
            LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                items(uiState.comicList) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(colorPrimary())
                            .padding(4.dp)
                            .clickable {
                            },
                        text = "$it", color = Color.White, textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
