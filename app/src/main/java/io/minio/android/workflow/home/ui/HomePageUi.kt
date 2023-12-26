package io.minio.android.workflow.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Visibility
import io.minio.android.R
import io.minio.android.base.ui.theme.body1
import io.minio.android.base.ui.theme.body2
import io.minio.android.base.ui.theme.body3
import io.minio.android.base.ui.theme.colorPrimary
import io.minio.android.base.ui.theme.colorSecondary
import io.minio.android.base.ui.theme.colorTertiary
import io.minio.android.entities.FileType
import io.minio.android.entities.FolderItemData
import io.minio.android.util.formatFileSize
import io.minio.android.workflow.home.TopBarModel
import io.minio.messages.Bucket


@Composable
fun ItemBucket(bucket: Bucket, onBucketItemClick: (Bucket) -> Unit) {
    Column(modifier = Modifier
        .padding(8.dp)
        .clickable {
            onBucketItemClick(bucket)
        }) {
        Text(
            text = bucket.name(), style = body1, color = colorTertiary()
        )

        Text(
            text = "${bucket.creationDate()}", style = body3, color = colorTertiary()
        )
    }
}

@Composable
fun HomeTopBar(
    title: String,
    subTitle: String,
    topBarModel: TopBarModel?,
    onShowPop: () -> Unit,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
                    onDeleteClick()
                }) {
                    Icon(Icons.Default.Delete, tint = colorSecondary(), contentDescription = null)
                }
            }
            else -> {}
        }

    }
}


@Composable
fun FolderItem(
    folderItem: FolderItemData,
    topBarModel: TopBarModel,
    onItemClick: (FolderItemData) -> Unit,
    onLongCLick: () -> Unit = {},
    onItemCheck: (FolderItemData) -> Unit = {}
) {
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .pointerInput(onItemClick, onLongCLick) {
            detectTapGestures(onLongPress = {
                onLongCLick()
            }, onTap = {
                onItemClick(folderItem)
            })
        }) {
        val (image, title, subSize, createData, line, check) = createRefs()

        Checkbox(modifier = Modifier.constrainAs(check) {
            start.linkTo(parent.start)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            visibility = if (topBarModel == TopBarModel.INCREASE) {
                Visibility.Invisible
            } else {
                Visibility.Visible
            }
        }, checked = folderItem.checked, onCheckedChange = {
            onItemCheck(folderItem)
        })
        val fileIcon = when (folderItem.fileType) {
            FileType.FOLDER -> {
                R.drawable.baseline_folder_24
            }
            FileType.IMAGE_FILE -> {
                R.drawable.baseline_image_24
            }
            FileType.TEXT_FILE -> {
                R.drawable.baseline_text_snippet_24
            }
            FileType.VIDEO_FILE -> {
                R.drawable.baseline_featured_video_24
            }
        }

        Image(modifier = Modifier
            .constrainAs(image) {
                start.linkTo(
                    if (topBarModel == TopBarModel.INCREASE) {
                        parent.start
                    } else {
                        check.end
                    }
                )
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
            .size(36.dp),
            contentScale = ContentScale.Crop,
            painter = painterResource(fileIcon),
            contentDescription = null)

        Text(modifier = Modifier.constrainAs(title) {
            start.linkTo(image.end, 8.dp)
            top.linkTo(parent.top, 4.dp)
        }, text = folderItem.fileName, style = body2)

        when (folderItem.fileType) {
            FileType.FOLDER -> {
                Text(modifier = Modifier.constrainAs(subSize) {
                    start.linkTo(image.end, 8.dp)
                    top.linkTo(title.bottom)
                    bottom.linkTo(parent.bottom, 4.dp)
                }, text = "${folderItem.subSize} 项", style = body3)

            }
            FileType.VIDEO_FILE,
            FileType.TEXT_FILE,
            FileType.IMAGE_FILE -> {
                Text(modifier = Modifier.constrainAs(subSize) {
                    start.linkTo(image.end, 8.dp)
                    top.linkTo(title.bottom)
                    bottom.linkTo(parent.bottom, 4.dp)
                }, text = folderItem.fileSize.formatFileSize(), style = body3)

                Text(modifier = Modifier.constrainAs(createData) {
                    bottom.linkTo(image.bottom)
                    end.linkTo(parent.end)
                }, text = folderItem.lastModifierTime, style = body3)
            }
        }
    }
}