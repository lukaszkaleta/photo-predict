package com.hackathon2025.deviationwizard

import androidx.annotation.OptIn
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hackathon2025.deviationwizard.api.Deviation
import com.hackathon2025.deviationwizard.api.DeviationAnalysis
import com.hackathon2025.deviationwizard.model.ApplicationViewModel
import kotlinx.serialization.Serializable

@Serializable
object MainScreen

@Serializable
object CameraScreen

@Serializable
object ListScreen

@Serializable
object ConfigScreen

@Serializable
data class DetailsScreen(val id: String)

@OptIn(UnstableApi::class)
@Composable
fun Application() {
    val applicationContext = LocalContext.current
    val controller = remember {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
        }
    }

    val viewModel = viewModel { ApplicationViewModel() }

    val bitmaps = viewModel.bitmaps.collectAsStateWithLifecycle()
    val records = viewModel.records.collectAsStateWithLifecycle()
    val comment = viewModel.comment.collectAsStateWithLifecycle()
    val deviations = viewModel.deviations.collectAsStateWithLifecycle()
    viewModel.selectedDeviation.collectAsStateWithLifecycle()
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle()
    val error = viewModel.error.collectAsStateWithLifecycle()

    // Fetch deviations when the app starts
    LaunchedEffect(Unit) {
        viewModel.fetchDeviations()
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainScreen) {
        composable<MainScreen> {
            MainView(
                onCameraOpen = { navController.navigate(CameraScreen) },
                onRecordTaken = viewModel::onRecordTaken,
                onGotoList = { navController.navigate(ListScreen) },
                onGotoConfig = { navController.navigate(ConfigScreen) },
                onImageSelect = { viewModel.onImageSelect(it) },
                onSubmit = {
                    viewModel.onSenData()
                    // Refresh the list of deviations after successful submission
                    viewModel.fetchDeviations()
                },
                onCancel = {
                    // Clear the form data
                    viewModel.onCommentChanged("")
                    viewModel.onRemoveBitmap(bitmaps.value.firstOrNull() ?: return@MainView)
                    viewModel.onRemoveRecord(records.value.firstOrNull() ?: return@MainView)
                },
                onRemoveBitmap = viewModel::onRemoveBitmap,
                onRemoveRecord = viewModel::onRemoveRecord,
                onCommentChanged = viewModel::onCommentChanged,
                bitmaps = bitmaps.value,
                records = records.value,
                comment = comment.value,
                isLoading = isLoading.value,
                error = error.value
            )
        }
        composable<CameraScreen> {
            CameraScreen(
                controller = controller,
                onGoBack = { navController.navigate(MainScreen) },
                onPhotoTaken = viewModel::onPhotoTaken
            )
        }
        composable<ListScreen> {
            ListScreen(
                deviations = deviations.value,
                error = error.value,
                onRefresh = { viewModel.fetchDeviations() },
                onGotoEntity = { id -> navController.navigate(DetailsScreen(id)) },
                onGoBack = { navController.navigate(MainScreen) },
                onDeleteDeviation = { id -> viewModel.deleteDeviation(id) }
            )
        }
        composable<ConfigScreen> {
            ConfigScreen(
                onGoBack = { navController.navigate(MainScreen) },
                onUpdateBaseUrl = viewModel::updateBaseUrl
            )
        }
        composable(route = DetailsScreen::class) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            val deviation = viewModel.selectedDeviation.collectAsStateWithLifecycle<Deviation?>().value
            val analysis = viewModel.selectedDeviationAnalysis.collectAsStateWithLifecycle<DeviationAnalysis?>().value
            val isLoading = viewModel.isLoading.collectAsStateWithLifecycle<Boolean>().value
            val error = viewModel.error.collectAsStateWithLifecycle<String?>().value

            LaunchedEffect(id) {
                viewModel.fetchDeviation(id)
            }

            DetailsScreen(
                deviation = deviation,
                analysis = analysis,
                isLoading = isLoading,
                error = error,
                onRefresh = { viewModel.fetchDeviation(id) },
                onGoBack = { navController.popBackStack() }
            )
        }
    }
}