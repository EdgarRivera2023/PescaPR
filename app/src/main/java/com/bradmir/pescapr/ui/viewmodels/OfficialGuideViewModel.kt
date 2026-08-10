package com.bradmir.pescapr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bradmir.pescapr.data.model.FichaPez
import com.bradmir.pescapr.data.OfficialGuideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfficialGuideViewModel(private val repository: OfficialGuideRepository) : ViewModel() {

    private val _fichas = MutableStateFlow<List<FichaPez>>(emptyList())
    val fichas: StateFlow<List<FichaPez>> = _fichas.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeOfficialGuide().collect { guide ->
                _fichas.value = guide
            }
        }
    }
}
