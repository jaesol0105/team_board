package com.beinny.teamboard.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.adapters.MemberAdapter
import com.beinny.teamboard.databinding.ActivityMembersBinding
import com.beinny.teamboard.databinding.DialogSearchMemberBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.models.User
import com.beinny.teamboard.utils.Constants
import com.google.auth.oauth2.GoogleCredentials
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding

    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList:ArrayList<User>
    private var anyChangesDone : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 값을 넘겨 받고 글로벌 변수에 할당
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(
            this@MembersActivity,
            mBoardDetails.assignedTo
        )
    }

    /** [백 프레스 : 변경사항이 있으면 RESULT_OK] */
    override fun onBackPressed() {
        if (anyChangesDone) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    /** [멤버 목록을 recyclerView에 출력] */
    fun setupMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list

        hideProgressDialog()

        binding.rvMembersList.layoutManager = LinearLayoutManager(this@MembersActivity)
        binding.rvMembersList.setHasFixedSize(true)

        val adapter = MemberAdapter(this@MembersActivity, list)
        binding.rvMembersList.adapter = adapter
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMembersActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding.toolbarMembersActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                // 멤버를 등록하기 위해, 이메일을 통해서 멤버를 검색하는 대화상자를 출력
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** [이메일을 통해서 사용자를 검색하는 대화상자를 출력] */
    private fun dialogSearchMember() {
        val dialog = Dialog(this)

        val dialogSearchMemberBinding = DialogSearchMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogSearchMemberBinding.root)

        dialogSearchMemberBinding.tvAdd.setOnClickListener(View.OnClickListener {
            val email = dialogSearchMemberBinding.etEmailSearchMember.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()

                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this@MembersActivity, email)
            } else {
                showErrorSnackBar("Please enter members email address.")
            }
        })
        dialogSearchMemberBinding.tvCancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        dialog.show()
    }

    /** [사용자를 보드의 멤버로 등록한다 : FirestoreClass()에서 호출] */
    fun memberDetails(user: User) {
        // 사용자 ID를 보드의 기존의 멤버 목록에 추가한다.
        mBoardDetails.assignedTo.add(user.id)
        // 데이터베이스에 저장
        FirestoreClass().assignMemberToBoard(this@MembersActivity, mBoardDetails, user)
    }

    /** [보드 멤버 등록 결과를 받아온다 : FirestoreClass()에서 호출] */
    fun memberAssignSuccess(user: User) {
        hideProgressDialog()

        mAssignedMembersList.add(user)
        anyChangesDone = true // 맴버 등록 성공 처리
        setupMembersList(mAssignedMembersList) //새로고침

        // 성공적으로 멤버를 등록했을때, 해당 사용자의 FCM 토큰을 통해 알림을 전송
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    /** [FCM Token을 기반으로 사용자에게 알림을 보내기 위한 AsyncTask 클래스] */
    @SuppressLint("StaticFieldLeak")
    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) : AsyncTask<Any, Void, String>() {
        // Asynctask : 새로운 비동기 작업을 만든다. UI 쓰레드에서 실행되어야한다.

        // 백그라운드 실행 전에 수행할 작업
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        // 백그라운드에서 실행될 작업 : 네트워크 작업(FCM 서버에 요청)을 수행
        override fun doInBackground(vararg params: Any): String {
            var result: String

            // HttpURLConnection : POST 요청을 수행하기 위해 HTTP 연결 설정
            var connection: HttpURLConnection? = null

            try {
                /** serviceAccountStream 은 JSON 파일의 내용을 담고 있는 InputStream 객체
                 * 서비스 계정 키 파일(JSON 형식)을 읽어서 GoogleCredentials 객체를 생성
                 * GoogleCredentials 객체는 이 정보를 바탕으로 Google API에 대한 인증을 수행할 수 있는 OAuth 2.0 토큰을 생성
                 * createScoped 메서드는 특정 API 접근 범위(scope)를 지정하여 OAuth 2.0 토큰을 발급받도록 설정 */

                // 1. 서비스 계정 JSON 파일을 사용해 GoogleCredentials 객체를 초기화
                val serviceAccountStream = resources.assets.open("teamboard-1451c-firebase-adminsdk-hf699-9d58a9d272.json")
                val googleCredentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                // 2.  OAuth 2.0 인증 토큰 갱신
                googleCredentials.refreshIfExpired()
                val accessToken = googleCredentials.accessToken.tokenValue

                // 3. FCM HTTP v1 API URL 설정
                val url = URL(Constants.FCM_HTTP_V1_API_URL)
                connection = url.openConnection() as HttpURLConnection

                // URL 연결은 입력 및/또는 출력에 사용될 수 있다.
                // URL 연결을 출력에 사용하고자 하는 경우 DoOutput 플래그를 true로 설정 (기본 값은 false)
                connection.doOutput = true // 데이터를 받기 위한 설정
                connection.doInput = true // 데이터를 보내기 위한 설정

                // HTTP 요청에서 리디렉션이 발생할 때, 리디렉션을 자동으로 따를지 여부
                connection.instanceFollowRedirects = false

                // URL의 요청 메서드를 설정 : POST
                connection.requestMethod = "POST"

                // JSON 형식의 데이터를 전송하고 수신
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                // 토큰을 통해 요청의 인증을 처리
                connection.setRequestProperty("Authorization", "Bearer $accessToken")

                // 캐시를 무시한다. (예: 브라우저의 새로고침).
                connection.useCaches = false

                // 4. JSON 생성 : 푸시 알림에 표시될 제목과 메시지를 포함한다
                val jsonRequest = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token) // 상대방 유저의 FCM 디바이스 토큰
                        put("data", JSONObject().apply {
                            put("title", "Assigned to the Board $boardName")
                            put("body", "You have been assigned to the new board by ${mAssignedMembersList[0].name}")
                        })
                    })
                }

                // 데이터 전송 : JSON 데이터를 DataOutputStream을 사용하여 FCM 서버에 전송한다
                val wr = DataOutputStream(connection.outputStream)
                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                // 5. 서버 응답 처리
                val httpResult: Int = connection.responseCode // status code

                // 서버에서 반환된 JSON 응답을 읽고, 결과를 문자열로 저장한다
                if (httpResult == HttpURLConnection.HTTP_OK) { // HTTP_OK(200)
                    val inputStream = connection.inputStream // 입력 스트림
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) // 한줄
                            sb.append(line + "\n")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    // HTTP 응답 메시지(있는 경우)를 status code와 함께 받는다
                    result = connection.responseMessage

                    val httpResult: Int = connection.responseCode
                    val responseBody: String

                    val inputStream = if (httpResult in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                    responseBody = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()

                    Log.e("JSON Response Result", "Code: $httpResult, Message: $result, Body: $responseBody")
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }

            // 결과를 PostExecute에 통지
            return result
        }

        /** [백그라운드 작업이 완료된 후 UI 스레드에서 실행] */
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            hideProgressDialog()

            Log.e("JSON Response Result", result) // JSON 결과
        }
    }
}