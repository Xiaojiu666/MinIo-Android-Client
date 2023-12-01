package io.minio.android.workflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.minio.android.R
import io.minio.android.base.LoadingWrapper
import io.minio.android.base.UiStateWrapper
import io.minio.android.base.ui.theme.body1
import io.minio.android.base.ui.theme.body3
import io.minio.android.base.ui.theme.colorBackground
import io.minio.android.base.ui.theme.colorPrimary
import io.minio.android.base.ui.theme.colorSecondary
import io.minio.android.base.ui.theme.colorTertiary
import io.minio.messages.Bucket


@Composable
fun HomeRouter(viewModel: HomeViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomePage(uiState)
}


@Composable
fun HomePage(uiState: UiStateWrapper) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scaffoldState = rememberScaffoldState(drawerState)
    var showBucketPop by remember { mutableStateOf(false) }
    var bucket by remember {
        mutableStateOf(Bucket())
    }
    Scaffold(topBar = {
        HomeTopBar(
            bucket.name() ?: "",
            bucket.creationDate().toString(),
            onShowPop = {
                showBucketPop = !showBucketPop
            })
    }, drawerContent = {
        Text("Drawer Content")
    }, scaffoldState = scaffoldState, content = {
        uiState.LoadingWrapper<HomeViewModel.HomeUiState>(content = { uiState ->
            uiState.selectorBucket?.let {
                bucket = it
            }
            Box(modifier = Modifier.padding(it)) {
                if (showBucketPop) {
                    Popup(
                        onDismissRequest = { showBucketPop = !showBucketPop }
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorBackground()),
                        ) {
                            items(uiState.buckets) {
                                itemBucket(it)
                            }
                        }
                    }
                }

                Text(modifier = Modifier.padding(it), text = stringResource(id = R.string.app_name))
            }
        }) {

        }
    })
}

@Composable
private fun itemBucket(it: Bucket) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "${it.name()}",
            style = body1,
            color = colorTertiary()
        )

        Text(
            text = "${it.creationDate()}",
            style = body3,
            color = colorTertiary()
        )
    }
}


@Composable
fun HomeTopBar(title: String, subTitle: String, onShowPop: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorPrimary()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = {

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

        IconButton(modifier = Modifier.padding(horizontal = 8.dp), onClick = {

        }) {
            Icon(Icons.Default.Settings, tint = colorSecondary(), contentDescription = null)
        }
    }
}
