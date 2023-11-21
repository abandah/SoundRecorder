package com.playback.soundrec.providers

import android.adservices.adid.AdId
import com.playback.soundrec.model.User
import java.io.File

interface CallsAPi {
    fun login(username: String, password: String, callback: (User?) -> Unit)
    fun getUserInfo(userId: String , callback: (User?) -> Unit)
    fun uploadFile(userId: String, file: File, callback: (Boolean) -> Unit)
    fun getFile(userId: String, callback: (File?) -> Unit)
    fun createUser(user:User, callback: (String?) -> Unit)
    fun updatePassword(userId: String, password: String, callback: (Boolean) -> Unit)
    fun updateSoundSample(userId: String, mediaUrl: String, callback: (Boolean) -> Unit)

//    fun updateSettingSampleRate(userId: String, sampleRate: String, callback: (Boolean) -> Unit)
//    fun updateSettingFormat(userId: String, format: String, callback: (Boolean) -> Unit)
//    fun updateSettingEnableSendDataToServer(userId: String, enableSendDataToServer: Boolean, callback: (Boolean) -> Unit)
//    fun updateSettingDelay(userId: String, delay: String, callback: (Boolean) -> Unit)
//    fun updateSettingTimeToStartSoundSample(userId: String, timeToStartSoundSample: String, callback: (Boolean) -> Unit)
//    fun updateSettingSoundSampleDuration(userId: String, soundSampleDuration: String, callback: (Boolean) -> Unit)

    fun getAllUsers(callback: (List<User>?) -> Unit)

    fun updateField(parent :String? , userId: String, field: String, value: String, callback: (String) -> Unit)
    abstract fun deleteFile(userId: String, callback: (Boolean) -> Unit)


}