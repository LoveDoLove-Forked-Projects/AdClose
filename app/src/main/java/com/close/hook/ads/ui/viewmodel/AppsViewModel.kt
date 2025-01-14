package com.close.hook.ads.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.close.hook.ads.R
import com.close.hook.ads.data.model.AppInfo
import com.close.hook.ads.data.repository.AppRepository
import com.close.hook.ads.util.PrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AppsViewModel(
    application: Application
) : AndroidViewModel(application) {

    var type: String = "user"
    private val updateParams =
        MutableStateFlow(Triple(Pair("", emptyList<String>()), Pair("", false), 0L))
    val appsLiveData: LiveData<List<AppInfo>> by lazy { setupAppsLiveData() }
    private val appRepository: AppRepository = AppRepository(
        packageManager = application.packageManager,
        context = application.applicationContext
    )

    init {
        refreshApps()
    }

    private fun setupAppsLiveData(): LiveData<List<AppInfo>> {
        return updateParams
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { (filter, params, _) ->
                flow {
                    val isSystem = when (type) {
                        "user" -> false
                        "system" -> true
                        else -> null
                    }
                    val apps = appRepository.getInstalledApps(isSystem)
                        .filter { type != "configured" || it.isEnable == 1 }

                    emit(apps)
                }.map { apps ->
                    appRepository.getFilteredAndSortedApps(
                        apps = apps,
                        filter = filter,
                        keyword = params.first,
                        isReverse = params.second
                    )
                }
            }
            .asLiveData(viewModelScope.coroutineContext)
    }

    fun refreshApps() {
        updateParams.value = updateParams.value.copy(
            first = Pair(PrefManager.order, getFilterList()),
            second = Pair("", PrefManager.isReverse),
            third = System.currentTimeMillis()
        )
    }

    private fun getFilterList(): List<String> {
        val context = getApplication<Application>()
        return listOfNotNull(
            if (PrefManager.configured) context.getString(R.string.filter_configured) else null,
            if (PrefManager.updated) context.getString(R.string.filter_recent_update) else null,
            if (PrefManager.disabled) context.getString(R.string.filter_disabled) else null
        )
    }

    fun updateList(
        filter: Pair<String, List<String>>,
        keyword: String,
        isReverse: Boolean
    ) {
        updateParams.value = updateParams.value.copy(
            first = filter,
            second = Pair(keyword, isReverse),
            third = System.currentTimeMillis()
        )
    }
}
