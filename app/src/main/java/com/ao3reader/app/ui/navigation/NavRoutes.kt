package com.ao3reader.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browser : Screen("browser?url={url}") {
        fun createRoute(url: String = "https://archiveofourown.org"): String {
            return "browser?url=${java.net.URLEncoder.encode(url, "UTF-8")}"
        }
    }
    object WorkDetail : Screen("work_detail/{workId}") {
        fun createRoute(workId: String): String = "work_detail/$workId"
    }
    object Reader : Screen("reader/{workId}?chapter={chapter}") {
        fun createRoute(workId: String, chapterIndex: Int = 1): String {
            return "reader/$workId?chapter=$chapterIndex"
        }
    }
    object Settings : Screen("settings")
}
