package com.playback.soundrec.model

import com.playback.soundrec.AppConstants
import java.io.Serializable

class User() : Serializable{
    var admin: String? = "0"
    var user_id: String? = null
    var name: String? = null
    var phonenumber : String? = null
    var userName : String? = null
    var password : String? = null
    var setting : Setting? = null
    var soundSample : String? = null


    class Setting :Serializable{
        var defaultSampleRate : String? = "44100"
        var defaultFormat: String? = AppConstants.FORMAT_AAC
        var defaultEnableSendDataToServer : String?= "0"
        var defaultDelay: String? = "0"
        var defaultTimeToStartSoundSample: String? = "0"
        var defaultSoundSampleDuration : String?= "5"
    }

}