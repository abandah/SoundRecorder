package com.playback.soundrec.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.lifecycle.MutableLiveData
import com.playback.soundrec.bases.BaseViewModel

class MainActivityViewModel : BaseViewModel() {
    var isRecording: MutableLiveData<Boolean> = MutableLiveData(false)
    var recordInfo: MutableLiveData<String> = MutableLiveData("")
    var progressTime: MutableLiveData<String> = MutableLiveData("")

    var mainActivityNav: MainActivityNav? = null

    fun onRecordClick(view: View) {
       mainActivityNav?.onRecordClick(view)
    }

    fun onSettingsClick(view: View) {
        mainActivityNav?.onSettingsClick(view)

    }

    fun onExitClick(view: View) {
        mainActivityNav?.onExitClick(view)

    }
}