package com.bradmir.pescapr.ui.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bradmir.pescapr.data.PuntoPesca
import com.bradmir.pescapr.data.SpotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: SpotRepository,
    context: Context? = null
) : ViewModel() {

    private val _pinesComunidad = MutableStateFlow<List<PuntoPesca>>(emptyList())
    val pinesComunidad: StateFlow<List<PuntoPesca>> = _pinesComunidad

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()



    init {
        context?.let { observeNetworkConnectivity(it.applicationContext) }
    }

    fun observeNetworkConnectivity(appContext: Context) {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            _isOffline.value = false
            return
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOffline.value = false
            }

            override fun onLost(network: Network) {
                _isOffline.value = true
            }

            override fun onUnavailable() {
                _isOffline.value = true
            }
        }

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _isOffline.value = !hasInternet

        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshPins(userId: String, isPro: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val pins = repository.fetchCommunityPins(userId, isPro)
            _pinesComunidad.value = pins
            _isLoading.value = false
        }
    }

    suspend fun shareSpotToCommunity(spot: PuntoPesca): String? {
        return repository.shareSpotToCommunity(spot)
    }
}
