package com.beinny.teamboard.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ActivityMainBinding
import com.beinny.teamboard.databinding.NavHeaderMainBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.createboard.CreateBoardActivity
import com.beinny.teamboard.ui.login.IntroActivity
import com.beinny.teamboard.ui.myprofile.MyProfileActivity
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.boardlist.BoardListFragment
import com.beinny.teamboard.ui.shared.BoardListViewModel
import com.beinny.teamboard.ui.bookmark.BookmarkFragment
import com.beinny.teamboard.ui.common.launch
import com.beinny.teamboard.ui.common.launchIntent
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.notification.NotificationFragment
import com.beinny.teamboard.ui.state.UiState
import com.beinny.teamboard.utils.Constants
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.navigation.NavigationView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: BoardListViewModel by viewModels { ViewModelFactory(applicationContext) }
    private lateinit var activityResultHelper: ActivityResultHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        setupActionBar()
        setupLaunchers()
        setupObservers()
        setupNavigations()

        viewModel.checkAndUpdateFCMToken()
    }

    override fun onResume() {
        super.onResume()

        loadFragment(BoardListFragment())
        binding.appBarMain.bottomNavigationView.menu.findItem(R.id.nav_home).isChecked = true
    }

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.appBarMain.toolbarMainActivity)
    }

    private fun setupLaunchers() {
        activityResultHelper = ActivityResultHelper(this,
            onCreateBoardResult = {
                viewModel.loadUserAndBoard()
            },
            onMyProfileResult = {
                viewModel.loadUser()
            }
        )
    }

    private fun setupObservers() {
        /** 뱃지 설정 */
        repeatOnStarted {
            viewModel.unreadNotificationsCount.collect { count ->
                updateBadgeCount(count)
            }
        }
        /** Drawer Navigation 프로필 갱신 */
        repeatOnStarted {
            viewModel.user.collect { user ->
                val headerBinding: NavHeaderMainBinding = DataBindingUtil.bind(binding.navViewMain.getHeaderView(0))!!
                headerBinding.user = user
            }
        }
        /** ui state */
        repeatOnStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showProgressDialog(resources.getString(R.string.please_wait))
                    is UiState.Success, is UiState.Idle -> hideProgressDialog()
                    is UiState.Error -> {
                        hideProgressDialog()
                        showErrorSnackBar(state.message)
                    }
                }
            }
        }
    }

    /** 하단 네비게이션 알림 뱃지 업데이트 */
    fun updateBadgeCount(count: Int) {
        val badge = binding.appBarMain.bottomNavigationView.getOrCreateBadge(R.id.nav_notification).apply {
            badgeGravity = BadgeDrawable.TOP_END
        }
        if (count >0) {
            badge.isVisible = true
            badge.number = count
        } else {
            binding.appBarMain.bottomNavigationView.removeBadge(R.id.nav_notification)
        }
    }

    /** 네비게이션 리스너 및 속성 설정 */
    private fun setupNavigations() {
        setupDrawerNav()
        setupBottomNav()
    }

    /** Drawer 네비게이션 설정 */
    private fun setupDrawerNav() {
        val width = resources.displayMetrics.widthPixels
        binding.navViewMain.apply {
            setNavigationItemSelectedListener(this@MainActivity)
            layoutParams.width = width
        }

        val headerBinding: NavHeaderMainBinding = DataBindingUtil.bind(binding.navViewMain.getHeaderView(0))!!
        headerBinding.ivNavHeaderBackArrow.setOnClickListener {
            toggleDrawer()
        }
    }

    /** Bottom 네비게이션 설정 */
    private fun setupBottomNav() {
        binding.appBarMain.bottomNavigationView.setOnItemSelectedListener { menuItem ->
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
                    launchIntent<CreateBoardActivity>(activityResultHelper.createBoardLauncher){
                        putExtra(Constants.NAME, viewModel.user.value.name) // 보드를 만든 사용자 명 전달
                    }
                    return@setOnItemSelectedListener true
                }

                else -> return@setOnItemSelectedListener false
            }
        }
    }

    /** Navigation Drawer 열기,닫기 */
    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    /** 백프레스 - Navigation Drawer 열려 있으면 닫기 */
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END))
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        else
            doubleBackToExit()
    }

    /** 네비게이션 아이템 클릭 리스너 설정 */
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = false

        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                launchIntent<MyProfileActivity>(activityResultHelper.myProfileLauncher)
                overridePendingTransition(0, 0)
            }
            R.id.nav_sign_out -> {
                viewModel.signOut {
                    launch<IntroActivity>(
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK,
                        finishCurrent = true
                    )
                }
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
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

    /** 프래그먼트 화면 전환 */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.appBarMain.contentMain.fragmentContainerView.id, fragment)
            .commit()
    }
}