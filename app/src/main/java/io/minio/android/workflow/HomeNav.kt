package io.minio.android.workflow

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import io.minio.android.workflow.home.HomeRouter
import io.minio.android.workflow.image.ImagePrePage
import io.minio.android.workflow.image.ImagePreRouter
import io.minio.android.workflow.image.imagePreViewModel
import io.minio.android.workflow.text.TextPreRouter
import io.minio.android.workflow.text.textPreViewModel

const val HOME_PAGE = "home_page"

const val IMAGE_PRE_PAGE = "image_pre_page"

const val TEXT_PRE_PAGE = "text_pre_page"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeNav() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = HOME_PAGE,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it })
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            ExitTransition.None
        }
    ) {
        composable(
            HOME_PAGE
        ) {
            HomeRouter(hiltViewModel(), navController)
        }

        composable(
            "$IMAGE_PRE_PAGE$?images={images}&selectorIndex={selectorIndex}"
        ) {
            val imageString = it.arguments?.getString("images") ?: ""
            val selectorIndex = it.arguments?.getString("selectorIndex") ?: ""
            val imagePreViewModel = imagePreViewModel(imageString, selectorIndex, it)
            ImagePreRouter(imagePreViewModel, navController)
        }

        composable(
            "$TEXT_PRE_PAGE$?fileUrl={fileUrl}"
        ) {
            val fileUrl = it.arguments?.getString("fileUrl") ?: ""
            TextPreRouter(
                textPreViewModel(
                    fileUrl = fileUrl,
                    viewModelStoreOwner = it
                )
            )
        }
    }
}