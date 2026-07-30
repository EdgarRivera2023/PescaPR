package com.bradmir.pescapr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bradmir.pescapr.data.PuntoPesca
import com.bradmir.pescapr.data.SpotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val repository: SpotRepository) : ViewModel() {

    private val _pinesComunidad = MutableStateFlow<List<PuntoPesca>>(emptyList())
    val pinesComunidad: StateFlow<List<PuntoPesca>> = _pinesComunidad

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun refreshPins(userId: String, isPro: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val pins = repository.fetchCommunityPins(userId, isPro)
            _pinesComunidad.value = pins
            _isLoading.value = false
        }
    }

    fun shareSpotToCommunity(spot: PuntoPesca) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.shareSpotToCommunity(spot)
        }
    }
}
