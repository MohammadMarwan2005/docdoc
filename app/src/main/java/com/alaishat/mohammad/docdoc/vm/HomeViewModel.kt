package com.alaishat.mohammad.docdoc.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alaishat.mohammad.data.remote.APIService
import com.alaishat.mohammad.domain.model.core.LocalError
import com.alaishat.mohammad.domain.model.home.HomeResponse
import com.alaishat.mohammad.domain.usecase.GetUserTokenAndNameUseCase
import com.alaishat.mohammad.domain.usecase.SetFirstVisitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Mohammad Al-Aishat on Aug/21/2024.
 * DocDoc Project.
 */

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserTokenAndNameUseCase: GetUserTokenAndNameUseCase,// Wow, I am using UseCases 🔥
    private val setFirstVisitUseCase: SetFirstVisitUseCase, // Wow, I am using UseCases 🔥
    private val apiService: APIService, // WTF 😱😱😱😱😱😱😱😱
) : ViewModel() {

    private val _userTokenAndName: MutableStateFlow<Pair<String?, String?>> = MutableStateFlow(null to null)
    val userTokenAndName = _userTokenAndName.asStateFlow()

    private val _homeResponse: MutableStateFlow<HomeResponse?> = MutableStateFlow(null)
    val homeResponse = _homeResponse.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    init {
        viewModelScope.launch {
            _userTokenAndName.value = getUserTokenAndNameUseCase()
            getHomeResponse()
        }
    }

    fun setFirstVisit() {
        viewModelScope.launch {
            setFirstVisitUseCase.invoke()
        }
    }


    fun getHomeResponse() {
        viewModelScope.launch {
            if (isLoading.value) return@launch
            userTokenAndName.value.first?.let {
                _isLoading.value = true
                _homeResponse.value = try {
                    apiService.getHomeResponse(token = it)
                } catch (e: Exception) {
                    LocalError()
                }
                _isLoading.value = false
            }
        }
    }

    fun resetHomeResponse() {
        _homeResponse.value = null
    }
}
