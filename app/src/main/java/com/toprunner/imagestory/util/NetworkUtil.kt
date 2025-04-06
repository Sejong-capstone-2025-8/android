package com.toprunner.imagestory.util

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NetworkUtil {
    companion object {
        private const val TAG = "NetworkUtil"
        private const val TIMEOUT = 60000 // 60초 (대용량 응답을 위해 긴 타임아웃)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun sendHttpRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String? = null
    ): String {
        var connection: HttpURLConnection? = null
        try {
            Log.d(TAG, "Sending $method request to $url")

            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT

            // 헤더 설정
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            // POST, PUT 등의 메서드일 경우 본문 추가
            if (body != null && (method == "POST" || method == "PUT" || method == "PATCH")) {
                connection.doOutput = true

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(body)
                    writer.flush()
                }
            }

            // 응답 코드 확인
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            // 응답 읽기
            if (responseCode in 200..299) {
                // 성공 응답
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    return response.toString()
                }
            } else {
                // 오류 응답
                val errorMessage = BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    response.toString()
                }

                throw Exception("HTTP 오류 $responseCode: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "HTTP request error: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    fun downloadFile(url: String, destination: File): Boolean {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Download file error: HTTP $responseCode")
                return false
            }

            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: ${e.message}", e)
            return false
        } finally {
            connection?.disconnect()
        }
    }

    fun downloadAudio(url: String, headers: Map<String, String>, body: String): ByteArray {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT

            // 헤더 설정
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            // 요청 본문 추가
            connection.doOutput = true
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body)
                writer.flush()
            }

            // 응답 코드 확인
            val responseCode = connection.responseCode
            Log.d(TAG, "Audio download response code: $responseCode")

            // 응답 읽기
            if (responseCode in 200..299) {
                // 성공 응답 - 오디오 데이터 읽기
                val outputStream = ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
                return outputStream.toByteArray()
            } else {
                // 오류 응답
                val errorMessage = BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    response.toString()
                }

                throw Exception("HTTP 오류 $responseCode: $errorMessage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading audio: ${e.message}", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }

    fun handleHttpError(statusCode: Int, errorMessage: String): String {
        return when (statusCode) {
            400 -> "잘못된 요청입니다: $errorMessage"
            401 -> "인증에 실패했습니다: $errorMessage"
            403 -> "접근이 거부되었습니다: $errorMessage"
            404 -> "요청한 리소스를 찾을 수 없습니다: $errorMessage"
            429 -> "요청이 너무 많습니다. 잠시 후 다시 시도해주세요: $errorMessage"
            500, 502, 503, 504 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요: $errorMessage"
            else -> "오류가 발생했습니다 (코드: $statusCode): $errorMessage"
        }
    }
}