package com.beinny.teamboard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.beinny.teamboard.R
import com.beinny.teamboard.ServiceLocator
import com.beinny.teamboard.data.model.NotificationEntity
import com.beinny.teamboard.data.source.remote.firebase.FirestoreClass
import com.beinny.teamboard.ui.main.MainActivity
import com.beinny.teamboard.ui.login.SignInActivity
import com.beinny.teamboard.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /** 앱이 FCM을 통해 메시지를 수신할 때 호출됨
     * @param remoteMessage FCM 서버에서 받은 메시지
     * @param remoteMessage.from 메시지를 보낸 발신자의 식별자
     * @param remoteMessage.data 메시지의 데이터 페이로드를 포함 (키-값) */
    override fun onMessageReceived(remoteMessage: RemoteMessage)
    {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 메시지에 데이터 페이로드가 포함되어 있는지 확인
        remoteMessage.data.isNotEmpty().let {
            Log.i(TAG, "Message data payload: " + remoteMessage.data)

            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]?: "알림"
            val message = remoteMessage.data["body"] ?: "내용 없음"

            val notification = NotificationEntity(
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            // 기기에 알림을 띄운다
            sendNotification(title, message)
            // Room에 알림 메세지 저장
            CoroutineScope(Dispatchers.IO).launch {
                val repository = ServiceLocator.provideNotificationRepository(applicationContext)
                repository.insertNotification(notification)
            }
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    /** FCM 토큰의 생성 또는 갱신될 때 호출 */
    override fun onNewToken(token: String) {
        Log.e(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    /** 새로 생성된 FCM 토큰을 서버에 등록 */
    private fun sendRegistrationToServer(token: String?) {
        // 비즈니스 로직에서 구현 했음.
    }

    /** FCM 메시지를 수신했을 때 알림(Notification)을 생성 */
    private fun sendNotification(title:String, messageBody: String) {
        // 알림을 터치했을 때 수행할 동작 정의
        val intent: Intent = if (FirestoreClass().getCurrentUserID().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, SignInActivity::class.java)
        }

        /** FLAG_ACTIVITY_CLEAR_TASK : 테스크 내 모든 Activity 제거
         * FLAG_ACTIVITY_NEW_TASK : 새로운 태스크(Task)로 Activity 시작
         * FLAG_ACTIVITY_CLEAR_TOP : 현재 태스크 내에 동일한 Activity가 이미 존재할 경우 (재사용) 해당 Activity 위의 모든 Activity 제거 */
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        /** Android 8.0 (Oreo, API 레벨 26) 이상에서는 Notification Channel을 사용하여 알림을 관리한다.
         * 각 알림은 특정 채널에 할당되며, 이 채널은 알림의 중요도, 소리, 진동 등의 설정을 제어한다.
         * 채널 id : 채널을 식별하는 문자열 */
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 디바이스의 기본 알림 소리의 URI

        // 알림의 기본 설정(아이콘, 제목, 내용, 사운드 등)을 정의
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.teamboard_icon)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_teamboard) // 상태바용 (단색)
            .setLargeIcon(bitmap) // 본문에 컬러 아이콘 적용
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        // 알림을 시스템에 표시하는 데 사용한다
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /** NotificationChannel : 알림 채널
             * (알림 채널 ID , 알림 채널의 이름 , 알림의 중요도) */
            val channel = NotificationChannel(channelId, "Channel Teamboard title", NotificationManager.IMPORTANCE_HIGH)

            // 알림 채널을 시스템에 등록, 이후 생성되는 모든 알림이 이 채널을 통해 전달되도록 한다
            notificationManager.createNotificationChannel(channel)
        }

        /** 알림을 생성하고, 사용자의 상태바와 알림 패널에 표시한다
         * ID of notification : 알림을 식별하기 위한 고유한 값
         * 만약 같은 ID를 가진 알림을 다시 발행하면, 이전 알림이 업데이트되거나 교체된다 */
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}