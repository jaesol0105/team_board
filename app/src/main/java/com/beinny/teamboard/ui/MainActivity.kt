package com.beinny.teamboard.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.adapters.BoardAdapter
import com.beinny.teamboard.databinding.ActivityMainBinding
import com.beinny.teamboard.databinding.NavHeaderMainBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.models.User
import com.beinny.teamboard.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mUserName: String // 사용자 명 글로벌 변수
    private lateinit var mSharedPreferences: SharedPreferences // 토큰 업데이트 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        // 네비게이션 리스너 설정 및 속성
        binding.navViewMain.setNavigationItemSelectedListener(this)
        val width = resources.displayMetrics.widthPixels
        binding.navViewMain.layoutParams.width = width

        val headerBinding: NavHeaderMainBinding = DataBindingUtil.bind(binding.navViewMain.getHeaderView(0))!!
        headerBinding.ivNavHeaderBackArrow.setOnClickListener {
            toggleDrawer()
        }

        mSharedPreferences = this.getSharedPreferences(Constants.TEAMBOARD_PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        updateTokenAndLoadUserData(tokenUpdated)

        // 새 보드 생성 fab
        binding.appBarMain.fabCreateBoard.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            // 보드를 만든 사용자 명 전달
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    /** [백 프레스] */
    override fun onBackPressed() {
        // Navigation Drawer 열려 있으면 닫기
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END))
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        // 닫혀 있으면 뒤로 가기
        else
            doubleBackToExit()
    }

    /** [네비게이션 아이템 클릭 리스너] */
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                // 디바이스에서 로그아웃
                FirebaseAuth.getInstance().signOut()

                // sharedPreferences의 토큰 업데이트 여부 초기화
                mSharedPreferences.edit().clear().apply()

                // Intro Activity로 이동
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /** 프로필을 수정했을 경우 */
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this@MainActivity) // 네비게이션의 프로필 갱신
        }
        /** 새로운 보드를 생성했을 경우 **/
        else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardsList(this@MainActivity) // 최신 보드 목록 불러오기
        }
        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drawer -> {
                toggleDrawer()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.appBarMain.toolbarMainActivity)

        // 액션 바에 네비게이션 아이콘 적용
        //binding.appBarMain.toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        // 네비게이션 아이콘에 리스너 설정
        //binding.appBarMain.toolbarMainActivity.setNavigationOnClickListener {
        //    toggleDrawer()
        //}
    }

    /** [Navigation Drawer 열기,닫기] */
    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    /** [네비게이션 뷰의 사용자 정보를 업데이트 : FirestoreClass()에서 호출]
     * @param isToReadBoardsList true일때 보드 목록을 불러온다. */
    fun updateNavigationUserDetails(user: User, isToReadBoardsList: Boolean) {
        hideProgressDialog()

        mUserName = user.name // 글로벌 변수

        // NavigationView의 헤더 뷰에 데이터 바인딩 설정
        val headerBinding: NavHeaderMainBinding = DataBindingUtil.bind(binding.navViewMain.getHeaderView(0))!!
        headerBinding.user = user // ui 갱신

        if (isToReadBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this@MainActivity)
        }
    }

    /** [보드 목록을 출력 : FirestoreClass()에서 호출] */
    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        if (boardsList.size > 0) {
            binding.appBarMain.contentMain.rvBoardsList.visibility = View.VISIBLE
            binding.appBarMain.contentMain.tvNoBoardsAvailable.visibility = View.GONE

            binding.appBarMain.contentMain.rvBoardsList.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.appBarMain.contentMain.rvBoardsList.setHasFixedSize(true)

            val adapter = BoardAdapter(this@MainActivity, boardsList)
            binding.appBarMain.contentMain.rvBoardsList.adapter = adapter

            // 보드의 각 item에 리스너 설정
            adapter.setOnClickListener(object :
                BoardAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java) // TaskActivity 실행
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId) // 해당 보드의 documentId를 전달
                    startActivity(intent)
                }
            })
        } else {
            binding.appBarMain.contentMain.rvBoardsList.visibility = View.GONE
            binding.appBarMain.contentMain.tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }

    /** [토큰을 업데이트하고 사용자 정보를 불러온다] */
    private fun updateTokenAndLoadUserData(tokenUpdated: Boolean) {
        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this@MainActivity, true)
        } else {
            // FirebaseInstanceId : FCM 토큰을 관리하는 클래스
            // 이 클래스는 디바이스에 고유한 FCM 토큰을 생성하고 관리하며, 해당 토큰을 통해 푸시 알림을 받을 수 있다.
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                updateFCMToken(instanceIdResult.token)
            }
        }
    }

    /** [사용자의 FCM 토큰을 데이터베이스에 업데이트] */
    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this@MainActivity, userHashMap)
    }

    /** [토큰 업데이트 성공 (SharedPreferences 저장) : FirestoreClass()에서 호출] */
    fun tokenUpdateSuccess() {
        hideProgressDialog()

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this@MainActivity, true)
    }

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11 // 프로필 변경 요청(MyProfileActivity에서 OK) 및 결과 받을때
        const val CREATE_BOARD_REQUEST_CODE: Int = 12 // 보드 생성 요청(CreateBoardActivity에서 OK) 및 결과 받을때
    }
}