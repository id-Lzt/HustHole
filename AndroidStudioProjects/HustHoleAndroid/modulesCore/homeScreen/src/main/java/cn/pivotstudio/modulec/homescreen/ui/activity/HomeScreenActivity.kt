package cn.pivotstudio.modulec.homescreen.ui.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import cn.pivotstudio.moduleb.database.MMKVUtil
import cn.pivotstudio.modulec.homescreen.R
import cn.pivotstudio.modulec.homescreen.custom_view.dialog.UpdateDialog
import cn.pivotstudio.modulec.homescreen.custom_view.dialog.WelcomeDialog
import cn.pivotstudio.modulec.homescreen.databinding.ActivityHsHomescreenBinding
import cn.pivotstudio.modulec.homescreen.network.VersionResponse
import cn.pivotstudio.modulec.homescreen.repository.HomeScreenRepository
import cn.pivotstudio.modulec.homescreen.ui.fragment.HomePageFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import cn.pivotstudio.moduleb.libbase.BuildConfig
import cn.pivotstudio.moduleb.libbase.base.ui.activity.BaseActivity
import cn.pivotstudio.moduleb.libbase.constant.Constant
import cn.pivotstudio.modulec.homescreen.ui.fragment.ForestDetailFragment
import cn.pivotstudio.modulec.homescreen.ui.fragment.ForestFragment

/**
 * @classname: HomeScreenActivity
 * @description:
 * @date: 2022/4/28 20:58
 * @version:1.0
 * @author:
 */
@Route(path = "/homeScreen/HomeScreenActivity")
class HomeScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityHsHomescreenBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_hs_homescreen)
        initView()
        checkVersion()
    }

    /**
     * fragment?????????onActivityResult?????????????????????????????????navigation???activity???onActivityResult??????????????????????????????fragment???onActivityResult?????????????????????
     */
    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val mMainNavFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val fragment = mMainNavFragment!!.childFragmentManager.primaryNavigationFragment
        when (fragment) {
            is HomePageFragment,
            is ForestDetailFragment,
            is ForestFragment -> {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    /**
     * ???????????????
     */
    private fun initView() {

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.homeScreenToolbar.let {
            setSupportActionBar(it)
            it.setupWithNavController(
                navController,
                AppBarConfiguration(
                    setOf(
                        R.id.homepage_fragment,
                        R.id.forest_fragment,
                        R.id.notice_fragment,
                        R.id.mine_fragment,
                        R.id.forest_detail_fragment
                    )
                )
            )
        }

        navController.addOnDestinationChangedListener { _, destination, argument ->
            supportActionBar?.title = destination.label

            // BottomNavigationBar??????????????????
            binding.apply {
                layoutBottomBar.isVisible =
                    (destination.id != R.id.all_forest_fragment && destination.id != R.id.forest_detail_fragment)

                bottomNavigationView.setupWithNavController(navController)
                bottomNavigationView.background = null
            }

            // ActionBar??????????????????
            supportActionBar?.let {
                if (destination.id == R.id.forest_detail_fragment) {
                    it.hide()
                } else {
                    it.show()
                }
            }

        }

    }

    /**
     * ??????????????????
     */
    private fun packageName(context: Context): String? {
        val manager = context.packageManager
        var name: String? = null
        try {
            val info = manager.getPackageInfo(context.packageName, 0)
            name = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return name
    }

    /**
     * ???????????????????????????????????????
     */
    private fun checkVersion() {
        val homeScreenRepository = HomeScreenRepository()
        homeScreenRepository.getVersionMsgForNetwork()
        homeScreenRepository.pHomeScreenVersionMsg.observe(this) { versionResponse: VersionResponse ->
            val oldVersion = packageName(this@HomeScreenActivity)
            val lastVersion = versionResponse.androidversion
            val downloadUrl = versionResponse.androidUpdateUrl
            if (lastVersion != oldVersion) { //???????????????????????????
                val updateDialog =
                    UpdateDialog(this@HomeScreenActivity, oldVersion, lastVersion, downloadUrl)
                updateDialog.show()
            } else { //???????????????
                val mmkvUtil = MMKVUtil.getMMKV(this)
                if (!mmkvUtil.getBoolean(Constant.IS_FIRST_USED)) { //?????????????????????1037??????,??????welcomeDialog???????????????????????????
                    val welcomeDialog = WelcomeDialog(context)
                    welcomeDialog.show()
                    mmkvUtil.put(Constant.IS_FIRST_USED, true)
                }
            }
        }
    }

    /**
     * ??????????????????
     */
    fun jumpToPublishHoleByARouter(v: View) {
        val id = v.id
        if (id == R.id.fab_homescreen_publishhole) {
            if (BuildConfig.isRelease) {
                ARouter.getInstance().build("/publishHole/PublishHoleActivity").navigation()
            } else {
                showMsg("???????????????????????????")
            }
        }
    }

    private var firstTime: Long = 0

    /**
     * ????????????????????????????????????
     *
     * @param keyCode
     * @param event
     * @return
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        navController.currentDestination?.let {
            if (it.id == R.id.all_forest_fragment || it.id == R.id.forest_detail_fragment) {
                return navController.popBackStack()
            }

            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                val secondTime = System.currentTimeMillis()
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(this@HomeScreenActivity, "????????????????????????", Toast.LENGTH_SHORT).show()
                    firstTime = secondTime
                    return true
                } else {
                    finish()
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }
}