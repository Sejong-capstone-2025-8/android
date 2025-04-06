package com.toprunner.imagestory.model

data class VoiceFeatures(
    val averagePitch: Double,
    val pitchStdDev: Double,
    val mfccValues: List<DoubleArray>
) {
    // DoubleArray 특성을 고려한 equals 및 hashCode 오버라이드
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VoiceFeatures

        if (averagePitch != other.averagePitch) return false
        if (pitchStdDev != other.pitchStdDev) return false
        if (mfccValues.size != other.mfccValues.size) return false

        for (i in mfccValues.indices) {
            if (!mfccValues[i].contentEquals(other.mfccValues[i])) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = averagePitch.hashCode()
        result = 31 * result + pitchStdDev.hashCode()
        result = 31 * result + mfccValues.hashCode()
        return result
    }
}