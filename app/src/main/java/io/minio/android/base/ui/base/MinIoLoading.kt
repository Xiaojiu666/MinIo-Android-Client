package io.minio.android.base.ui.base

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.minio.android.R
import kotlinx.coroutines.delay

@Composable
fun BoxScope.MinIoLoading() {
    val images = listOf(
        R.mipmap.ic_loading_fir,
        R.mipmap.ic_loading_sec,
        R.mipmap.ic_loading_thr
    )

    var index by remember { mutableStateOf(0) }

    LaunchedEffect(index) {
        delay(800)
        index = (index + 1) % images.size
    }


    Image(
        painter = painterResource(id = images[index]),
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .align(Alignment.Center),
        contentScale = ContentScale.Crop
    )
}
