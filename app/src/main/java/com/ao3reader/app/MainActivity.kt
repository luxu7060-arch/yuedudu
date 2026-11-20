package com.ao3reader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ao3reader.app.data.parser.AO3HtmlParser
import com.ao3reader.app.ui.navigation.Screen
import com.ao3reader.app.ui.screens.browser.AO3BrowserScreen
import com.ao3reader.app.ui.screens.home.HomeScreen
import com.ao3reader.app.ui.screens.reader.ReaderScreen
import com.ao3reader.app.ui.screens.settings.ThemeSettingsScreen
import com.ao3reader.app.ui.screens.workdetail.WorkDetailScreen
import com.ao3reader.app.ui.theme.AO3ReaderTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val prefRepo = AO3Application.instance.preferenceRepository
        val workRepo = AO3Application.instance.workRepository

        // Check if opened via intent (e.g. https://archiveofourown.org/works/12345678)
        val intentData = intent?.data?.toString()
        val initialWorkIdFromIntent = intentData?.let { AO3HtmlParser.extractWorkId(it) }

        if (!initialWorkIdFromIntent.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                workRepo.fetchWork(initialWorkIdFromIntent)
            }
        }

        setContent {
            val themeSettings by prefRepo.themeSettings.collectAsState()

            AO3ReaderTheme(themeSettings = themeSettings) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    val startDestination = if (!initialWorkIdFromIntent.isNullOrBlank()) {
                        Screen.WorkDetail.createRoute(initialWorkIdFromIntent)
                    } else {
                        Screen.Home.route
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(navController = navController)
                        }

                        composable(
                            route = Screen.Browser.route,
                            arguments = listOf(
                                navArgument("url") {
                                    type = NavType.StringType
                                    defaultValue = "https://archiveofourown.org"
                                }
                            )
                        ) { backStackEntry ->
                            val rawUrl = backStackEntry.arguments?.getString("url") ?: "https://archiveofourown.org"
                            val decodedUrl = java.net.URLDecoder.decode(rawUrl, "UTF-8")
                            AO3BrowserScreen(navController = navController, initialUrl = decodedUrl)
                        }

                        composable(
                            route = Screen.WorkDetail.route,
                            arguments = listOf(
                                navArgument("workId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val workId = backStackEntry.arguments?.getString("workId") ?: ""
                            WorkDetailScreen(navController = navController, workId = workId)
                        }

                        composable(
                            route = Screen.Reader.route,
                            arguments = listOf(
                                navArgument("workId") { type = NavType.StringType },
                                navArgument("chapter") {
                                    type = NavType.IntType
                                    defaultValue = 1
                                }
                            )
                        ) { backStackEntry ->
                            val workId = backStackEntry.arguments?.getString("workId") ?: ""
                            val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1
                            ReaderScreen(
                                navController = navController,
                                workId = workId,
                                initialChapterIndex = chapter
                            )
                        }

                        composable(Screen.Settings.route) {
                            ThemeSettingsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
