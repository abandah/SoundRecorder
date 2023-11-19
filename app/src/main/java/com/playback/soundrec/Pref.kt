package com.playback.soundrec

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.playback.soundrec.model.User

class Pref {

    private var preferences : SharedPreferences? = null
    companion object{
        var INSTANCE : Pref? = null
        fun getInstance() : Pref{
            if(INSTANCE == null){
                INSTANCE = Pref()
            }
            return INSTANCE!!
        }
//        var defaultSampleRate : String = "44100"
//        var defaultFormat: String = AppConstants.FORMAT_AAC
//        var defaultEnableSendDataToServer : Boolean= false
//        var defaultDelay: String = "0"
//        var defaultTimeToStartSoundSample: String = "0"
//        var defaultSoundSampleDuration : String= "5"

    }
    init {
        preferences = App.context?.getSharedPreferences("soundrec", Context.MODE_PRIVATE)
    }

    private fun setString(key : String, value : String){
        preferences?.edit()?.putString(key, value)?.apply()
    }
    private fun getString(key : String,default: String) : String?{
        return preferences?.getString(key, default)
    }

    fun saveUser(user: User?){
        if(user == null){
            setString("user", "")
            return
        }
        val string = Gson().toJson(user)
        setString("user", string)

    }
    fun getUser() : User?{
        val string = getString("user", "")
        if(string.isNullOrEmpty()){
            return null
        }
        return Gson().fromJson(string, User::class.java)
    }
}