package com.toprunner.imagestory.model


data class Story(
    val title: String,
    val theme: String,
    val text: String,
    val averagePitch: Double,
    val pitchStdDev: Double,
    val mfccValues: List<List<Double>>
)
