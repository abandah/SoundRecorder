package com.playback.soundrec.ui.settings

import android.view.View
import com.playback.soundrec.bases.BaseViewModel

class SettingActivityViewModel  : BaseViewModel(), SettingsActivityNav{
    var settingsActivityNav : SettingsActivityNav? = null
    override fun onBackClick(view: View) {
       settingsActivityNav?.onBackClick(view)
    }
}