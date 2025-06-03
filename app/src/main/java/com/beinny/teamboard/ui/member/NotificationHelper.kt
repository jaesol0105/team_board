package com.beinny.teamboard.ui.member

import android.content.Context
import android.util.Log
import com.beinny.teamboard.utils.Constants
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class NotificationHelper (
    private val context: Context
) {
    suspend fun sendMemberInviteNotification(boardName: String, token: String, inviterName: String) = withContext(Dispatchers.IO) {
        val accessToken = try {
            getAccessToken()
        } catch (e: Exception) {
            Log.e("NotificationHelper", "FCM 토큰 가져오기 실패", e)
            return@withContext
        }

        // 3. FCM HTTP v1 API URL 설정
        val connection = (URL(Constants.FCM_HTTP_V1_API_URL).openConnection() as HttpURLConnection).apply {

            /** URL 연결은 입력 및 출력에 사용될 수 있다. 출력에 사용하고자 하는 경우 DoOutput 플래그를 true로 설정 (기본 값은 false) */
            doOutput = true // 데이터를 받기 위한 설정
            doInput = true // 데이터를 보내기 위한 설정

            requestMethod = "POST" // URL의 요청 메서드를 설정 : POST

            instanceFollowRedirects = false // HTTP 요청에서 리디렉션이 발생할 때, 리디렉션을 자동으로 따를지 여부
            useCaches = false // 캐시를 무시함

            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("charset", "utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $accessToken") // 토큰을 통해 요청의 인증을 처리함
        }

        // 4. JSON 생성 및 전송 : 푸시 알림에 표시될 제목과 메시지를 포함
        val jsonRequest = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", token) // 상대방 유저의 FCM 디바이스 토큰
                put("data", JSONObject().apply {
                    put("title", "[${boardName}] 초대 알림")
                    put("body", "${inviterName}님이 당신을 ${boardName}에 초대했습니다.")
                })
            })
        }

        try {
            val jsonBytes = jsonRequest.toString().toByteArray(Charsets.UTF_8) // UTF-8 인코딩

            // FCM을 서버에 전송한다
            connection.outputStream.use { os ->
                os.write(jsonBytes)
                os.flush()
            } // use : 자동 close

            Log.d("FCM_JSON", jsonRequest.toString())

            // 5. 서버 응답 처리
            val responseCode = connection.responseCode // status code

            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = inputStream.bufferedReader().use { it.readText() }

            if (responseCode in 200..299) {
                Log.d("NotificationHelper", "FCM 전송 성공: $responseBody")
            } else {
                Log.e("NotificationHelper", "FCM 실패 $responseCode: $responseBody")
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "FCM 전송 에러", e)
        } finally {
            connection.disconnect()
        }
    }

    private fun getAccessToken(): String {
        /** serviceAccountStream 은 JSON 파일의 내용을 담고 있는 InputStream 객체
         * 서비스 계정 키 파일(JSON 형식)을 읽어서 GoogleCredentials 객체를 생성
         * GoogleCredentials 객체는 이 정보를 바탕으로 Google API에 대한 인증을 수행할 수 있는 OAuth 2.0 토큰을 생성
         * createScoped 메서드는 특정 API 접근 범위(scope)를 지정하여 OAuth 2.0 토큰을 발급받도록 설정 */

        // 1. 서비스 계정 JSON 파일을 사용해 GoogleCredentials 객체를 초기화
        val serviceAccountStream = context.assets.open("teamboard-1451c-firebase-adminsdk-hf699-2bf31dbf12.json")
        val googleCredentials = GoogleCredentials.fromStream(serviceAccountStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

        // 2.  OAuth 2.0 인증 토큰 갱신
        googleCredentials.refreshIfExpired()
        return googleCredentials.accessToken.tokenValue
    }
}