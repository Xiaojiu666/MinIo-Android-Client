package io.minio.android.workflow.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.minio.android.ui.video.FullScreenVideo

@Composable
fun VideoPlayPage(videoPreViewModel: VideoPreViewModel) {
    val uiState by videoPreViewModel.uiState.collectAsStateWithLifecycle()
    FullScreenVideo(uiState.videoUrl)
}