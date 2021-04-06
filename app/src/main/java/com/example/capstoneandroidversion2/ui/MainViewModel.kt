package com.example.capstoneandroidversion2.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//TODO: Put Bus communication values in here
class MainViewModel : ViewModel() {

    // LiveData
    private val _viewState = MutableLiveData<MainViewState>().apply {
        value = MainViewState()
    }
    val viewState: LiveData<MainViewState> = _viewState

    fun postReadValue(msg: String) {
        _viewState.postValue(_viewState.value?.appendMessage(msg))
    }

}
