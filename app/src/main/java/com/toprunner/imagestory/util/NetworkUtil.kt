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

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun sendHttpRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String? = null
    ): String = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 300000 // 300초
                readTimeout = 300000 // 300초

                // 헤더 설정
                headers.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }

                // POST 요청인 경우 body 추가
                if (method == "POST" && body != null) {
                    doOutput = true
                    outputStream.use { os ->
                        os.write(body.toByteArray())
                        os.flush()
                    }
                }
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                // 성공 응답 읽기
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                // 오류 응답 읽기
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorMessage = "HTTP Error: $responseCode - $errorResponse"
                Log.e(TAG, errorMessage)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in HTTP request: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun downloadAudio(
        url: String,
        headers: Map<String, String>,
        requestBody: String
    ): ByteArray = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 60000 // 1분
                readTimeout = 60000 // 1분

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

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                // 성공 응답 읽기 (바이너리 데이터)
                connection.inputStream.use { input ->
                    input.readBytes()
                }
            } else {
                // 오류 응답 읽기
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorMessage = "HTTP Error: $responseCode - $errorResponse"
                Log.e(TAG, errorMessage)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading audio: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun downloadFile(url: String, destination: File): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 30000 // 30초
                readTimeout = 30000 // 30초
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                // 성공 응답 읽고 파일로 저장
                connection.inputStream.use { input ->
                    FileOutputStream(destination).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: ${e.message}", e)
            false
        } finally {
            connection?.disconnect()
        }
    }

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
}