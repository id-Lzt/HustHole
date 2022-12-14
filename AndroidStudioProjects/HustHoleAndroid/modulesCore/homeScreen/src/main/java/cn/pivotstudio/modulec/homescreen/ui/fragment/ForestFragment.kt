package cn.pivotstudio.modulec.homescreen.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import cn.pivotstudio.modulec.homescreen.BuildConfig
import cn.pivotstudio.modulec.homescreen.R
import cn.pivotstudio.modulec.homescreen.custom_view.dialog.DeleteDialog
import cn.pivotstudio.modulec.homescreen.custom_view.refresh.StandardRefreshFooter
import cn.pivotstudio.modulec.homescreen.custom_view.refresh.StandardRefreshHeader
import cn.pivotstudio.modulec.homescreen.databinding.FragmentForestBinding
import cn.pivotstudio.husthole.moduleb.network.model.ForestHole
import cn.pivotstudio.husthole.moduleb.network.model.HoleV2
import cn.pivotstudio.moduleb.libbase.base.model.HoleReturnInfo
import cn.pivotstudio.modulec.homescreen.repository.LoadStatus
import cn.pivotstudio.modulec.homescreen.ui.adapter.JoinedForestsAdapter
import cn.pivotstudio.modulec.homescreen.ui.adapter.ForestHoleAdapter
import cn.pivotstudio.modulec.homescreen.viewmodel.ForestViewModel
import com.alibaba.android.arouter.launcher.ARouter
import cn.pivotstudio.moduleb.libbase.base.ui.fragment.BaseFragment
import cn.pivotstudio.moduleb.libbase.constant.Constant
import cn.pivotstudio.moduleb.libbase.constant.ResultCodeConstant
import cn.pivotstudio.modulec.homescreen.viewmodel.AllForestViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * @classname: ForestFragment
 * @description:
 * @date: 2022/5/2 22:57
 * @version: 1.0
 * @author:
 */
class ForestFragment : BaseFragment() {
    private lateinit var binding: FragmentForestBinding
    private val forestViewModel: ForestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_forest, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == ResultCodeConstant.Hole) {
            val returnInfo = data!!.getParcelableExtra<HoleReturnInfo>(
                Constant.HOLE_RETURN_INFO
            )
            returnInfo?.let {
                forestViewModel.refreshLoadLaterHole(
                    it.is_thumbup,
                    it.is_reply,
                    it.is_follow,
                    it.thumbup_num,
                    it.reply_num,
                    it.follow_num
                )
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefresh()

        val holeAdapter = ForestHoleAdapter(
            this
        )

        val headAdapter = JoinedForestsAdapter(
            onItemClick = ::navToSpecificForest,
            navToAllForest = ::navToAllForests
        )

        // ???????????????RecyclerView???liveData?????????
        binding.apply {
            recyclerViewForestHoles.apply {
                adapter = holeAdapter
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        (adapter as ForestHoleAdapter).lastImageMore?.let {
                            if (it.isVisible) {
                                it.visibility = GONE
                            }
                        }
                    }
                })
            }

            recyclerViewForestHead.adapter = headAdapter
            viewModel = forestViewModel.apply {
                lifecycleScope.launchWhenStarted {
                    holesV2.collectLatest {
                        holeAdapter.submitList(it)
                    }
                }

                lifecycleScope.launchWhenStarted {
                    forestsV2.collectLatest {
                        headAdapter.submitList(it)
                    }
                }

                holesLoadState.observe(viewLifecycleOwner) { it ->
                    when (it) {
                        LoadStatus.DONE -> {
                            recyclerViewForestHoles.visibility = VISIBLE
                            forestPlaceholder.visibility = GONE
                            finishRefreshAnim()
                        }
                        LoadStatus.ERROR -> {
                            holesV2.value.takeIf { holes ->
                                holes.isEmpty()
                            }?.let {
                                forestPlaceholder.visibility = VISIBLE
                            }
                            finishRefreshAnim()
                        }
                        LoadStatus.LOADING -> {
                            forestPlaceholder.visibility =
                                if (recyclerViewForestHoles.isVisible) GONE else VISIBLE
                        }
                        else -> {
                            recyclerViewForestHoles.visibility = GONE
                            forestPlaceholder.visibility = VISIBLE
                            finishRefreshAnim()
                        }
                    }
                }

                tip.observe(viewLifecycleOwner) {
                    it?.let {
                        showMsg(it)
                        doneShowingTip()
                    }
                }
            }

            if (forestFragment == null) {
                this.forestFragment = this@ForestFragment
            }

        }

        preloadAllForests()
    }

    override fun onResume() {
        super.onResume()
        forestViewModel.tryLoadNewHeader()
    }


    /**
     * ?????? Navigation ????????? AllForestFragment
     *
     * ????????? [AllForestFragment],nav_graph.xml
     */
    fun navToAllForests() {
        forestViewModel.loadHeaderLater()
        findNavController(
            requireActivity(), R.id.nav_host_fragment
        ).navigate(R.id.action_forest_fragment_to_all_forest_fragment)
    }

    fun navToSpecificForest(forestId: String) {
        forestViewModel.loadHeaderLater()
        val action = ForestFragmentDirections.actionForestFragmentToForestDetailFragment(
            forestViewModel.getForestById(forestId)
        )
        findNavController(requireActivity(), R.id.nav_host_fragment).navigate(action)
    }

    // ?????????????????????????????????
    fun navToSpecificHole(holeId: Int) {
        forestViewModel.loadHoleLater(holeId)
        if (BuildConfig.isRelease) {
            ARouter.getInstance().build("/hole/HoleActivity").withInt(Constant.HOLE_ID, holeId)
                .withBoolean(Constant.IF_OPEN_KEYBOARD, false)
                .navigation(requireActivity(), ResultCodeConstant.Hole)
        }
    }

    // ?????????????????????????????????????????????????????????
    fun navToSpecificHoleWithReply(holeId: Int) {
        if (BuildConfig.isRelease) {
            ARouter.getInstance().build("/hole/HoleActivity").withInt(Constant.HOLE_ID, holeId)
                .withBoolean(Constant.IF_OPEN_KEYBOARD, true)
                .navigation(requireActivity(), ResultCodeConstant.Hole)
        }
    }

    // ??????
    fun giveALikeToTheHole(hole: HoleV2) {
        forestViewModel.giveALikeToTheHole(hole)
    }

    // ??????/??????
    fun followTheHole(hole: HoleV2) {
        forestViewModel.followTheHole(hole)
    }

    // ????????????????????????????????????
    fun reportTheHole(hole: ForestHole) {
        ARouter.getInstance().build("/report/ReportActivity").withInt(Constant.HOLE_ID, hole.holeId)
            .withInt(Constant.REPLY_LOCAL_ID, -1).withString(
                Constant.ALIAS, "??????"
            ).navigation()
    }

    // ????????????
    fun deleteTheHole(hole: ForestHole) {
        val dialog = DeleteDialog(context)
        dialog.show()
        dialog.setOptionsListener {
            forestViewModel.deleteTheHole(hole)
        }
    }

    // ????????????HomePageFragment??????????????????????????????????????????
    private fun initRefresh() {
        binding.refreshLayout.apply {
            setRefreshHeader(StandardRefreshHeader(activity)) //????????????????????????
            setRefreshFooter(StandardRefreshFooter(activity)) //????????????????????????
            setOnRefreshListener {    //??????????????????
                forestViewModel.loadHolesAndHeads()
                binding.recyclerViewForestHoles.isEnabled = false
            }
            setOnLoadMoreListener {   //??????????????????
                if (forestViewModel.holesV2.value.isEmpty() || forestViewModel.holesV2.value.isEmpty()) { //???????????????????????????????????????????????????????????????
                    forestViewModel.loadHolesAndHeads()
                } else {
                    forestViewModel.loadMoreForestHoles()
                }
                binding.recyclerViewForestHoles.isEnabled = false
            }
        }
    }

    private fun finishRefreshAnim() {
        binding.refreshLayout.finishRefresh() //????????????????????????
        binding.refreshLayout.finishLoadMore() //????????????????????????
        binding.recyclerViewForestHoles.isEnabled = true //???????????????????????????
    }

    private fun preloadAllForests() {
        val allForestViewModel: AllForestViewModel by activityViewModels()
        allForestViewModel.preload()
    }
}