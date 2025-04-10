package com.toprunner.imagestory.navigation

sealed class NavRoute(val route: String) {
    object Home : NavRoute("home")
    object FairyTaleList : NavRoute("fairytale_list")
    object VoiceList : NavRoute("voice_list")
    object MusicList : NavRoute("music_list")
    object Settings : NavRoute("settings")
    object GeneratedStory : NavRoute("generated_story/{storyId}") {
        fun createRoute(storyId: Long) = "generated_story/$storyId"
    }
    object VoiceRecording : NavRoute("voice_recording")
}