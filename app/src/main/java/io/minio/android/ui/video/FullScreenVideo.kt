package io.minio.android.ui.video

import android.media.session.MediaController
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.minio.android.R
import io.minio.android.base.ui.theme.colorPrimary
import retrofit2.http.Url

@Composable
fun FullScreenVideo(
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        AndroidView(
            modifier = modifier
                .fillMaxSize()
                .background(colorPrimary()),
            factory = { _ ->
                VideoView(context)
            },
            update = { videoView ->
                videoView.apply {
                    setVideoURI(Uri.parse(videoUrl))
                    start()
                }
            }
        )

    }
}
