package com.toprunner.imagestory.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.State
import java.io.File


class VoiceRecorderViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    val context = application.applicationContext

    var mediaRecorder: MediaRecorder? = null
    var mediaPlayer: MediaPlayer? = null

    val _isRecording = mutableStateOf(false)
    val isRecording: State<Boolean> = _isRecording

    val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    val _recordedFile = mutableStateOf<File?>(null)
    val recordedFile: State<File?> = _recordedFile

    var audioFilePath: String? = null

    // ViewModel 안에서 수정
    fun toggleRecording() {
        if (_isRecording.value) stopRecording() else startRecording()
    }


    fun startRecording() {
        val outputFile = File(context.cacheDir, "recorded_audio.3gp")
        _recordedFile.value = outputFile

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }

        _isRecording.value = true
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        _isRecording.value = false
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        val file = _recordedFile.value ?: return

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                stopPlayback()
            }
        }

        _isPlaying.value = true
    }

    fun stopPlayback() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun getRecordedFile(): File? {
        return audioFilePath?.let { File(it) }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
        stopPlayback()
    }
}