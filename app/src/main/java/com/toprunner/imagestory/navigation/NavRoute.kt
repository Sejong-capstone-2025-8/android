package com.toprunner.imagestory.navigation

sealed class NavRoute(val route: String) {
    object Login : NavRoute("login")               // 추가한코드
    object Register : NavRoute("register")         // 추가한코드
    object Home : NavRoute("home")
    object FairyTaleList : NavRoute("fairytale_list")
    object VoiceList : NavRoute("voice_list")
    object MusicList : NavRoute("music_list")
    object Settings : NavRoute("settings")
    object ManageAccount : NavRoute("manage_account") //  manage account
    object EditAccount : NavRoute("edit_account_screen")   // edit account
    object GeneratedStory : NavRoute("generated_story/{storyId}") {
        fun createRoute(storyId: Long) = "generated_story/$storyId"
    }
    object VoiceRecording : NavRoute("voice_recording")
}