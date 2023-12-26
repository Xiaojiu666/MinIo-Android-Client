package io.minio.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.minio.android.R


@Composable
fun FileEmptyPage(
    modifier: Modifier = Modifier,
    resId: Int = R.mipmap.empty_no_folder,
    text: String = "您的数据为空"
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = resId), contentDescription = "")
        Text(text = text)
    }
}


@Preview
@Composable
fun preFileEmpty() {
    FileEmptyPage()
}