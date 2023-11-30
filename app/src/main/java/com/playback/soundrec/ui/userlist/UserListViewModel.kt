package com.playback.soundrec.ui.userlist

import androidx.lifecycle.MutableLiveData
import com.playback.soundrec.bases.BaseViewModel
import com.playback.soundrec.model.User
import com.playback.soundrec.providers.FireBaseService

class UserListViewModel:BaseViewModel() {
    private val fireBaseService = FireBaseService.INSTANCE
    val users = MutableLiveData<List<User>>()

    fun fetchUsers() {
        fireBaseService!!.getAllUsers { usersList ->
            users.postValue(usersList ?: emptyList())
        }
    }
}