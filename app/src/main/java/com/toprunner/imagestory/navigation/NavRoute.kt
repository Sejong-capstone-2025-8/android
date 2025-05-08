package com.toprunner.imagestory.navigation

sealed class NavRoute(val route: String) {
    object Login : NavRoute("login")               // 추가한코드
    object Register : NavRoute("register")         // 추가한코드
    object Home : NavRoute("home")
    object FairyTaleList : NavRoute("fairytale_list")
    object VoiceList : NavRoute("voice_list")
    object MusicManager : NavRoute("music_manager")
    object MusicList : NavRoute("music_list/{storyId}") {
        fun routeWithArgs(storyId: Long): String = "music_list/$storyId"
    }

    object Settings : NavRoute("settings")
    object ManageAccount : NavRoute("manage_account") //  manage account
    object EditAccount : NavRoute("edit_account_screen")   // edit account
    object GeneratedStory : NavRoute("generated_story/{storyId}?bgmPath={bgmPath}") {
        fun createRoute(storyId: Long, bgmPath: String? = null): String {
            return if (bgmPath != null) {
                "generated_story/$storyId?bgmPath=$bgmPath"
            } else {
                "generated_story/$storyId"
            }
        }
    }
    object VoiceRecording : NavRoute("voice_recording")
}

//object GeneratedStory : NavRoute("generated_story/{storyId}") {
//    fun createRoute(storyId: Long) = "generated_story_screen/$storyId"
//}
//    object GeneratedStory : NavRoute("generated_story_screen/{storyId}") {
//        fun routeWithArgs(storyId: Long): String = "generated_story_screen/$storyId"
//    }
