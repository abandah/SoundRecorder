package com.playback.soundrec.ui.login

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.playback.soundrec.Pref
import com.playback.soundrec.bases.BaseViewModel
import com.playback.soundrec.providers.FireBaseService

class LoginActivityViewModel:BaseViewModel() {
    var username : MutableLiveData<String> = MutableLiveData("m3bndah@gmail.com")
    var password : MutableLiveData<String> = MutableLiveData("123456789")

    var nav: LoginNav? = null

    fun login(view : View){
        //validate
        if(username.value.isNullOrEmpty()){
            toast("Please enter username")
            return
        }
        if(password.value.isNullOrEmpty()){
            toast("Please enter password")
            return
        }
        //login
        FireBaseService.INSTANCE?.login(username.value!!,password.value!!){
            if(it != null){
                nav?.toast("Login success")
                Pref.getInstance().saveUser(it)
                nav?.openMainActivity()
            }else{
                nav?.toast("Login failed")
            }
        }
    }

    private fun toast(s: String) {
        nav?.toast(s)
    }
}