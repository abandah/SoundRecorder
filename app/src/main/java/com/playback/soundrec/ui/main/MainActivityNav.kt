package com.playback.soundrec.ui.main

import android.view.View

interface MainActivityNav {
    fun onSettingsClick(view: View)
    fun onRecordClick(view: View)

    fun onExitClick(view: View)
}