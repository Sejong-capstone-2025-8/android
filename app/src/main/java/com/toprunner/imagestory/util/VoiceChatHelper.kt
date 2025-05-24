package com.toprunner.imagestory.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.*


class VoiceChatHelper(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onTTSComplete: (() -> Unit)? = null // TTS 완료 콜백 추가
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var _isListening by mutableStateOf(false)
    private var _isSpeaking by mutableStateOf(false)
    private var isTTSInitialized = false

    companion object {
        private const val TAG = "VoiceChatHelper"
    }

    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }

    // 음성 인식 초기화
    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(this)
    }

    // TTS 초기화
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context, this)
    }

    // TTS 초기화 완료 콜백
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.KOREAN
            isTTSInitialized = true
            Log.d(TAG, "TTS initialized successfully")
        } else {
            Log.e(TAG, "TTS initialization failed")
        }
    }

    // 음성 인식 시작
    fun startListening() {
        if (_isListening) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // 한국어 설정
            putExtra(RecognizerIntent.EXTRA_PROMPT, "동화에 대해 궁금한 것을 말씀해주세요")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening = true
            Log.d(TAG, "Speech recognition started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition: ${e.message}")
            onError("음성 인식을 시작할 수 없습니다")
        }
    }

    // 음성 인식 중지
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening = false
        Log.d(TAG, "Speech recognition stopped")
    }

    // 텍스트를 음성으로 변환 - TTS 완료 감지 개선
    fun speak(text: String) {
        if (!isTTSInitialized) {
            Log.e(TAG, "TTS not initialized")
            return
        }

        try {
            // 기존 음성 중지
            textToSpeech?.stop()

            // TTS 완료 리스너 설정
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking = true
                        Log.d(TAG, "TTS started: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking = false
                        Log.d(TAG, "TTS completed: $utteranceId")
                        onTTSComplete?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking = false
                        Log.e(TAG, "TTS error: $utteranceId")
                        onTTSComplete?.invoke()
                    }
                })
            } else {
                // 구버전 API 대응 - 익명 객체로 리스너 구현
                @Suppress("DEPRECATION")
                textToSpeech?.setOnUtteranceCompletedListener { utteranceId ->
                    _isSpeaking = false
                    Log.d(TAG, "TTS completed (legacy): $utteranceId")
                    onTTSComplete?.invoke()
                }
            }

            // 새 텍스트 읽기
            val utteranceId = "chatbot_response_${System.currentTimeMillis()}"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val params = Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            } else {
                @Suppress("DEPRECATION")
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
            }

            _isSpeaking = true
            Log.d(TAG, "Speaking: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Error in TTS: ${e.message}")
            _isSpeaking = false
            onTTSComplete?.invoke()
        }
    }

    // TTS 중지
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking = false
        Log.d(TAG, "TTS stopped")
    }

    // 상태 확인 메서드들
    fun isListening(): Boolean = _isListening
    fun isSpeaking(): Boolean = _isSpeaking

    // 리소스 해제
    fun cleanup() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
        speechRecognizer = null
        textToSpeech = null
        Log.d(TAG, "VoiceChatHelper cleaned up")
    }

    // RecognitionListener 구현
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // 음성 레벨 변화 (필요시 UI 업데이트용)
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // 음성 버퍼 수신
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech")
        _isListening = false
    }

    override fun onError(error: Int) {
        _isListening = false
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "오디오 오류가 발생했습니다"
            SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류가 발생했습니다"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "마이크 권한이 필요합니다"
            SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류가 발생했습니다"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과입니다"
            SpeechRecognizer.ERROR_NO_MATCH -> "음성을 인식할 수 없습니다"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식기가 사용 중입니다"
            SpeechRecognizer.ERROR_SERVER -> "서버 오류가 발생했습니다"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간이 초과되었습니다"
            else -> "알 수 없는 오류가 발생했습니다"
        }

        Log.e(TAG, "Speech recognition error: $errorMessage (code: $error)")
        onError(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val recognizedText = matches[0]
            Log.d(TAG, "Speech recognized: $recognizedText")
            onSpeechResult(recognizedText)
        }
        _isListening = false
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // 부분 결과 (실시간 인식 결과)
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            Log.d(TAG, "Partial result: ${matches[0]}")
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // 기타 이벤트
    }
}