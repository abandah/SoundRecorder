package com.playback.soundrec

import android.content.Context
import android.content.SharedPreferences

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

        var defaultSampleRate = "44100"
        var defaultFormat = AppConstants.FORMAT_PCM
        var defaultEnableSendDataToServer = false
        var defaultDelay = "0"
        var defaultTimeToStartSoundSample = "0"
        var defaultSoundSampleDuration = "5"

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

    private fun setBoolean(key : String, value : Boolean){
        preferences?.edit()?.putBoolean(key, value)?.apply()
    }
    private fun getBoolean(key : String,default: Boolean) : Boolean{
        return preferences?.getBoolean(key, default)!!
    }
    fun setSampleRate(value : String){
        setString("sample_rate", value)
    }
    fun getSampleRate() : String{
        return getString("sample_rate" , defaultSampleRate)!!
    }

    fun setFormat(value : String){
        setString("format", value)
    }
    fun getFormat() : String{
        return getString("format" , defaultFormat)!!
    }

    fun setEnableSendDataToServer(value : Boolean){
        setBoolean("enablesendtoserver", value)
    }
    fun getEnableSendDataToServer() : Boolean{
        return getBoolean("enablesendtoserver" , defaultEnableSendDataToServer)
    }

    fun setDelay(toInt: Int) {
        setString("delay", toInt.toString())
    }
    fun getDelay() : Int{
        return getString("delay" , defaultDelay)!!.toInt()
    }

    fun setTimeToStartSoundSample(toInt: Int) {
        setString("time_to_start_sound_sample", toInt.toString())
    }
    fun getTimeToStartSoundSample() : Int{
        return getString("time_to_start_sound_sample" , defaultTimeToStartSoundSample)!!.toInt()
    }

    fun setSoundSampleDuration(toInt: Int) {
        setString("sound_sample_duration", toInt.toString())
    }
    fun getSoundSampleDuration() : Int{
        return getString("sound_sample_duration" , defaultSoundSampleDuration)!!.toInt()
    }
}