package com.close.hook.ads.ui.fragment.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.close.hook.ads.BuildConfig
import com.close.hook.ads.R
import com.close.hook.ads.databinding.FragmentHomeBinding
import com.close.hook.ads.debug.PerformanceActivity
import com.close.hook.ads.manager.ServiceManager
import com.close.hook.ads.ui.activity.AboutActivity
import com.close.hook.ads.ui.fragment.base.BaseFragment
import com.close.hook.ads.util.resolveColorAttr

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()
        initInfo()
    }

    @SuppressLint("SetTextI1n", "HardwareIds")
    private fun initInfo() {
        val context = requireContext()
        val isActivated = ServiceManager.isModuleActivated

        val colorAttr = if (isActivated) {
            android.R.attr.colorPrimary
        } else {
            android.R.attr.colorError
        }
        val primaryOrErrorColor = context.resolveColorAttr(colorAttr)

        binding.apply {
            status.setCardBackgroundColor(primaryOrErrorColor)
            statusIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    if (isActivated) R.drawable.ic_round_check_circle_24 else R.drawable.ic_about
                )
            )
            statusTitle.text = getString(if (isActivated) R.string.activated else R.string.not_activated)
            statusSummary.text = getString(R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

            setSystemInfo(context)
            setHyperLinks()
        }
    }

    private fun setSystemInfo(context: Context) {
        val contentResolver = context.contentResolver

        binding.apply {
            androidVersionValue.text = Build.VERSION.RELEASE
            sdkVersionValue.text = Build.VERSION.SDK_INT.toString()

            androidIdValue.text = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            brandValue.text = Build.MANUFACTURER
            modelValue.text = Build.MODEL

            skuValue.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SKU else ""

            typeValue.text = when {
                Build.TYPE == "user" -> "Release"
                Build.TYPE == "userdebug" -> "Debug"
                Build.TYPE == "eng" -> "Engineering"
                else -> Build.TYPE
            }

            fingerValue.text = Build.FINGERPRINT
        }
    }

    private fun setHyperLinks() {
        val linkMovementMethod = LinkMovementMethod.getInstance()
        binding.apply {
            viewSource.movementMethod = linkMovementMethod
            viewSource.text = HtmlCompat.fromHtml(
                getString(R.string.about_view_source_code, "<b><a href=\"https://github.com/zjyzip/AdClose\">GitHub</a></b>"),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            feedback.movementMethod = linkMovementMethod
            feedback.text = HtmlCompat.fromHtml(
                getString(R.string.join_telegram_channel, "<b><a href=\"https://t.me/AdClose\">Telegram</a></b>"),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
    }

    private fun initToolBar() {
        binding.toolbar.apply {
            title = getString(R.string.app_name)
            inflateMenu(R.menu.menu_home)

            setOnMenuItemClickListener {
                if (it.itemId == R.id.about) {
                    startActivity(Intent(requireContext(), AboutActivity::class.java))
                }
                true
            }

            val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    startActivity(Intent(requireContext(), PerformanceActivity::class.java))
                }
            })

            setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        }
    }
}
