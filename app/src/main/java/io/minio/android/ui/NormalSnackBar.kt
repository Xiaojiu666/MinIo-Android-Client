package io.minio.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NormalSnackBar(
    text: String = "提示内容",
    snackBarType: SnackBarType = SnackBarType.NORMAL,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(color = Color.LightGray, shape = RoundedCornerShape(size = 6.dp))
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
        ) {
            when (snackBarType) {
                SnackBarType.FAIL -> {
                    Icon(Icons.Default.Warning, contentDescription = null)
                }
                SnackBarType.NORMAL -> {

                }
                SnackBarType.SUCCESSFUL -> {
                    Icon(Icons.Default.Done, contentDescription = null)
                }
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(400),
                    color = Color.White,
                ),
            )
        }

    }
}


suspend fun SnackbarHostState.showImmediately(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    currentSnackbarData?.dismiss()
    showSnackbar(message, actionLabel, duration)
}


@Composable
@Preview
fun PreviewSnackBar() {
    NormalSnackBar()
}

enum class SnackBarType {
    SUCCESSFUL, FAIL, NORMAL
}