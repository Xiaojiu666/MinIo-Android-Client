package io.minio.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.minio.android.R
import io.minio.android.ui.theme.colorPrimary
import io.minio.android.ui.theme.colorSecondary
import io.minio.android.ui.theme.subtitle2Bold


val TOOLBAR_HEIGHT = 56.dp

@Composable
fun BaseToolbar(
    leftView: @Composable BoxScope.() -> Unit = {},
    centerView: @Composable BoxScope.() -> Unit = {},
    rightView: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        Modifier
            .background(colorPrimary())
            .statusBarsPadding()
            .padding(11.dp, 0.dp, 16.dp, 0.dp)
            .fillMaxWidth()
            .height(TOOLBAR_HEIGHT)
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
        ) {
            centerView(this)
        }

        Box(
            Modifier.align(Alignment.CenterStart)
        ) {
            leftView(this)
        }

        Box(
            Modifier.align(Alignment.CenterEnd)
        ) {
            rightView(this)
        }
    }
}

@Composable
fun BaseTitleToolbar(
    title: String,
) {
    BaseToolbar(
        centerView = {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = title,
                textAlign = TextAlign.Center,
                style = subtitle2Bold,
                color = colorSecondary()
            )
        })
}

@Composable
fun BaseBackToolbar(
    title: String,
    imageResId: Int,
    onBackClick: () -> Unit
) {
    BaseToolbar(
        centerView = {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = title,
                textAlign = TextAlign.Center,
                style = subtitle2Bold,
                color = colorSecondary()
            )
        }, leftView = {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable {
                        onBackClick()
                    },
                painter = painterResource(id = imageResId),
                contentDescription = ""
            )
        })
}

@Preview
@Composable
fun preBaseTitleToolbar() {
    BaseTitleToolbar(title = "home")
}


