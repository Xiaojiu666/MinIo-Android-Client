package io.minio.android.workflow.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.minio.android.base.ui.theme.body1
import io.minio.android.base.ui.theme.body3
import io.minio.android.base.ui.theme.colorPrimary
import io.minio.android.base.ui.theme.colorSecondary
import io.minio.android.workflow.home.TopBarModel


@Composable
fun HomeTopBar(
    title: String,
    subTitle: String,
    topBarModel: TopBarModel?,
    onShowPop: () -> Unit,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorPrimary()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = {
            onMenuClick()
        }) {
            Icon(Icons.Default.Menu, tint = colorSecondary(), contentDescription = null)
        }
        Row(
            modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
        ) {
            Column() {
                Text(text = title, style = body1, maxLines = 1, color = colorSecondary())

                Text(text = subTitle, style = body3, maxLines = 1, color = colorSecondary())
            }
            IconButton(modifier = Modifier.padding(8.dp), onClick = {
                onShowPop()
            }) {
                Icon(
                    Icons.Default.ArrowDropDown, tint = colorSecondary(), contentDescription = null
                )
            }
        }
        when (topBarModel) {
            TopBarModel.INCREASE -> {
                IconButton(modifier = Modifier.padding(horizontal = 8.dp), onClick = {
                    onAddClick()
                }) {
                    Icon(Icons.Default.Add, tint = colorSecondary(), contentDescription = null)
                }
            }
            TopBarModel.DELETE -> {
                IconButton(modifier = Modifier.padding(horizontal = 8.dp), onClick = {
                    onAddClick()
                }) {
                    Icon(Icons.Default.Delete, tint = colorSecondary(), contentDescription = null)
                }
            }
            else -> {}
        }

    }
}
