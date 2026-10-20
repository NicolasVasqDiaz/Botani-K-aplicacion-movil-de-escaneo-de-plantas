package com.botanik.app.ui.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val isLastPage: Boolean = false
)

private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    companion object {
        const val PAGE_COUNT = 3
    }

    fun onPageChanged(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page, isLastPage = page == PAGE_COUNT - 1)
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETED_KEY] = true }
            onDone()
        }
    }
}
