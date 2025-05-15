package com.toprunner.imagestory.util

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class NetworkUtil {
    private val TAG = "NetworkUtil"

    // 타임아웃 설정을 위한 상수
    companion object {
        private const val DEFAULT_TIMEOUT = 60_000   // 1분 (일반 요청용)
        private const val LONG_TIMEOUT = 300_000     // 5분 (대용량 데이터 요청용)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * HTTP 요청을 보내고 응답 텍스트와 상태 코드를 반환하는 함수
     * @param url 요청 URL
     * @param method HTTP 메서드 (GET, POST 등)
     * @param headers HTTP 헤더 맵
     * @param body 요청 본문 (POST 요청 등에 사용)
     * @param longTimeout 긴 타임아웃 사용 여부 (대용량 요청시 true)
     * @return Pair<String, Int> - 응답 텍스트와 HTTP 상태 코드
     */
    suspend fun sendHttpRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String? = null,
        longTimeout: Boolean = false
    ): Pair<String, Int> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = if (longTimeout) LONG_TIMEOUT else DEFAULT_TIMEOUT
                readTimeout = if (longTimeout) LONG_TIMEOUT else DEFAULT_TIMEOUT

                // 헤더 설정
                headers.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }

                // POST 요청인 경우 body 추가
                if ((method == "POST" || method == "PUT") && body != null) {
                    doOutput = true
                    outputStream.use { os ->
                        os.write(body.toByteArray())
                        os.flush()
                    }
                }
            }

            val statusCode = connection.responseCode
            val responseText = if (statusCode in 200..299) {
                // 성공 응답 읽기
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                // 오류 응답 읽기
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.w(TAG, "HTTP Error: $statusCode - $errorResponse")
                errorResponse
            }

            return@withContext Pair(responseText, statusCode)
        } catch (e: Exception) {
            Log.e(TAG, "Error in HTTP request: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 멀티파트 요청을 보내는 함수 (파일 업로드 등에 사용)
     * @param url API 엔드포인트 URL
     * @param headers HTTP 헤더 맵
     * @param body 바이트 배열로 구성된 멀티파트 요청 본문
     * @return Pair<String, Int> - API 응답 문자열과 HTTP 상태 코드
     */
    suspend fun sendMultipartRequest(
        url: String,
        headers: Map<String, String>,
        body: ByteArray
    ): Pair<String, Int> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = LONG_TIMEOUT // 멀티파트는 더 긴 타임아웃
                readTimeout = LONG_TIMEOUT

                // 헤더 설정
                headers.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }

                // 요청 바디 전송
                doOutput = true
                outputStream.use { os ->
                    os.write(body)
                    os.flush()
                }
            }

            val statusCode = connection.responseCode
            Log.d(TAG, "Multipart request response code: $statusCode")

            val responseText = if (statusCode in 200..299) {
                // 성공 응답 읽기
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                // 오류 응답 읽기
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e(TAG, "HTTP Error: $statusCode - $errorResponse")
                errorResponse
            }

            return@withContext Pair(responseText, statusCode)
        } catch (e: Exception) {
            Log.e(TAG, "Error in multipart request: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }



    /**
     * 오디오 데이터를 다운로드하는 함수 (바이너리 데이터 처리용)
     * @param url 요청 URL
     * @param headers HTTP 헤더 맵
     * @param requestBody 요청 본문
     * @param longTimeout 긴 타임아웃 사용 여부 (기본값: false)
     * @return Pair<ByteArray, Int> - 오디오 바이트 데이터와 HTTP 상태 코드
     */
    suspend fun downloadAudio(
        url: String,
        headers: Map<String, String>,
        requestBody: String,
        longTimeout: Boolean = false
    ): Pair<ByteArray, Int> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = if (longTimeout) LONG_TIMEOUT else DEFAULT_TIMEOUT
                readTimeout = if (longTimeout) LONG_TIMEOUT else DEFAULT_TIMEOUT

                // 헤더 설정
                headers.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }

                // POST body 추가
                doOutput = true
                outputStream.use { os ->
                    os.write(requestBody.toByteArray())
                    os.flush()
                }
            }

            val statusCode = connection.responseCode
            val responseBytes = if (statusCode in 200..299) {
                // 성공 응답 읽기 (바이너리 데이터)
                connection.inputStream.use { input ->
                    input.readBytes()
                }
            } else {
                // 오류 응답 로깅 및 빈 바이트 배열 반환
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e(TAG, "HTTP Error: $statusCode - $errorResponse")
                ByteArray(0)
            }

            return@withContext Pair(responseBytes, statusCode)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading audio: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 파일을 다운로드하는 함수
     * @param url 다운로드 URL
     * @param destination 저장할 파일 객체
     * @return Pair<Boolean, Int> - 성공 여부와 HTTP 상태 코드
     */
    suspend fun downloadFile(
        url: String,
        destination: File,
        headers: Map<String, String> = emptyMap()
    ): Pair<Boolean, Int> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = LONG_TIMEOUT // 파일 다운로드는 더 긴 타임아웃
                readTimeout = LONG_TIMEOUT

                // 헤더 추가
                headers.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }
            }

            val statusCode = connection.responseCode
            if (statusCode in 200..299) {
                // 성공 응답 읽고 파일로 저장
                connection.inputStream.use { input ->
                    FileOutputStream(destination).use { output ->
                        input.copyTo(output)
                    }
                }
                Pair(true, statusCode)
            } else {
                Log.e(TAG, "HTTP Error: $statusCode")
                Pair(false, statusCode)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: ${e.message}", e)
            Pair(false, -1)
        } finally {
            connection?.disconnect()
        }
    }
    /**
     * HTTP 오류 코드에 따른 사용자 친화적인 메시지 생성
     * @param statusCode HTTP 상태 코드
     * @param errorMessage 서버로부터 받은 오류 메시지
     * @return 사용자 친화적인 오류 메시지
     */
    fun handleHttpError(statusCode: Int, errorMessage: String): String {
        return when (statusCode) {
            400 -> "잘못된 요청입니다: $errorMessage"
            401 -> "인증이 필요합니다."
            403 -> "접근이 거부되었습니다."
            404 -> "요청한 리소스를 찾을 수 없습니다."
            408 -> "요청 시간이 초과되었습니다."
            429 -> "너무 많은 요청을 보냈습니다. 잠시 후 다시 시도해주세요."
            500 -> "서버 내부 오류가 발생했습니다."
            502 -> "서버로부터 잘못된 응답을 받았습니다."
            503 -> "서비스를 일시적으로 사용할 수 없습니다."
            504 -> "서버 응답 시간이 초과되었습니다."
            else -> "오류가 발생했습니다 (코드: $statusCode): $errorMessage"
        }
    }

    /**
     * 네트워크 타임아웃 조정 함수 (필요한 경우 사용)
     * @param connectionTimeoutMs 연결 타임아웃 (밀리초)
     * @param readTimeoutMs 읽기 타임아웃 (밀리초)
     * @return HttpURLConnection의 설정을 위한 확장 함수
     */
    private fun HttpURLConnection.setTimeouts(connectionTimeoutMs: Int, readTimeoutMs: Int) {
        this.connectTimeout = connectionTimeoutMs
        this.readTimeout = readTimeoutMs
    }
}