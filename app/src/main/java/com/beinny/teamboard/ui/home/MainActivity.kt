package com.beinny.teamboard.ui.home

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivityMainBinding
import com.beinny.teamboard.databinding.NavHeaderMainBinding
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.models.User
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.home.createboard.CreateBoardActivity
import com.beinny.teamboard.ui.login.IntroActivity
import com.beinny.teamboard.ui.myprofile.MyProfileActivity
import com.beinny.teamboard.ui.common.ViewModelFactory
import com.beinny.teamboard.ui.home.boardList.BoardListFragment
import com.beinny.teamboard.ui.home.boardList.BoardListViewModel
import com.beinny.teamboard.ui.home.bookmark.BookmarkFragment
import com.beinny.teamboard.ui.home.notification.NotificationFragment
import com.beinny.teamboard.utils.Constants
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, BoardListFragment.CallBacks {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mUserId: String
    private lateinit var mUserName: String
    private lateinit var mUserBookmarkedBoards: ArrayList<String>

    private lateinit var mSharedPreferences: SharedPreferences // 토큰 업데이트 여부
    private val boardListViewModel: BoardListViewModel by viewModels { ViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupNavigation()

        /** 기본으로 보여지는 프래그먼트 */
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.appBarMain.contentMain.fragmentContainerView.id, BoardListFragment())
                .commit()
        }

        /** 토큰 갱신 및 유저 데이터 불러오기 */
        mSharedPreferences = this.getSharedPreferences(Constants.TEAMBOARD_PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)
        updateTokenAndLoadUserData(tokenUpdated)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentToken = task.result
                Log.d("FCM_TOKEN", "현재 핸드폰 FCM 토큰: $currentToken")
            } else {
                Log.e("FCM_TOKEN", "토큰 가져오기 실패", task.exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        /** 새 보드를 생성하고 돌아왔을때 기본으로 boardList를 출력 */
        loadFragment(BoardListFragment())
        binding.appBarMain.bottomNavigationView.menu.findItem(R.id.nav_home).isChecked = true
    }

    /** [백 프레스 설정] */
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) // Navigation Drawer 열려 있으면 닫기
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        else
            doubleBackToExit() // 닫혀 있으면 뒤로 가기
    }

    /** [네비게이션 아이템 클릭 리스너 설정] */
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = false

        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut() // 디바이스에서 로그아웃
                mSharedPreferences.edit().clear().apply() // sharedPreferences의 토큰 업데이트 여부 초기화

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
            FirestoreClass().getBoardsList(this@MainActivity)
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

            R.id.nav_create_board -> {
                val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
                // 보드를 만든 사용자 명 전달
                intent.putExtra(Constants.NAME, mUserName)
                startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /** [북마크 추가 및 해제 : BoardListFragment() 콜백 구현] */
    override fun bookmarkOnOff(boardDocId : String, bookmarked : Boolean) {
        if (bookmarked)
            mUserBookmarkedBoards.remove(boardDocId)
        else
            mUserBookmarkedBoards.add(boardDocId)

        FirestoreClass().userBookmarkOnOff(this@MainActivity, mUserId, mUserBookmarkedBoards)
    }

    /** [프래그먼트 화면 전환] */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.appBarMain.contentMain.fragmentContainerView.id, fragment)
            .commit()
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.appBarMain.toolbarMainActivity)
    }

    /** [네비게이션 리스너 및 속성 설정] */
    private fun setupNavigation() {
        /** 네비게이션(Drawer) */
        binding.navViewMain.setNavigationItemSelectedListener(this)
        val width = resources.displayMetrics.widthPixels
        binding.navViewMain.layoutParams.width = width

        val headerBinding: NavHeaderMainBinding = DataBindingUtil.bind(binding.navViewMain.getHeaderView(0))!!
        headerBinding.ivNavHeaderBackArrow.setOnClickListener {
            toggleDrawer()
        }

        /** 네비게이션(하단) */
        binding.appBarMain.bottomNavigationView.setOnItemSelectedListener {menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(BoardListFragment())
                    true
                }
                R.id.nav_bookmark -> {
                    loadFragment(BookmarkFragment())
                    true
                }
                R.id.nav_notification -> {
                    loadFragment(NotificationFragment())
                    true
                }
                R.id.nav_create_board -> {
                    val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
                    intent.putExtra(Constants.NAME, mUserName) // 보드를 만든 사용자 명 전달
                    startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
                    return@setOnItemSelectedListener true
                }
                else -> return@setOnItemSelectedListener false
            }
        }

        /** 뱃지 설정 */
        boardListViewModel.unreadCount.observe(this) { count ->
            updateBadgeCount(count)
        }
    }

    /** [Navigation Drawer 열기,닫기] */
    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    /** [하단 네비게이션 알림 뱃지 업데이트] */
    fun updateBadgeCount(count: Int) {
        val badge = binding.appBarMain.bottomNavigationView.getOrCreateBadge(R.id.nav_notification)
        badge.badgeGravity = BadgeDrawable.TOP_END

        if (count >0) {
            badge.isVisible = true
            badge.number = count
        } else {
            binding.appBarMain.bottomNavigationView.removeBadge(R.id.nav_notification)
        }
    }

    /** [네비게이션 뷰의 사용자 정보를 업데이트 : FirestoreClass()에서 호출]
     * @param isToReadBoardsList true일때 보드 목록을 불러온다. */
    fun updateNavigationUserDetails(user: User, isToReadBoardsList: Boolean) {
        hideProgressDialog()

        mUserBookmarkedBoards = user.bookmarkedBoards // 북마크 기능
        mUserId = user.id // 북마크 기능
        mUserName = user.name

        val headerBinding: NavHeaderMainBinding = DataBindingUtil.bind(binding.navViewMain.getHeaderView(0))!! // NavigationView의 헤더 뷰에 데이터 바인딩 설정
        headerBinding.user = user // UI 갱신 (데이터 바인딩)

        if (isToReadBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this@MainActivity)
        }
    }

    /** [보드 목록을 UI에 출력 : FirestoreClass()에서 호출] */
    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        val bookmarkedBoardList = ArrayList<Board>() // 북마크 바에 넘겨줄 인스턴스
        if (mUserBookmarkedBoards.size > 0) {
            for (i in boardsList.indices) {
                for (j in mUserBookmarkedBoards) {
                    if (boardsList[i].documentId == j) {
                        boardsList[i].bookmarked = true // 북마크 체킹
                        bookmarkedBoardList.add(boardsList[i])
                    }
                }
            }
        }

        hideProgressDialog()
        boardListViewModel.updateBoardList(boardsList)
        boardListViewModel.updateBookmarkedBoardList(bookmarkedBoardList)
    }

    /** [토큰을 업데이트하고 사용자 정보를 불러온다] */
    private fun updateTokenAndLoadUserData(tokenUpdated: Boolean) {
        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this@MainActivity, true)
        } else {
            // FirebaseInstanceId : FCM 토큰을 관리하는 클래스
            // 이 클래스는 디바이스에 고유한 FCM 토큰을 생성하고 관리하며, 해당 토큰을 통해 푸시 알림을 받을 수 있다.
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                updateFCMToken(instanceIdResult)
            }
        }
    }

    /** [사용자의 FCM 토큰을 데이터베이스에 등록] */
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
        const val MY_PROFILE_REQUEST_CODE: Int = 11 // 프로필 변경 요청(MyProfileActivity에서 OK) 및 결과 받을 때
        const val CREATE_BOARD_REQUEST_CODE: Int = 12 // 보드 생성 요청(CreateBoardActivity에서 OK) 및 결과 받을 때
    }
}