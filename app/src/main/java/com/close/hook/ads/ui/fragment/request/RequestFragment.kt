package com.close.hook.ads.ui.fragment.request

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.close.hook.ads.R
import com.close.hook.ads.data.model.RequestInfo
import com.close.hook.ads.databinding.BaseTablayoutViewpagerBinding
import com.close.hook.ads.ui.fragment.base.BasePagerFragment
import com.close.hook.ads.ui.viewmodel.RequestViewModel
import com.close.hook.ads.util.IOnFabClickContainer
import com.close.hook.ads.util.IOnFabClickListener
import com.close.hook.ads.util.OnBackPressListener
import com.close.hook.ads.util.dp
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RequestFragment : BasePagerFragment(), IOnFabClickContainer {

    private val viewModel: RequestViewModel by viewModels({ requireActivity() })

    override val tabList: List<Int> =
        listOf(R.string.tab_request_list, R.string.tab_block_list, R.string.tab_pass_list)
    override var fabController: IOnFabClickListener? = null

    private lateinit var fab: FloatingActionButton
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }

    private val backPressDelegates = mutableMapOf<Int, OnBackPressListener>()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("request", RequestInfo::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("request")
            }
            request?.let { item ->
                viewModel.updateRequestList(item)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BaseTablayoutViewpagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFab()
        initBar()
        setupBroadcastReceiver()
    }

    private fun setupBroadcastReceiver() {
        val filter = IntentFilter("com.rikkati.REQUEST")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            requireContext().registerReceiver(receiver, filter)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(receiver)
    }

    private fun setupFab() {
        if (!::fab.isInitialized) {
            fab = FloatingActionButton(requireContext()).apply {
                layoutParams = CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.END
                    behavior = fabViewBehavior
                }
                setImageResource(R.drawable.ic_export)
                tooltipText = getString(R.string.export)
                setOnClickListener { fabController?.onExport() }
            }
            binding.root.addView(fab)
            updateFabMargin()
        }
    }

    private fun updateFabMargin() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            fab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                rightMargin = 25.dp
                bottomMargin = navigationBars.bottom + 105.dp
            }
            insets
        }
    }

    private fun initBar() {
        binding.toolBar.apply {
            inflateMenu(R.menu.menu_clear)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.clear -> controller?.onClearAll()
                }
                true
            }
        }
        binding.editText.hint = getString(R.string.search_hint)
    }

    override fun initView() {
        super.initView()
        binding.viewPager.isUserInputEnabled = false
    }

    override fun search(text: String) {
        controller?.search(text)
    }

    override fun getFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> RequestListFragment.newInstance("all")
            1 -> RequestListFragment.newInstance("block")
            2 -> RequestListFragment.newInstance("pass")
            else -> throw IllegalArgumentException()
        }
        if (fragment is OnBackPressListener) {
            backPressDelegates[position] = fragment
        }
        return fragment
    }

    override fun onBackPressed(): Boolean {
        val currentChildListener = backPressDelegates[binding.viewPager.currentItem]
        if (currentChildListener?.onBackPressed() == true) {
            return true
        }
        return super.onBackPressed()
    }
}
